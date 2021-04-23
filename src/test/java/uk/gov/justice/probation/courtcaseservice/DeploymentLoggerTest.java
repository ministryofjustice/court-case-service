package uk.gov.justice.probation.courtcaseservice;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.boot.info.BuildProperties;

import java.net.UnknownHostException;

import static org.assertj.core.api.AssertionsForClassTypes.assertThat;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class DeploymentLoggerTest {

    @Mock
    private BuildProperties buildProperties;

    @Test
    public void shouldLogInfo() throws UnknownHostException {
        when(buildProperties.getVersion()).thenReturn("expected_version");
        final var logger = new DeploymentLogger(buildProperties);

        logger.onApplicationEvent(null);
        assertThat(TestAppender.events.size()).isEqualTo(1);
        assertThat(TestAppender.events.get(0).toString()).startsWith("[INFO] Starting CourtCaseServiceApplication expected_version using Java ");
    }

}
