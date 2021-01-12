package uk.gov.justice.probation.courtcaseservice.application;

import lombok.AllArgsConstructor;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.HandlerInterceptor;
import org.springframework.web.servlet.config.annotation.InterceptorRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

@AllArgsConstructor
@Configuration
public class InterceptorConfigurer implements WebMvcConfigurer {
    private HandlerInterceptor clientTrackingInterceptor;

    @Override
    public void addInterceptors(InterceptorRegistry registry) {
        registry.addInterceptor(clientTrackingInterceptor).addPathPatterns("/**");
    }
}
