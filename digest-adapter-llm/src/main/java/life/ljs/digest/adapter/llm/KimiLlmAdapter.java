package life.ljs.digest.adapter.llm;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.databind.ObjectMapper;
import life.ljs.digest.domain.model.TopicCluster;
import life.ljs.digest.domain.model.Tweet;
import life.ljs.digest.domain.port.LlmPort;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Profile;
import org.springframework.stereotype.Component;

import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.time.Duration;
import java.util.*;
import java.util.stream.Collectors;

@Component
@Profile("!fake")
public class KimiLlmAdapter implements LlmPort {

    private static final Logger log = LoggerFactory.getLogger(KimiLlmAdapter.class);

    private final String apiKey;
    private final String baseUrl;
    private final String model;
    private final int timeout;
    private final HttpClient httpClient;
    private final ObjectMapper objectMapper;

    public KimiLlmAdapter(
            @Value("${kimi.api-key:}") String apiKey,
            @Value("${kimi.base-url:https://api.moonshot.cn/v1}") String baseUrl,
            @Value("${kimi.model:kimi-k2}") String model,
            @Value("${kimi.timeout:30000}") int timeout) {
        this.apiKey = apiKey;
        this.baseUrl = baseUrl.endsWith("/") ? baseUrl.substring(0, baseUrl.length() - 1) : baseUrl;
        this.model = model;
        this.timeout = timeout;
        this.httpClient = HttpClient.newBuilder()
                .connectTimeout(Duration.ofMillis(timeout))
                .build();
        this.objectMapper = new ObjectMapper();
    }

    @Override
    public String summarizeTweet(Tweet tweet) {
        if (apiKey == null || apiKey.isBlank()) {
            log.warn("Kimi API key not configured, using fallback");
            return fallbackSummarize(tweet.getText());
        }

        String prompt = String.format(
                "请用一句话(不超过60字)概括以下推文的核心观点，直接输出概括内容，不要加\"概括:\"等前缀:\n\n%s",
                tweet.getText());

        try {
            String response = callKimiApi(prompt, 100);
            tweet.setSummary(response.trim());
            return response.trim();
        } catch (Exception e) {
            log.error("Failed to summarize tweet with Kimi", e);
            return fallbackSummarize(tweet.getText());
        }
    }

    @Override
    public List<TopicCluster> clusterTweets(List<Tweet> tweets) {
        if (apiKey == null || apiKey.isBlank() || tweets.isEmpty()) {
            log.warn("Kimi API key not configured or no tweets, using fallback clustering");
            return fallbackCluster(tweets);
        }

        // Build tweet list for prompt
        StringBuilder tweetList = new StringBuilder();
        Map<Integer, Tweet> indexMap = new HashMap<>();
        for (int i = 0; i < tweets.size(); i++) {
            tweetList.append(i).append(". ").append(tweets.get(i).getText()).append("\n\n");
            indexMap.put(i, tweets.get(i));
        }

        String prompt = String.format(
                "以下是%d条推文,请分析它们的主题并分组。要求:\n" +
                        "1. 每个主题用emoji+简短名称概括(如\"🤖 AI工具应用\")\n" +
                        "2. 返回JSON数组格式: [{\"topic\": \"主题名\", \"indices\": [推文序号列表]}]\n" +
                        "3. 每条推文只能属于一个主题\n" +
                        "4. 直接输出JSON,不要markdown代码块\n\n" +
                        "推文列表:\n%s",
                tweets.size(), tweetList);

        try {
            String response = callKimiApi(prompt, 800);
            return parseClusterResponse(response, indexMap);
        } catch (Exception e) {
            log.error("Failed to cluster tweets with Kimi", e);
            return fallbackCluster(tweets);
        }
    }

    @Override
    public String summarizeBatch(List<TopicCluster> clusters, List<Tweet> top3) {
        if (apiKey == null || apiKey.isBlank()) {
            log.warn("Kimi API key not configured, using fallback");
            return fallbackBatchSummary(clusters, top3);
        }

        // Build context
        StringBuilder topicsDesc = new StringBuilder();
        for (TopicCluster c : clusters) {
            topicsDesc.append("- ").append(c.getTopicName())
                    .append(" (").append(c.size()).append("条)\n");
        }

        StringBuilder top3Desc = new StringBuilder();
        for (int i = 0; i < Math.min(3, top3.size()); i++) {
            Tweet t = top3.get(i);
            top3Desc.append(i + 1).append(". ")
                    .append(t.getAuthor()).append(": ")
                    .append(t.getText().substring(0, Math.min(100, t.getText().length())))
                    .append("\n");
        }

        String prompt = String.format(
                "根据以下主题分布和热门推文,生成一段专业的趋势洞察(100-150字):\n\n" +
                        "主题分布:\n%s\n" +
                        "Top推文:\n%s\n" +
                        "要求:\n" +
                        "1. 指出主要趋势和热点话题\n" +
                        "2. 提供有价值的观察和建议\n" +
                        "3. 语气专业但易读\n" +
                        "4. 直接输出内容,不要标题",
                topicsDesc, top3Desc);

        try {
            return callKimiApi(prompt, 300);
        } catch (Exception e) {
            log.error("Failed to generate batch summary with Kimi", e);
            return fallbackBatchSummary(clusters, top3);
        }
    }

