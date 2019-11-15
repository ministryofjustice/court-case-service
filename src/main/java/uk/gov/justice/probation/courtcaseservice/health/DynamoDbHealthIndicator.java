package uk.gov.justice.probation.courtcaseservice.health;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.actuate.health.Health;
import org.springframework.boot.actuate.health.HealthIndicator;
import org.springframework.stereotype.Component;
import software.amazon.awssdk.services.dynamodb.model.DescribeTableRequest;
import software.amazon.awssdk.services.dynamodb.model.TableDescription;
import uk.gov.justice.probation.courtcaseservice.config.DynamoDbConfiguration;

@Component
public class DynamoDbHealthIndicator implements HealthIndicator {

    private final DynamoDbConfiguration config;

    @Autowired
    public DynamoDbHealthIndicator(DynamoDbConfiguration dynamoDbConfiguration) {
        this.config = dynamoDbConfiguration;
    }

    @Override
    public Health health() {
        final String tableName = config.getTableName();
        try {

            final TableDescription tableInfo = config.getClient().describeTable(
                    DescribeTableRequest.builder()
                            .tableName(tableName)
                            .build()).table();

            return Health.up()
                    .withDetail("table", tableName)
                    .withDetail("status", tableInfo.tableStatus())
                    .withDetail("itemCount", tableInfo.itemCount())
                    .withDetail("sizeBytesSystem", tableInfo.tableSizeBytes())
                    .build();

        } catch (Exception e) {
            return Health.down().withDetail("Error Message", e.getMessage()).build();
        }
    }
}
