package life.ljs.digest.app.scheduler;

import life.ljs.digest.domain.model.DigestBatch;
import life.ljs.digest.domain.model.Tweet;
import life.ljs.digest.domain.port.DiscordPort;
import life.ljs.digest.domain.service.DigestBuildService;
import life.ljs.digest.domain.service.FetchTweetService;
import life.ljs.digest.domain.service.FilterService;
import life.ljs.digest.domain.service.RankingService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import java.time.OffsetDateTime;
import java.util.List;

@Component
public class DigestScheduleTask {
    private static final Logger log = LoggerFactory.getLogger(DigestScheduleTask.class);

    private final FetchTweetService fetchTweetService;
    private final FilterService filterService;
    private final RankingService rankingService;
    private final DigestBuildService digestBuildService;
    private final DiscordPort discordPort;

    public DigestScheduleTask(FetchTweetService fetchTweetService,
                              FilterService filterService,
                              RankingService rankingService,
                              DigestBuildService digestBuildService,
                              DiscordPort discordPort) {
        this.fetchTweetService = fetchTweetService;
        this.filterService = filterService;
        this.rankingService = rankingService;
        this.digestBuildService = digestBuildService;
        this.discordPort = discordPort;
    }


    @Scheduled(cron = "0 0 0/2 * * ?")
    public void runDigest() {
        log.info("开始执行一次 Digest 任务 …");

        OffsetDateTime start = OffsetDateTime.now().minusMinutes(30);
        OffsetDateTime end = OffsetDateTime.now();

        // 1. 拉取新的 tweets
        List<Tweet> rawTweets = fetchTweetService.fetchNewTweets();
        log.info("从时间线拉取到 {} 条原始 tweets", rawTweets.size());

        // 2. 过滤垃圾 & 广告
        List<Tweet> filtered = filterService.filter(rawTweets);
        log.info("过滤后剩余 {} 条有效 tweets", filtered.size());

        if (filtered.isEmpty()) {
            log.info("本次没有可以用来生成 Digest 的内容，跳过。");
            return;
        }

        // 3. 排序选出 Top25
        List<Tweet> topTweets = rankingService.selectTopTweets(filtered);
        log.info("选出 Top {} 条用于 Digest", topTweets.size());

        // 4. 用 LLM 构建 DigestBatch
        DigestBatch batch = digestBuildService.buildDigest(topTweets, start, end);

        // 5. 推送到 Discord（现在先打印日志）
        discordPort.pushDigest(batch);

        log.info("本次 Digest 任务执行完毕。");
    }
}
