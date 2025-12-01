package life.ljs.digest.domain.service;

import life.ljs.digest.domain.model.Tweet;
import life.ljs.digest.domain.port.StateRepositoryPort;
import life.ljs.digest.domain.port.TimelinePort;

import java.util.List;

public class FetchTweetService {

    private final TimelinePort timelinePort;
    private final StateRepositoryPort stateRepositoryPort;

    public FetchTweetService(TimelinePort timelinePort, StateRepositoryPort stateRepositoryPort) {
        this.timelinePort = timelinePort;
        this.stateRepositoryPort = stateRepositoryPort;
    }

    public List<Tweet> fetchNewTweets() {
        String lastSeenTweetId = stateRepositoryPort.loadLastSeenTweetId();
        List<Tweet> tweets = timelinePort.fetchLatestTweets(lastSeenTweetId);

        //更新lastSeenTweetId:取本次返回中最新一条
        tweets.stream()
                .map(Tweet::getTweetId)
                .max(String::compareTo)
                .ifPresent(stateRepositoryPort::saveLastSeenTweetId);
        return tweets;
    }
}
