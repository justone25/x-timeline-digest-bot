package life.ljs.digest.domain.service;

import life.ljs.digest.domain.model.Tweet;
import life.ljs.digest.domain.policy.EngagementScorePolicy;

import java.util.Comparator;
import java.util.List;
import java.util.stream.Collectors;

public class RankingService {
    private final EngagementScorePolicy scorePolicy;
    private final int topN;
    public RankingService(EngagementScorePolicy scorePolicy, int topN) {
        this.scorePolicy = scorePolicy;
        this.topN = topN;
    }

    public List<Tweet> selectTopTweets(List<Tweet> tweets) {
        return tweets.stream()
                .sorted(Comparator.comparingDouble(scorePolicy::score).reversed())
                .limit(topN)
                .collect(Collectors.toList());
    }
}
