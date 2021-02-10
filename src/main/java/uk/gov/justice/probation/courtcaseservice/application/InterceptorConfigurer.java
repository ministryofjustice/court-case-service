package uk.gov.justice.probation.courtcaseservice.application;

import java.util.List;
import lombok.AllArgsConstructor;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.method.support.HandlerMethodArgumentResolver;
import org.springframework.web.servlet.HandlerInterceptor;
import org.springframework.web.servlet.config.annotation.InterceptorRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;
import uk.gov.justice.probation.courtcaseservice.controller.UpperCaseResolver;

@AllArgsConstructor
@Configuration
public class InterceptorConfigurer implements WebMvcConfigurer {
    private final HandlerInterceptor clientTrackingInterceptor;

    @Override
    public void addInterceptors(InterceptorRegistry registry) {
        registry.addInterceptor(clientTrackingInterceptor).addPathPatterns("/**");
    }

    public void addArgumentResolvers(List<HandlerMethodArgumentResolver> argumentResolvers) {
        argumentResolvers.add(new UpperCaseResolver());
    }
}
