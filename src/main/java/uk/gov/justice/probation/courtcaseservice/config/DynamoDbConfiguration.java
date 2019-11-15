package uk.gov.justice.probation.courtcaseservice.config;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import software.amazon.awssdk.auth.credentials.AwsBasicCredentials;
import software.amazon.awssdk.auth.credentials.StaticCredentialsProvider;
import software.amazon.awssdk.regions.Region;
import software.amazon.awssdk.services.dynamodb.DynamoDbClient;
import software.amazon.awssdk.services.dynamodb.DynamoDbClientBuilder;

import java.net.URI;

@Configuration
@Slf4j
public class DynamoDbConfiguration {

    private final String region;
    private final String endpoint;
    private final String tableName;
    private final String accessKey;
    private final String secretKey;

    @Autowired
    public DynamoDbConfiguration(
            @Value("${amazon.dynamodb.region}") String region,
            @Value("${amazon.dynamodb.endpoint:#{null}}") String endpoint,
            @Value("${amazon.dynamodb.table}") String tableName,
            @Value("${amazon.dynamodb.accesskey}") String accessKey,
            @Value("${amazon.dynamodb.secretkey}") String secretKey) {

        this.region = region;
        this.endpoint = endpoint;
        this.tableName = tableName;
        this.accessKey = accessKey;
        this.secretKey = secretKey;

        log.info("DynamoDB region = {}", region);
        log.info("DynamoDB table = {}", tableName);
    }

    @Bean
    public DynamoDbClient getClient() {
        return dynamoDbClientBuilder()
                .region(Region.of(region))
                .credentialsProvider(StaticCredentialsProvider.create(AwsBasicCredentials.create(accessKey, secretKey)))
                .build();
    }

    public String getTableName() {
        return tableName;
    }

    private DynamoDbClientBuilder dynamoDbClientBuilder() {
        DynamoDbClientBuilder builder = DynamoDbClient.builder();
        if (endpoint != null) {
            log.info("DynamoDB overriding endpoint to {}", endpoint);
            builder.endpointOverride(URI.create(endpoint));
        }
        return builder;
    }
}
