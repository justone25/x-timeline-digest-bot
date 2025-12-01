package life.ljs.digest.domain.port;

/**
 * 记住lastSeenTweetId
 */
public interface StateRepositoryPort {
    /**
     * 读取上一次的lastSeenTweetId
     * 若没有记录，可以返回 null
     * @return
     */
    String loadLastSeenTweetId();

    /**
     * 更新lastSeenTweetId
     * @param tweetId
     */
    void saveLastSeenTweetId(String tweetId);
}
