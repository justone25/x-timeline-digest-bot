package life.ljs.digest.app.adapter.fake;

import life.ljs.digest.domain.model.DigestBatch;
import life.ljs.digest.domain.model.TopicCluster;
import life.ljs.digest.domain.model.Tweet;
import life.ljs.digest.domain.port.DiscordPort;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.annotation.Profile;
import org.springframework.stereotype.Component;

import java.time.Duration;
import java.time.OffsetDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;

@Component
@Profile("fake")
public class FakeDiscordAdapter implements DiscordPort {
        private static final Logger log = LoggerFactory.getLogger(FakeDiscordAdapter.class);
        private static final DateTimeFormatter TIME_FORMATTER = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm");

        @Override
        public void pushDigest(DigestBatch batch) {
                StringBuilder sb = new StringBuilder();

                // Header
                sb.append("\n╔════════════════════════════════════════════════════════════╗\n");
                sb.append("║          📊 X Timeline Digest - 精选摘要              ║\n");
                sb.append("╚════════════════════════════════════════════════════════════╝\n\n");

                // Time window and stats
                sb.append("⏰ 时间段: ")
                                .append(batch.getStartTime().format(TIME_FORMATTER))
                                .append(" - ")
                                .append(batch.getEndTime().format(TIME_FORMATTER))
                                .append("\n");

                sb.append("📈 已处理: ")
                                .append(batch.getAllTweets().size())
                                .append(" 条推文");

                // Calculate total engagement
                long totalEngagement = batch.getAllTweets().stream()
                                .mapToLong(t -> t.getLikeCount() + t.getRetweetCount() + t.getReplyCount())
                                .sum();

                if (totalEngagement > 0) {
                        sb.append(" | 总互动: ").append(formatNumber(totalEngagement));
                }
                sb.append("\n\n");

                // Separator
                sb.append("━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━\n\n");

                // Top 3
                sb.append("🏆 高光时刻 TOP 3\n\n");
                int idx = 1;
                for (Tweet t : batch.getTop3()) {
                        sb.append(getNumberEmoji(idx)).append(" ");

                        // Extract title from first sentence or use text
                        String title = extractTitle(t.getText());
                        sb.append("**").append(title).append("**\n");

                        sb.append("   👤 ")
                                        .append(t.getAuthor())
                                        .append(" | ")
                                        .append(getRelativeTime(t.getCreatedAt()))
                                        .append("\n");

                        // Summary (if different from title)
                        String summary = t.getSummary();
                        if (summary != null && !summary.equals(title) && !summary.startsWith("[摘要]")) {
                                sb.append("   💡 ").append(summary).append("\n");
                        }

                        // Engagement
                        sb.append("   📊 ")
                                        .append(formatNumber(t.getLikeCount())).append(" ❤️ · ")
                                        .append(formatNumber(t.getRetweetCount())).append(" 🔄 · ")
                                        .append(formatNumber(t.getReplyCount())).append(" 💬\n");

                        sb.append("   🔗 [查看详情](").append(t.getUrl()).append(")\n\n");
                        idx++;
                }

                // Separator
                sb.append("━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━\n\n");

                // Topics
                sb.append("📚 主题分布\n\n");
                for (TopicCluster c : batch.getClusters()) {
                        sb.append(c.getTopicName())
                                        .append(" (").append(c.size()).append("条)\n");

                        // Show top keywords or sample tweets
                        if (!c.getTweets().isEmpty()) {
                                sb.append("   热门话题: ");
                                String keywords = extractKeywords(c.getTweets());
                                sb.append(keywords).append("\n");
                        }
                        sb.append("\n");
                }

                // Separator
                sb.append("━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━\n\n");

                // Overview
                sb.append("💭 趋势洞察\n\n");
                String overview = batch.getOverviewSummary();
                // Format overview with line breaks for readability
                String formattedOverview = wrapText(overview, 60);
                sb.append(formattedOverview).append("\n\n");

                // Footer
                sb.append("═══════════════════════════════════════════════════════════\n");

                log.info(sb.toString());
        }

        private String getNumberEmoji(int num) {
                return switch (num) {
                        case 1 -> "1️⃣";
                        case 2 -> "2️⃣";
                        case 3 -> "3️⃣";
                        default -> num + ".";
                };
        }

        private String extractTitle(String text) {
                if (text == null || text.isEmpty())
                        return "无标题";

                // Try to get first sentence
                int period = text.indexOf('。');
                int question = text.indexOf('?');
                int exclamation = text.indexOf('!');

                int end = text.length();
                if (period > 0 && period < 50)
                        end = Math.min(end, period);
                if (question > 0 && question < 50)
                        end = Math.min(end, question);
                if (exclamation > 0 && exclamation < 50)
                        end = Math.min(end, exclamation);

                String title = text.substring(0, Math.min(end, 60));
                if (end < text.length() && end < 60) {
                        title += "。";
                } else if (title.length() >= 60) {
                        title += "...";
                }

                return title;
        }

        private String getRelativeTime(OffsetDateTime time) {
                Duration duration = Duration.between(time, OffsetDateTime.now());
                long hours = duration.toHours();
                long minutes = duration.toMinutes();

                if (hours > 24) {
                        return (hours / 24) + "天前";
                } else if (hours > 0) {
                        return hours + "小时前";
                } else if (minutes > 0) {
                        return minutes + "分钟前";
                } else {
                        return "刚刚";
                }
        }

        private String formatNumber(long num) {
                if (num >= 1000000) {
                        return String.format("%.1fM", num / 1000000.0);
                } else if (num >= 1000) {
                        return String.format("%.1fK", num / 1000.0);
                }
                return String.valueOf(num);
        }

        private String extractKeywords(List<Tweet> tweets) {
                // Simple keyword extraction - take common terms from first few tweets
                if (tweets.isEmpty())
                        return "无";

                StringBuilder keywords = new StringBuilder();
                int count = 0;
                for (Tweet t : tweets) {
                        if (count >= 3)
                                break;
                        String text = t.getText();

                        // Extract first meaningful phrase (very naive)
                        String[] words = text.split("[,，。\\s]+");
                        for (String word : words) {
                                if (word.length() >= 3 && word.length() <= 15) {
                                        if (keywords.length() > 0)
                                                keywords.append(", ");
                                        keywords.append(word);
                                        count++;
                                        break;
                                }
                        }
                }

                return keywords.length() > 0 ? keywords.toString() : "综合动态";
        }

        private String wrapText(String text, int maxWidth) {
                if (text.length() <= maxWidth)
                        return text;

                StringBuilder result = new StringBuilder();
                int currentLineLength = 0;

                for (char c : text.toCharArray()) {
                        result.append(c);
                        currentLineLength++;

                        if (c == '。' || c == '!' || c == '?') {
                                if (currentLineLength >= maxWidth * 0.7) {
                                        result.append("\n");
                                        currentLineLength = 0;
                                }
                        }
                }

                return result.toString();
        }
}
