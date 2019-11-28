//package uk.gov.justice.probation.courtcaseservice;
//
//import lombok.extern.slf4j.Slf4j;
//import lombok.extern.slf4j.XSlf4j;
//import org.slf4j.Logger;
//import org.slf4j.LoggerFactory;
//import org.springframework.boot.CommandLineRunner;
//import org.springframework.context.annotation.Profile;
//import org.springframework.stereotype.Component;
//import software.amazon.awssdk.services.dynamodb.DynamoDbClient;
//import software.amazon.awssdk.services.dynamodb.model.*;
//import uk.gov.justice.probation.courtcaseservice.config.DynamoDbConfiguration;
//
//@Component
//@Slf4j
//@Profile("localDynamo")
//public class CreateTableCommandLineRunner implements CommandLineRunner {
//
//    private final DynamoDbConfiguration config;
//
//    public CreateTableCommandLineRunner(DynamoDbConfiguration dynamoDbConfiguration) {
//        this.config = dynamoDbConfiguration;
//    }
//
//    @Override
//    public void run(String... args) throws Exception {
//
//        final DynamoDbClient dynamoDb = config.getClient();
//        final String tableName = config.getTableName();
//        try {
//
//            ListTablesResponse response = config.getClient().listTables(
//                    ListTablesRequest.builder().build());
//
//            if (response.tableNames().contains(tableName)) {
//                log.info("Found table [{}]", tableName);
//            } else {
//                log.info("Attempting to create table [{}]", tableName);
//                CreateTableResponse tableResponse = dynamoDb.createTable(createTableRequest());
//                log.info("Successfully created table [{}]", tableName);
//            }
//        } catch (Exception e) {
//            log.error("Unable to create table [{}]", tableName, e);
//            throw e;
//        }
//    }
//
//    private CreateTableRequest createTableRequest() {
//        return CreateTableRequest.builder()
//                .tableName(config.getTableName())
//                .attributeDefinitions(AttributeDefinition.builder()
//                        .attributeName("pk")
//                        .attributeType(ScalarAttributeType.S)
//                        .build())
//                .keySchema(KeySchemaElement.builder()
//                        .attributeName("pk")
//                        .keyType(KeyType.HASH)
//                        .build())
//                .provisionedThroughput(ProvisionedThroughput.builder()
//                        .readCapacityUnits(10L)
//                        .writeCapacityUnits(10L)
//                        .build())
//                .build();
//    }
//}
