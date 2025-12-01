package life.ljs.digest.domain.model;

import java.util.List;

/**
 * 主题组
 */
public class TopicCluster {
    private final String topicName;
    private final List<Tweet> tweets;

    public TopicCluster(String topicName, List<Tweet> tweets) {
        this.topicName = topicName;
        this.tweets = tweets;
    }

    public String getTopicName() {
        return topicName;
    }

    public List<Tweet> getTweets() {
        return tweets;
    }

    public int size() {
        return tweets.size();
    }
}
