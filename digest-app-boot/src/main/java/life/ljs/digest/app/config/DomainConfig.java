package life.ljs.digest.app.config;

import life.ljs.digest.domain.policy.*;
import life.ljs.digest.domain.port.LlmPort;
import life.ljs.digest.domain.port.StateRepositoryPort;
import life.ljs.digest.domain.port.TimelinePort;
import life.ljs.digest.domain.service.DigestBuildService;
import life.ljs.digest.domain.service.FetchTweetService;
import life.ljs.digest.domain.service.FilterService;
import life.ljs.digest.domain.service.RankingService;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.util.List;


@Configuration
public class DomainConfig {
    // ===== Policy 层Bean ======

    @Bean
    public EngagementScorePolicy engagementScorePolicy() {
        return new DefaultEngagementScorePolicy();
    }

    @Bean
    public SpamDetectPolicy spamDetectPolicy() {
        return new SimpleSpamDetectPolicy();
    }

    @Bean
    public AdDetectPolicy adDetectPolicy() {
        return new SimpleAdDetectPolicy();
    }

    // ===== Domain Service 层 Bean =====
    @Bean
    public FetchTweetService fetchTweetService(TimelinePort timelinePort,
                                               StateRepositoryPort stateRepositoryPort) {
        return new FetchTweetService(timelinePort, stateRepositoryPort);
    }
    @Bean
    public FilterService filterService(SpamDetectPolicy spamDetectPolicy,
                                       AdDetectPolicy adDetectPolicy) {
        return new FilterService(spamDetectPolicy, adDetectPolicy, List.of("en", "zh"));
    }
    @Bean
    public RankingService rankingService(EngagementScorePolicy engagementScorePolicy) {
        return new RankingService(engagementScorePolicy, 25);
    }
    @Bean
    public DigestBuildService digestBuildService(LlmPort llmPort) {
        return new DigestBuildService(llmPort);
    }
}
