package life.ljs.digest.app.adapter.fake;

import life.ljs.digest.domain.model.Tweet;
import life.ljs.digest.domain.port.TimelinePort;
import org.springframework.context.annotation.Profile;
import org.springframework.stereotype.Component;

import java.time.OffsetDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

@Component
@Profile("fake")
public class FakeTimelineAdapter implements TimelinePort {
    @Override
    public List<Tweet> fetchLatestTweets(String lastSeenTweetId) {
        List<Tweet> tweets = new ArrayList<>();

        tweets.add(new Tweet(
                UUID.randomUUID().toString(),
                "vitalik",
                "Soma thoughts on rollup decentralization and L2 security",
                OffsetDateTime.now().minusMinutes(5),
                1200, 300, 210,
                "https://x.com/vitalik/status/xxx1",
                "en"
        ));

        tweets.add(new Tweet(
                UUID.randomUUID().toString(),
               "ai_researcher",
               "New paper: Efficient long-context transformers with sparse routing.",
               OffsetDateTime.now().minusMinutes(10),
               800, 150, 90,
               "https://x.com/ai_researcher/status/xxx2",
               "en"
        ));
        tweets.add(new Tweet(
                UUID.randomUUID().toString(),
                "random_shiller",
                "BUY NOW!!! 1000x gem, limited offer, insane discount!!!",
                OffsetDateTime.now().minusMinutes(1),
                2, 1, 0,
                "https://x.com/shiller/status/xxx3",
                "en"
        ));
        return tweets;
    }
}
