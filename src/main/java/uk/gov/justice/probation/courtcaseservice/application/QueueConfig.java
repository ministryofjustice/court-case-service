package uk.gov.justice.probation.courtcaseservice.application;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import uk.gov.justice.hmpps.sqs.HmppsQueueService;
import uk.gov.justice.hmpps.sqs.HmppsTopic;

import java.util.Optional;

@Configuration
public class QueueConfig {
//    @Bean
//    public HmppsTopic hmppsDomainEventsTopic(HmppsQueueService hmppsQueueService) {
//        return Optional.ofNullable(hmppsQueueService.findByTopicId("hmppsdomainevents"))
//                .orElseThrow(() -> new RuntimeException("Fatal error: The hmppsdomainevents topic does not exist. The environment configuration may be faulty."));
//    }
}
