package life.ljs.digest.domain.model;

import java.time.OffsetDateTime;
import java.util.Objects;

/**
 * 时间线上的一条内容
 */
public class Tweet {

    private final String tweetId;
    private final String author;
    private final String text;
    private final OffsetDateTime createdAt;

    private final long likeCount;
    private final long retweetCount;
    private final long replyCount;

    private final String url;
    private final String language;

    // 业务分析标签
    private boolean isAd;
    private boolean isSpam;
    private String summary; // LLM生成的摘要

    public String getTweetId() {
        return tweetId;
    }

    public String getAuthor() {
        return author;
    }

    public String getText() {
        return text;
    }

    public OffsetDateTime getCreatedAt() {
        return createdAt;
    }

    public long getLikeCount() {
        return likeCount;
    }

    public long getRetweetCount() {
        return retweetCount;
    }

    public long getReplyCount() {
        return replyCount;
    }

    public String getUrl() {
        return url;
    }

    public String getLanguage() {
        return language;
    }

    public boolean isAd() {
        return isAd;
    }

    public boolean isSpam() {
        return isSpam;
    }

    public String getSummary() {
        return summary;
    }

    public void setSummary(String summary) {
        this.summary = summary;
    }

    public Tweet(String tweetId,
            String author,
            String text,
            OffsetDateTime createdAt,
            long likeCount,
            long retweetCount,
            long replyCount,
            String url,
            String language) {
        this.tweetId = tweetId;
        this.author = author;
        this.text = text;
        this.createdAt = createdAt;
        this.likeCount = likeCount;
        this.retweetCount = retweetCount;
        this.replyCount = replyCount;
        this.url = url;
        this.language = language;
    }

    public void markAsAd() {
        this.isAd = true;
    }

    public void markAsSpam() {
        this.isSpam = true;
    }

    public long engagementScore() {
        return (long) (0.5 * likeCount +
                1.2 * retweetCount +
                1.5 * replyCount);
    }

    @Override
    public boolean equals(Object o) {
        if (this == o)
            return true;
        if (o == null || getClass() != o.getClass())
            return false;
        Tweet tweet = (Tweet) o;
        return Objects.equals(tweetId, tweet.tweetId);
    }

    @Override
    public int hashCode() {
        return Objects.hashCode(tweetId);
    }
}
