package uk.gov.justice.probation.courtcaseservice.application;

import com.microsoft.applicationinsights.TelemetryClient;
import com.microsoft.applicationinsights.telemetry.BaseTelemetry;
import com.microsoft.applicationinsights.web.internal.RequestTelemetryContext;
import com.microsoft.applicationinsights.web.internal.ThreadContext;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Condition;
import org.springframework.context.annotation.ConditionContext;
import org.springframework.context.annotation.Conditional;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Profile;
import org.springframework.core.type.AnnotatedTypeMetadata;
import org.springframework.lang.NonNull;
import org.springframework.web.context.annotation.RequestScope;
import reactor.util.StringUtils;

import java.util.Collections;
import java.util.Map;
import java.util.Optional;

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

    @Bean
    @Profile("!test")
    @RequestScope
    public Map<String, String> requestProperties(){
        return Optional.ofNullable(ThreadContext.getRequestTelemetryContext())
                .map(RequestTelemetryContext::getHttpRequestTelemetry)
                .map(BaseTelemetry::getProperties)
                .orElse(Collections.emptyMap());
    }
}
