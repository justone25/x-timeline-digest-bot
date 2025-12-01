package life.ljs.digest.domain.policy;

import life.ljs.digest.domain.model.Tweet;

public interface AdDetectPolicy {
    boolean isAd(Tweet tweet);
}
