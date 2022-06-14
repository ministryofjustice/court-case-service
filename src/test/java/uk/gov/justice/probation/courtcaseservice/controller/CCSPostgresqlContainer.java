package uk.gov.justice.probation.courtcaseservice.controller;

import org.testcontainers.containers.PostgreSQLContainer;

public class CCSPostgresqlContainer extends PostgreSQLContainer<CCSPostgresqlContainer> {
    private static final String IMAGE_VERSION = "postgres:11.1";
    private static CCSPostgresqlContainer container;

    private CCSPostgresqlContainer() {
        super(IMAGE_VERSION);
    }

    public static CCSPostgresqlContainer getInstance() {
        if (container == null) {
            container = new CCSPostgresqlContainer()
                .withExposedPorts(5432)
                .withDatabaseName("courtcaseservicetest")
                .withUsername("ituser")
                .withPassword("itpass");
        }
        return container;
    }
}