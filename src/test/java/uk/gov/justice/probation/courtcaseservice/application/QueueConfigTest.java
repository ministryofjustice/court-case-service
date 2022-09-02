package uk.gov.justice.probation.courtcaseservice.application;

import com.amazonaws.services.sns.AmazonSNS;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import uk.gov.justice.hmpps.sqs.HmppsQueueService;
import uk.gov.justice.hmpps.sqs.HmppsTopic;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatExceptionOfType;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class QueueConfigTest {

    @Mock
    private HmppsQueueService hmppsQueueService;
    @Mock
    private AmazonSNS client;
    private HmppsTopic hmppsTopic;
    private final QueueConfig queueConfig = new QueueConfig();

    @BeforeEach
    public void before(){
        hmppsTopic = new HmppsTopic("foo", "foo", client);
    }

    @Test
    public void shouldReturnTopicIfAvailable() {
        when(hmppsQueueService.findByTopicId("hmpps-domain-events")).thenReturn(hmppsTopic);
        final var hmppsTopic = queueConfig.hmppsDomainEventsTopic(hmppsQueueService);

        assertThat(hmppsTopic).isEqualTo(hmppsTopic);
    }

    @Test
    public void shouldReturnThrowExceptionIfNoTopicFound() {
        when(hmppsQueueService.findByTopicId("hmpps-domain-events")).thenReturn(null);
        assertThatExceptionOfType(RuntimeException.class)
                .isThrownBy(() -> queueConfig.hmppsDomainEventsTopic(hmppsQueueService))
                        .withMessage("Fatal error: The hmpps-domain-events topic does not exist. The environment configuration may be faulty.");

        assertThat(hmppsTopic).isEqualTo(hmppsTopic);
    }

}
