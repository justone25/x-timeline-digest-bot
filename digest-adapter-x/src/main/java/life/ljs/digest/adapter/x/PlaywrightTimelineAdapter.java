package life.ljs.digest.adapter.x;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.microsoft.playwright.*;
import com.microsoft.playwright.options.Cookie;
import life.ljs.digest.domain.model.Tweet;
import life.ljs.digest.domain.port.TimelinePort;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Profile;
import org.springframework.stereotype.Component;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.time.OffsetDateTime;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;
import java.util.*;
import java.util.stream.Collectors;

@Component
@Profile("playwright")
public class PlaywrightTimelineAdapter implements TimelinePort {

    private static final Logger log = LoggerFactory.getLogger(PlaywrightTimelineAdapter.class);

    private final String cookiesPath;
    private final boolean headless;
    private final int timeoutMs;
    private final int maxScrolls;
    private final ObjectMapper objectMapper;

    public PlaywrightTimelineAdapter(
            @Value("${twitter.playwright.cookies-path:}") String cookiesPath,
            @Value("${twitter.playwright.headless:true}") boolean headless,
            @Value("${twitter.playwright.timeout:30000}") int timeoutMs,
            @Value("${twitter.playwright.max-scrolls:10}") int maxScrolls,
            ObjectMapper objectMapper) {
        this.cookiesPath = cookiesPath;
        this.headless = headless;
        this.timeoutMs = timeoutMs;
        this.maxScrolls = maxScrolls;
        this.objectMapper = objectMapper;
    }

    @Override
    public List<Tweet> fetchLatestTweets(String lastSeenTweetId) {
        if (cookiesPath == null || cookiesPath.isBlank()) {
            log.warn("twitter.playwright.cookies-path is not configured, skipping Playwright fetch.");
            return List.of();
        }

        Path path = Path.of(cookiesPath);
        if (!Files.exists(path)) {
            log.warn("Cookies file not found at: {}", cookiesPath);
            return List.of();
        }

        try (Playwright playwright = Playwright
                .create(new Playwright.CreateOptions().setEnv(Map.of("PLAYWRIGHT_SKIP_BROWSER_DOWNLOAD", "1")))) {
            Browser browser = playwright.chromium().launch(new BrowserType.LaunchOptions().setHeadless(headless));
            BrowserContext context = browser.newContext();

            // Load cookies
            try {
                byte[] bytes = Files.readAllBytes(path);
                com.fasterxml.jackson.databind.JsonNode rootNode = objectMapper.readTree(bytes);
                if (rootNode.isArray()) {
                    for (com.fasterxml.jackson.databind.JsonNode node : rootNode) {
                        if (node.has("sameSite")) {
                            com.fasterxml.jackson.databind.node.ObjectNode objectNode = (com.fasterxml.jackson.databind.node.ObjectNode) node;
                            String sameSite = node.get("sameSite").asText();
                            if ("unspecified".equalsIgnoreCase(sameSite)
                                    || "no_restriction".equalsIgnoreCase(sameSite)) {
                                objectNode.put("sameSite", "NONE");
                            } else {
                                objectNode.put("sameSite", sameSite.toUpperCase());
                            }
                        }
                    }
                }
                List<Cookie> cookies = objectMapper.convertValue(rootNode, new TypeReference<List<Cookie>>() {
                });
                context.addCookies(cookies);
            } catch (IOException e) {
                log.error("Failed to load cookies from {}", cookiesPath, e);
                return List.of();
            }

            Page page = context.newPage();
            page.setDefaultTimeout(timeoutMs);

            log.info("Navigating to X home...");
            page.navigate("https://x.com/home");

            // Wait for timeline to load (look for a tweet article)
            try {
                page.waitForSelector("article[data-testid='tweet']",
                        new Page.WaitForSelectorOptions().setTimeout(timeoutMs));
            } catch (TimeoutError e) {
                log.error("Timeout waiting for timeline to load. Check login status or network.");
                // Maybe take screenshot?
                return List.of();
            }

            // Ensure "For You" is selected (usually default, but good to check if possible,
            // skipping for simplicity now)

            Set<Tweet> allTweets = new HashSet<>();
            for (int i = 0; i < maxScrolls; i++) {
                List<Tweet> batch = scrapeVisibleTweets(page);
                int beforeSize = allTweets.size();
                allTweets.addAll(batch);
                int afterSize = allTweets.size();

                log.debug("Scroll {}: Found {} tweets (Total unique: {})", i + 1, batch.size(), afterSize);

                if (afterSize == beforeSize && i > 2) {
                    // Stop if no new tweets found for a while
                    log.debug("No new tweets found, stopping scroll.");
                    break;
                }

                page.evaluate("window.scrollBy(0, 2000)");
                page.waitForTimeout(2000); // Wait for load
            }

            return new ArrayList<>(allTweets).stream()
                    .sorted(Comparator.comparing(Tweet::getCreatedAt).reversed())
                    .collect(Collectors.toList());

        } catch (Exception e) {
            log.error("Error during Playwright execution", e);
            return List.of();
        }
    }

