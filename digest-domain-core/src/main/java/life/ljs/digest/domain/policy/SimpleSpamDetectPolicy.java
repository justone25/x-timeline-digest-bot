package life.ljs.digest.domain.policy;

import life.ljs.digest.domain.model.Tweet;

public class SimpleSpamDetectPolicy implements SpamDetectPolicy {

    @Override
    public boolean isSpam(Tweet tweet) {
        String text = tweet.getText();
        if (text == null || text.isBlank()) {
            return true;
        }

        String cleaned = text.replaceAll("[#@\\s]","");
        if(cleaned.isBlank()){
            return true;
        }

        if(text.length() < 5){
            return true;
        }

        return false;
    }
}
