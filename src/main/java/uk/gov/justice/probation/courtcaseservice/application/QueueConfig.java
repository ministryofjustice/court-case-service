package uk.gov.justice.probation.courtcaseservice.application;

import com.amazonaws.auth.AWSStaticCredentialsProvider;
import com.amazonaws.auth.BasicAWSCredentials;
import com.amazonaws.client.builder.AwsClientBuilder;
import com.amazonaws.services.sqs.AmazonSQSAsync;
import com.amazonaws.services.sqs.AmazonSQSAsyncClientBuilder;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Primary;
import uk.gov.justice.hmpps.sqs.HmppsQueueService;
import uk.gov.justice.hmpps.sqs.HmppsTopic;

import java.util.Optional;

@Configuration
public class QueueConfig {
    @Bean
    public HmppsTopic hmppsDomainEventsTopic(HmppsQueueService hmppsQueueService) {
        return Optional.ofNullable(hmppsQueueService.findByTopicId("hmppsdomainevents"))
                .orElseThrow(() -> new RuntimeException("Fatal error: The hmppsdomainevents topic does not exist. The environment configuration may be faulty."));
    }

    @Primary
    @Bean(name = "probationOffenderEventsQueue")
    public AmazonSQSAsync probationOffenderEventsQueue(@Value("${hmpps.sqs.region-name}") final String regionName,
                                                   @Value("${hmpps.sqs.queues.picprobationoffendereventsqueue.queue_endpoint_url}") final String awsEndpointUrl,
                                                   @Value("${hmpps.sqs.queues.picprobationoffendereventsqueue.queue_access_key_id}") final String awsAccessKeyId,
                                                   @Value("${hmpps.sqs.queues.picprobationoffendereventsqueue.queue_secret_access_key}") final String awsSecretAccessKey) {
        return getAmazonSQSAsync(awsAccessKeyId, awsSecretAccessKey, awsEndpointUrl, regionName);
    }

    @Bean(name = "probationOffenderEventsDlq")
    public AmazonSQSAsync probationOffenderEventsDlq(@Value("${hmpps.sqs.region-name}") final String regionName,
                                                 @Value("${hmpps.sqs.queues.picprobationoffendereventsqueue.dlq_endpoint_url}") final String awsEndpointUrl,
                                                 @Value("${hmpps.sqs.queues.picprobationoffendereventsqueue.dlq_access_key_id}") final String awsAccessKeyId,
                                                 @Value("${hmpps.sqs.queues.picprobationoffendereventsqueue.dlq_secret_access_key}") final String awsSecretAccessKey) {
        return getAmazonSQSAsync(awsAccessKeyId, awsSecretAccessKey, awsEndpointUrl, regionName);
    }

    private AmazonSQSAsync getAmazonSQSAsync(String awsAccessKeyId, String awsSecretAccessKey, String awsEndpointUrl, String regionName) {
        return AmazonSQSAsyncClientBuilder
                .standard()
                .withCredentials(new AWSStaticCredentialsProvider(new BasicAWSCredentials(awsAccessKeyId, awsSecretAccessKey)))
                .withEndpointConfiguration(new AwsClientBuilder.EndpointConfiguration(awsEndpointUrl, regionName))
                .build();
    }
}
