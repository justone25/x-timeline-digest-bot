package life.ljs.digest.domain.policy;

import life.ljs.digest.domain.model.Tweet;

public class SimpleAdDetectPolicy implements AdDetectPolicy {
    @Override
    public boolean isAd(Tweet tweet) {
        String text = tweet.getText().toLowerCase();
        String [] adKeywords = {
                "buy now", "discount", "sale", "limited offer",
                "promo code", "sponsored"
        };
        for (String keyword : adKeywords) {
            if (text.contains(keyword)) {
                return true;
            }
        }
        //TODO 根据Author白名单/黑名单进一步判断
        return false;
    }
}
