package uk.gov.justice.probation.courtcaseservice;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Profile;

import java.util.Map;

@Configuration
public class TestTelemetryConfig {

    @Bean
    @Profile("test")
    public Map<String, String> requestProperties(){
        return Map.of(
                "username", "Test User",
                "clientId", "test-client-id"
        );
    }
}
