package life.ljs.digest.domain.policy;

import life.ljs.digest.domain.model.Tweet;

/**
 * 互动量评分策略
 */
public interface EngagementScorePolicy {
    /**
     * 计算tweet的排序评分，用于Top25
     * @param tweet
     * @return
     */
    double score(Tweet tweet);
}