    private String callKimiApi(String prompt, int maxTokens) throws Exception {
        ChatCompletionRequest request = new ChatCompletionRequest();
        request.model = this.model;
        request.messages = List.of(new Message("user", prompt));
        request.maxTokens = maxTokens;
        request.temperature = 0.7;

        String requestBody = objectMapper.writeValueAsString(request);

        HttpRequest httpRequest = HttpRequest.newBuilder()
                .uri(URI.create(baseUrl + "/chat/completions"))
                .header("Content-Type", "application/json")
                .header("Authorization", "Bearer " + apiKey)
                .timeout(Duration.ofMillis(timeout))
                .POST(HttpRequest.BodyPublishers.ofString(requestBody))
                .build();

        HttpResponse<String> response = httpClient.send(httpRequest, HttpResponse.BodyHandlers.ofString());

        if (response.statusCode() != 200) {
            throw new RuntimeException("Kimi API returned " + response.statusCode() + ": " + response.body());
        }

        ChatCompletionResponse chatResponse = objectMapper.readValue(response.body(), ChatCompletionResponse.class);
        if (chatResponse.choices == null || chatResponse.choices.isEmpty()) {
            throw new RuntimeException("Empty response from Kimi API");
        }

        return chatResponse.choices.get(0).message.content;
    }

    private List<TopicCluster> parseClusterResponse(String response, Map<Integer, Tweet> indexMap) {
        try {
            // Clean response - remove markdown code blocks if present
            String cleaned = response.trim();
            if (cleaned.startsWith("```json")) {
                cleaned = cleaned.substring(7);
            }
            if (cleaned.startsWith("```")) {
                cleaned = cleaned.substring(3);
            }
            if (cleaned.endsWith("```")) {
                cleaned = cleaned.substring(0, cleaned.length() - 3);
            }
            cleaned = cleaned.trim();

            ClusterResult[] results = objectMapper.readValue(cleaned, ClusterResult[].class);
            List<TopicCluster> clusters = new ArrayList<>();

            for (ClusterResult result : results) {
                List<Tweet> clusterTweets = new ArrayList<>();
                if (result.indices != null) {
                    for (Integer idx : result.indices) {
                        Tweet tweet = indexMap.get(idx);
                        if (tweet != null) {
                            clusterTweets.add(tweet);
                        }
                    }
                }
                if (!clusterTweets.isEmpty()) {
                    clusters.add(new TopicCluster(result.topic, clusterTweets));
                }
            }

            return clusters.isEmpty() ? fallbackCluster(new ArrayList<>(indexMap.values())) : clusters;
        } catch (Exception e) {
            log.error("Failed to parse cluster response", e);
            return fallbackCluster(new ArrayList<>(indexMap.values()));
        }
    }

    // Fallback methods
    private String fallbackSummarize(String text) {
        if (text.length() > 100) {
            int period = text.indexOf('。');
            if (period > 0 && period < 80) {
                return text.substring(0, period + 1);
            }
            return text.substring(0, 80) + "...";
        }
        return text;
    }

    private List<TopicCluster> fallbackCluster(List<Tweet> tweets) {
        List<Tweet> aiTech = new ArrayList<>();
        List<Tweet> businessGrowth = new ArrayList<>();
        List<Tweet> other = new ArrayList<>();

        for (Tweet t : tweets) {
            String lower = t.getText().toLowerCase();
            if (containsAny(lower, "ai", "llm", "gpt", "模型", "prompt")) {
                aiTech.add(t);
            } else if (containsAny(lower, "创业", "增长", "粉丝", "business", "growth")) {
                businessGrowth.add(t);
            } else {
                other.add(t);
            }
        }

        List<TopicCluster> clusters = new ArrayList<>();
        if (!aiTech.isEmpty())
            clusters.add(new TopicCluster("🤖 AI与工具", aiTech));
        if (!businessGrowth.isEmpty())
            clusters.add(new TopicCluster("💼 创业与增长", businessGrowth));
        if (!other.isEmpty())
            clusters.add(new TopicCluster("🎯 其他动态", other));

        return clusters;
    }

    private String fallbackBatchSummary(List<TopicCluster> clusters, List<Tweet> top3) {
        if (clusters.isEmpty())
            return "本时段暂无明显趋势,内容较为分散。";
        return "本时段内容聚焦于「" + stripEmoji(clusters.get(0).getTopicName()) +
                "」等话题,值得关注。";
    }

    private boolean containsAny(String text, String... keywords) {
        for (String keyword : keywords) {
            if (text.contains(keyword))
                return true;
        }
        return false;
    }

    private String stripEmoji(String text) {
        return text.replaceAll("[\\p{So}\\p{Sk}]", "").trim();
    }

    // Request/Response DTOs
    static class ChatCompletionRequest {
        public String model;
        public List<Message> messages;
        @JsonProperty("max_tokens")
        public Integer maxTokens;
        public Double temperature;
    }

    @JsonIgnoreProperties(ignoreUnknown = true)
    static class Message {
        public String role;
        public String content;

        public Message() {
        }

        public Message(String role, String content) {
            this.role = role;
            this.content = content;
        }
    }

    @JsonIgnoreProperties(ignoreUnknown = true)
    static class ChatCompletionResponse {
        public List<Choice> choices;
    }

    @JsonIgnoreProperties(ignoreUnknown = true)
    static class Choice {
        public Message message;
    }

    static class ClusterResult {
        public String topic;
        public List<Integer> indices;
    }
}
