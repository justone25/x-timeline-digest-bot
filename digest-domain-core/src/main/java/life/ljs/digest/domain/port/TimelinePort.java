package life.ljs.digest.domain.port;

import life.ljs.digest.domain.model.Tweet;

import java.util.List;

/**
 * 领域层对外发出的需求：时间线上的最新tweets
 */
public interface TimelinePort {
    /**
     * 拉取时间线上的最新tweets
     * @param lastSeenTweetId 可为空
     *                        - 为空：初次拉取
     *                        - 不为空：只返回比它更新的tweet
     * @return
     */
    List<Tweet> fetchLatestTweets(String lastSeenTweetId);
}
