package life.ljs.digest.domain.policy;

import life.ljs.digest.domain.model.Tweet;

public interface SpamDetectPolicy {
    /**
     * 是否判断为垃圾内容（无意义、重复、明显bot等）
     * @param tweet
     * @return
     */
    boolean isSpam(Tweet tweet);
}
