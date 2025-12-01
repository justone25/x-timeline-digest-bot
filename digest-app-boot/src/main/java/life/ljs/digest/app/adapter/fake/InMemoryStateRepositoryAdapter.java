package life.ljs.digest.app.adapter.fake;

import life.ljs.digest.domain.port.StateRepositoryPort;
import org.springframework.stereotype.Component;

@Component
public class InMemoryStateRepositoryAdapter implements StateRepositoryPort {
    private String lastSeenTwitterId;

    @Override
    public String loadLastSeenTweetId() {
        return lastSeenTwitterId;
    }

    @Override
    public void saveLastSeenTweetId(String tweetId) {
        this.lastSeenTwitterId = tweetId;
    }
}
