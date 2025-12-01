package life.ljs.digest.domain.service;

import life.ljs.digest.domain.model.Tweet;
import life.ljs.digest.domain.policy.AdDetectPolicy;
import life.ljs.digest.domain.policy.SpamDetectPolicy;

import java.util.List;
import java.util.stream.Collectors;

public class FilterService {

    private final SpamDetectPolicy spamDetectPolicy;
    private final AdDetectPolicy adDetectPolicy;

    // 简单语言过滤白名单
    private final List<String> allowedLanguages;

    public FilterService(SpamDetectPolicy spamDetectPolicy, AdDetectPolicy adDetectPolicy, List<String> allowedLanguages) {
        this.spamDetectPolicy = spamDetectPolicy;
        this.adDetectPolicy = adDetectPolicy;
        this.allowedLanguages = allowedLanguages;
    }

    public List<Tweet> filter(List<Tweet> tweets) {
        return tweets.stream()
                .filter(this::acceptLanguage)
                .filter(this::notSpam)
                .filter(this::notAd)
                .collect(Collectors.toList());
    }

    private boolean acceptLanguage(Tweet tweet) {
        if(allowedLanguages == null || allowedLanguages.isEmpty()) {
            return true;
        }
        String lang = tweet.getLanguage();
        if(lang == null) {
            return true;
        }
        String lower = lang.toLowerCase();
        return allowedLanguages.contains(lower);
    }

    private boolean notSpam(Tweet tweet) {
        boolean spam = spamDetectPolicy.isSpam(tweet);
        if (spam) {
            tweet.markAsSpam();
        }
        return !spam;
    }
    private boolean notAd(Tweet tweet) {
        boolean ad = adDetectPolicy.isAd(tweet);
        if (ad) {
            tweet.markAsAd();
        }
        return !ad;
    }
}
