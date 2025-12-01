package life.ljs.digest.domain.port;

import life.ljs.digest.domain.model.TopicCluster;
import life.ljs.digest.domain.model.Tweet;

import java.util.List;

/**
 * 领域层对外发出的需求：大模型生成摘要、聚类、总览
 */
public interface LlmPort {
    /**
     * 为单条tweet生成一句话摘要
     * @param tweet
     * @return
     */
    String summarizeTweet(Tweet tweet);

    /**
     * 为一组tweet生成主题聚类结果
     * @param tweets
     * @return
     */
    List<TopicCluster> clusterTweets(List<Tweet> tweets);

    /**
     * 为整个批次生成一段总结概览（150字左右）
     * @param clusters
     * @param top3
     * @return
     */
    String summarizeBatch(List<TopicCluster> clusters, List<Tweet> top3);

}
