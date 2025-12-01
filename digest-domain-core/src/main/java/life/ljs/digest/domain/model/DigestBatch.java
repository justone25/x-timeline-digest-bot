package life.ljs.digest.domain.model;

import java.time.OffsetDateTime;
import java.util.List;

public class DigestBatch {

    private final OffsetDateTime startTime;
    private final OffsetDateTime endTime;

    private final List<Tweet> top3;
    private final List<TopicCluster> clusters;
    private final List<Tweet> allTweets;
    private final String overviewSummary;

    public DigestBatch(OffsetDateTime startTime,
                       OffsetDateTime endTime,
                       List<Tweet> top3,
                       List<TopicCluster> clusters,
                       List<Tweet> allTweets,
                       String overviewSummary) {
        this.startTime = startTime;
        this.endTime = endTime;
        this.top3 = top3;
        this.clusters = clusters;
        this.allTweets = allTweets;
        this.overviewSummary = overviewSummary;
    }

    public OffsetDateTime getStartTime() {
        return startTime;
    }

    public OffsetDateTime getEndTime() {
        return endTime;
    }

    public List<Tweet> getTop3() {
        return top3;
    }

    public List<TopicCluster> getClusters() {
        return clusters;
    }

    public List<Tweet> getAllTweets() {
        return allTweets;
    }

    public String getOverviewSummary() {
        return overviewSummary;
    }
}
