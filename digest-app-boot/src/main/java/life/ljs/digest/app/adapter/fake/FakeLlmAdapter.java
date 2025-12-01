package life.ljs.digest.app.adapter.fake;

import life.ljs.digest.domain.model.TopicCluster;
import life.ljs.digest.domain.model.Tweet;
import life.ljs.digest.domain.port.LlmPort;
import org.springframework.context.annotation.Profile;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.List;

@Component
@Profile("fake")
public class FakeLlmAdapter implements LlmPort {

    @Override
    public String summarizeTweet(Tweet tweet) {
        String text = tweet.getText();

        // Extract key insight
        if (text.length() > 100) {
            // For longer tweets, extract first meaningful sentence
            int firstPeriod = text.indexOf('。');
            if (firstPeriod > 0 && firstPeriod < 80) {
                return text.substring(0, firstPeriod + 1);
            }
            return text.substring(0, 80) + "...";
        }
        return text;
    }

    @Override
    public List<TopicCluster> clusterTweets(List<Tweet> tweets) {
        List<Tweet> aiTech = new ArrayList<>();
        List<Tweet> businessGrowth = new ArrayList<>();
        List<Tweet> crypto = new ArrayList<>();
        List<Tweet> tutorial = new ArrayList<>();
        List<Tweet> other = new ArrayList<>();

        for (Tweet t : tweets) {
            String lower = t.getText().toLowerCase();

            if (containsAny(lower, "ai", "llm", "gpt", "gemini", "claude", "模型", "prompt", "提示词")) {
                aiTech.add(t);
            } else if (containsAny(lower, "创业", "增长", "粉丝", "business", "startup", "growth", "用户")) {
                businessGrowth.add(t);
            } else if (containsAny(lower, "crypto", "eth", "btc", "rollup", "l2", "defi", "web3")) {
                crypto.add(t);
            } else if (containsAny(lower, "教程", "tutorial", "如何", "how to", "指南", "guide")) {
                tutorial.add(t);
            } else {
                other.add(t);
            }
        }

        List<TopicCluster> clusters = new ArrayList<>();
        if (!aiTech.isEmpty()) {
            clusters.add(new TopicCluster("🤖 AI与工具", aiTech));
        }
        if (!businessGrowth.isEmpty()) {
            clusters.add(new TopicCluster("💼 创业与增长", businessGrowth));
        }
        if (!crypto.isEmpty()) {
            clusters.add(new TopicCluster("🪙 Crypto与Web3", crypto));
        }
        if (!tutorial.isEmpty()) {
            clusters.add(new TopicCluster("📖 教程与指南", tutorial));
        }
        if (!other.isEmpty()) {
            clusters.add(new TopicCluster("🎯 其他动态", other));
        }

        return clusters;
    }

    @Override
    public String summarizeBatch(List<TopicCluster> clusters, List<Tweet> top3) {
        if (clusters.isEmpty()) {
            return "本时段暂无明显趋势,内容较为分散。";
        }

        StringBuilder sb = new StringBuilder();

        // Main themes
        if (clusters.size() == 1) {
            sb.append("本时段内容高度聚焦于「").append(stripEmoji(clusters.get(0).getTopicName()))
                    .append("」话题");
        } else {
            sb.append("本时段内容聚焦于「").append(stripEmoji(clusters.get(0).getTopicName()))
                    .append("」和「").append(stripEmoji(clusters.get(1).getTopicName()))
                    .append("」两大主题");
        }

        // Engagement insight
        if (!top3.isEmpty()) {
            long totalEngagement = top3.stream()
                    .mapToLong(t -> t.getLikeCount() + t.getRetweetCount() + t.getReplyCount())
                    .sum();

            if (totalEngagement > 5000) {
                sb.append(",整体互动热度较高");
            }
        }

        sb.append("。");

        // Topic-specific insights
        if (!clusters.isEmpty()) {
            TopicCluster topCluster = clusters.get(0);
            String topicName = stripEmoji(topCluster.getTopicName());

            if (topicName.contains("AI")) {
                sb.append("AI工具和应用案例成为热点话题,建议关注相关实践经验分享。");
            } else if (topicName.contains("创业") || topicName.contains("增长")) {
                sb.append("创作者们积极分享增长方法论和数据复盘,值得学习借鉴。");
            } else if (topicName.contains("Crypto")) {
                sb.append("加密货币和Web3领域动态活跃,技术进展值得关注。");
            }
        }

        return sb.toString();
    }

    private boolean containsAny(String text, String... keywords) {
        for (String keyword : keywords) {
            if (text.contains(keyword)) {
                return true;
            }
        }
        return false;
    }

    private String stripEmoji(String text) {
        return text.replaceAll("[\\p{So}\\p{Sk}]", "").trim();
    }
}
