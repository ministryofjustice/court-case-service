package uk.gov.justice.probation.courtcaseservice.security;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Profile;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.web.SecurityFilterChain;

@Configuration
@EnableWebSecurity
@Profile("!unsecured")
public class OAuth2SecurityConfig {

    @Value("${spring.security.oauth2.resourceserver.jwt.issuer-uri}")
    private String issuer;

    @SuppressWarnings("removal")
    @Bean
    public SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {
        http
                // Can't have CSRF protection as requires session
                .csrf().disable()
                .sessionManagement()
                    .sessionCreationPolicy(SessionCreationPolicy.STATELESS)
                .and()
                    .oauth2Client()
                .and()
                    .authorizeHttpRequests(auth -> auth
                        .requestMatchers(
                            "/health/**",
                            "/info",
                            "/ping",
                            "/swagger-ui.html",
                            "/swagger-ui/**",
                            "/v3/api-docs/**",
                            "/queue-admin/retry-all-dlqs",
                            "/process-un-resulted-cases",
                            "/hearing/delete-duplicates"
                        ).permitAll()
                        .anyRequest().hasAnyRole("PREPARE_A_CASE", "SAR_DATA_ACCESS")
                    ).oauth2ResourceServer().jwt().jwtAuthenticationConverter(new AuthAwareTokenConverter());
        return http.build();
    }
}
