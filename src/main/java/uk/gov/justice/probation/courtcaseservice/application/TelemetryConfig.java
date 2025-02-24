package uk.gov.justice.probation.courtcaseservice.application;

import com.microsoft.applicationinsights.TelemetryClient;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Condition;
import org.springframework.context.annotation.ConditionContext;
import org.springframework.context.annotation.Conditional;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.type.AnnotatedTypeMetadata;
import org.springframework.lang.NonNull;
import reactor.util.StringUtils;

@Configuration
public class TelemetryConfig {

    @Bean
    @Conditional(AppInsightKeyAbsentCondition.class)
    public TelemetryClient getTelemetryClient() {
        return new TelemetryClient();
    }

    private static class AppInsightKeyAbsentCondition implements Condition {
        @Override
        public boolean matches(@NonNull ConditionContext context, @NonNull AnnotatedTypeMetadata metadata) {
            return StringUtils.isEmpty(context.getEnvironment().getProperty("application.insights.ikey"));
        }
    }
}
