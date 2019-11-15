package uk.gov.justice.probation.courtcaseservice.prototype.service;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.web.client.RestTemplateBuilder;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.client.ClientHttpRequestInterceptor;
import org.springframework.web.client.RestTemplate;
import uk.gov.justice.probation.courtcaseservice.prototype.utils.JwtAuthInterceptor;

import java.util.List;

@Configuration
@Slf4j
public class RestTemplateConfiguration {

    private final String apiRootUri;

    @Autowired
    public RestTemplateConfiguration(
            @Value("${crime.portal.base.url}") final String apiRootUri) {
        log.info("Crime portal API at {}", apiRootUri);
        this.apiRootUri = apiRootUri;

    }

    @Bean
    public RestTemplate restTemplate(final RestTemplateBuilder restTemplateBuilder) {
        return getRestTemplate(restTemplateBuilder, apiRootUri);
    }

    private RestTemplate getRestTemplate(final RestTemplateBuilder restTemplateBuilder, final String uri) {
        return restTemplateBuilder
                .rootUri(uri)
                .additionalInterceptors(getRequestInterceptors())
                .build();
    }

    private List<ClientHttpRequestInterceptor> getRequestInterceptors() {
        return List.of(
                new JwtAuthInterceptor());
    }

}