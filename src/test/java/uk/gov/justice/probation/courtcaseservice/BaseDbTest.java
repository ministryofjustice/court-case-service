package uk.gov.justice.probation.courtcaseservice;

import org.springframework.boot.test.autoconfigure.jdbc.AutoConfigureTestDatabase;
import org.springframework.context.ApplicationContextInitializer;
import org.springframework.context.ConfigurableApplicationContext;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.support.TestPropertySourceUtils;
import org.testcontainers.containers.PostgreSQLContainer;


@ActiveProfiles("test")
@AutoConfigureTestDatabase(replace= AutoConfigureTestDatabase.Replace.NONE)
@ContextConfiguration(initializers = BaseDbTest.DockerPostgreDataSourceInitializer.class)
public abstract class BaseDbTest {

    public static PostgreSQLContainer<?> postgresDBContainer = new PostgreSQLContainer<>("postgres:11.6")
            .withEnv("POSTGRES_USER", "root")
            .withEnv("POSTGRES_PASSWORD", "dev")
            .withExposedPorts(5432);

    static {
        postgresDBContainer.start();
    }

    public static class DockerPostgreDataSourceInitializer implements ApplicationContextInitializer<ConfigurableApplicationContext> {

        @Override
        public void initialize(ConfigurableApplicationContext applicationContext) {

            TestPropertySourceUtils.addInlinedPropertiesToEnvironment(
                    applicationContext,
                    "database.endpoint=" + postgresDBContainer.getHost() + ":" + postgresDBContainer.getFirstMappedPort(),
                    "database.username=" + postgresDBContainer.getUsername(),
                    "database.password=" + postgresDBContainer.getPassword()
            );
        }
    }
}
