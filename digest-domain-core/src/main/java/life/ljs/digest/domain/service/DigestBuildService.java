package life.ljs.digest.domain.service;

import life.ljs.digest.domain.model.DigestBatch;
import life.ljs.digest.domain.model.TopicCluster;
import life.ljs.digest.domain.model.Tweet;
import life.ljs.digest.domain.port.LlmPort;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.time.OffsetDateTime;
import java.util.stream.Collectors;

public class DigestBuildService {
    private final LlmPort llmPort;

    public DigestBuildService(LlmPort llmPort) {
        this.llmPort = llmPort;
    }

    public DigestBatch buildDigest(List<Tweet> topTweets,
                                   OffsetDateTime windowStart,
                                   OffsetDateTime windowEnd){
        if(topTweets == null){
            topTweets = new ArrayList<>();
        }
        List<Tweet> tweetsWithSummary = topTweets.stream()
                .map(this::appendSummaryAsSideEffect)
                .collect(Collectors.toList());
        List<TopicCluster> clusters = llmPort.clusterTweets(tweetsWithSummary);
        List<Tweet> top3 = tweetsWithSummary.stream()
                .sorted(Comparator.comparingLong(Tweet::engagementScore).reversed())
                .limit(3)
                .collect(Collectors.toList());

        String overview = llmPort.summarizeBatch(clusters, top3);
        return new DigestBatch(
                windowStart,
                windowEnd,
                top3,
                clusters,
                tweetsWithSummary,
                overview
        );
    }

    private Tweet appendSummaryAsSideEffect(Tweet tweet) {
        llmPort.summarizeTweet(tweet);
        return tweet;
    }
}
