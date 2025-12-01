package life.ljs.digest.domain.policy;

import life.ljs.digest.domain.model.Tweet;

public class DefaultEngagementScorePolicy implements EngagementScorePolicy {

    @Override
    public double score(Tweet tweet) {
        return 0.5 * tweet.getLikeCount()
                + 1.2 * tweet.getRetweetCount()
                + 1.5 * tweet.getReplyCount();
    }
}