    private List<Tweet> scrapeVisibleTweets(Page page) {
        List<Tweet> results = new ArrayList<>();
        List<ElementHandle> articles = page.querySelectorAll("article[data-testid='tweet']");

        for (ElementHandle article : articles) {
            try {
                // Extract ID from link
                // Usually: /User/status/123456789
                ElementHandle timeElement = article.querySelector("time");
                if (timeElement == null)
                    continue;

                String timeStr = timeElement.getAttribute("datetime");
                OffsetDateTime createdAt = parseTime(timeStr);

                ElementHandle linkElement = article.querySelector("a[href*='/status/']");
                if (linkElement == null)
                    continue;

                String href = linkElement.getAttribute("href");
                String tweetId = extractTweetId(href);
                if (tweetId == null)
                    continue;

                // Extract Text
                ElementHandle textElement = article.querySelector("div[data-testid='tweetText']");
                String text = textElement != null ? textElement.innerText() : "";

                // Extract Author
                ElementHandle authorElement = article.querySelector("div[data-testid='User-Name']");
                String authorName = authorElement != null ? authorElement.innerText().replace("\n", " ") : "Unknown";

                // Extract Stats (approximate)
                long replyCount = parseStat(article, "reply");
                long retweetCount = parseStat(article, "retweet");
                long likeCount = parseStat(article, "like");

                // Lang detection is hard from UI, default to "en" or empty
                String lang = "en";

                Tweet tweet = new Tweet(
                        tweetId,
                        authorName,
                        text,
                        createdAt,
                        likeCount,
                        retweetCount,
                        replyCount,
                        "https://x.com" + href,
                        lang);
                results.add(tweet);

            } catch (Exception e) {
                // Ignore single failure
                log.trace("Failed to parse a tweet element", e);
            }
        }
        return results;
    }

    private String extractTweetId(String href) {
        // href example: /user/status/123456
        if (href == null)
            return null;
        int idx = href.indexOf("/status/");
        if (idx >= 0) {
            String sub = href.substring(idx + 8);
            int end = sub.indexOf("/"); // Remove potential trailing params or routes
            if (end > 0)
                return sub.substring(0, end);
            return sub;
        }
        return null;
    }

    private OffsetDateTime parseTime(String timeStr) {
        try {
            return OffsetDateTime.parse(timeStr);
        } catch (DateTimeParseException e) {
            return OffsetDateTime.now();
        }
    }

    private long parseStat(ElementHandle article, String type) {
        // X (Twitter) uses different data-testid attributes for engagement buttons
        // "reply", "retweet", "like", "bookmark" etc.
        try {
            // Try multiple strategies to find the stat

            // Strategy 1: Find button with data-testid and check aria-label
            ElementHandle button = article.querySelector("button[data-testid='" + type + "']");
            if (button != null) {
                String ariaLabel = button.getAttribute("aria-label");
                if (ariaLabel != null) {
                    log.trace("Found {} aria-label: {}", type, ariaLabel);
                    // aria-label format: "123 Likes", "45 Retweets", "67 Replies"
                    String[] parts = ariaLabel.split(" ");
                    if (parts.length > 0) {
                        long count = parseCount(parts[0]);
                        if (count > 0) {
                            return count;
                        }
                    }
                }

                // Strategy 2: Check inner text of the button's span
                ElementHandle span = button.querySelector("span");
                if (span != null) {
                    String text = span.innerText();
                    if (text != null && !text.isEmpty()) {
                        log.trace("Found {} span text: {}", type, text);
                        long count = parseCount(text.trim());
                        if (count > 0) {
                            return count;
                        }
                    }
                }
            }

            // Strategy 3: Look for group with role="group" containing the stat
            List<ElementHandle> groups = article.querySelectorAll("div[role='group']");
            for (ElementHandle group : groups) {
                String ariaLabel = group.getAttribute("aria-label");
                if (ariaLabel != null && ariaLabel.toLowerCase().contains(type)) {
                    log.trace("Found {} in group aria-label: {}", type, ariaLabel);
                    String[] parts = ariaLabel.split(" ");
                    if (parts.length > 0) {
                        long count = parseCount(parts[0]);
                        if (count > 0) {
                            return count;
                        }
                    }
                }
            }

        } catch (Exception e) {
            log.trace("Error parsing {} stat", type, e);
        }

        return 0;
    }

    private long parseCount(String str) {
        if (str == null || str.isEmpty()) {
            return 0;
        }

        try {
            // Remove commas and spaces
            str = str.replace(",", "").replace(" ", "").trim();

            // Handle K, M suffixes
            if (str.endsWith("K") || str.endsWith("k")) {
                return (long) (Double.parseDouble(str.substring(0, str.length() - 1)) * 1000);
            }
            if (str.endsWith("M") || str.endsWith("m")) {
                return (long) (Double.parseDouble(str.substring(0, str.length() - 1)) * 1000000);
            }

            // Try to parse as long
            return Long.parseLong(str);
        } catch (NumberFormatException e) {
            log.trace("Failed to parse count from: {}", str);
            return 0;
        }
    }
}
