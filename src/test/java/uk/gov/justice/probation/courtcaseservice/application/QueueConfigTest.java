package uk.gov.justice.probation.courtcaseservice.application;


import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import uk.gov.justice.hmpps.sqs.HmppsQueueService;
import uk.gov.justice.hmpps.sqs.HmppsTopic;
import software.amazon.awssdk.services.sns.SnsAsyncClient;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatExceptionOfType;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class QueueConfigTest {

    @Mock
    private HmppsQueueService hmppsQueueService;
    @Mock
    private SnsAsyncClient client;
    private HmppsTopic hmppsTopic;
    private final QueueConfig queueConfig = new QueueConfig();

    @BeforeEach
    public void before(){
        hmppsTopic = new HmppsTopic("foo", "foo", client);
    }

    @Test
    public void shouldReturnTopicIfAvailable() {
        when(hmppsQueueService.findByTopicId("hmppsdomainevents")).thenReturn(hmppsTopic);
        final var domainEventsTopic = queueConfig.hmppsDomainEventsTopic(hmppsQueueService);

        assertThat(domainEventsTopic).isEqualTo(hmppsTopic);
    }

    @Test
    public void shouldReturnThrowExceptionIfNoTopicFound() {
        when(hmppsQueueService.findByTopicId("hmppsdomainevents")).thenReturn(null);
        assertThatExceptionOfType(RuntimeException.class)
                .isThrownBy(() -> queueConfig.hmppsDomainEventsTopic(hmppsQueueService))
                        .withMessage("Fatal error: The hmppsdomainevents topic does not exist. The environment configuration may be faulty.");

    }

}
