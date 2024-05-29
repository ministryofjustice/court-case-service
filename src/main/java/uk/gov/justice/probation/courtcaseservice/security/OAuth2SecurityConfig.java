package uk.gov.justice.probation.courtcaseservice.security;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Primary;
import org.springframework.context.annotation.Profile;
import org.springframework.core.annotation.Order;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.oauth2.jose.jws.SignatureAlgorithm;
import org.springframework.security.oauth2.jwt.JwtDecoder;
import org.springframework.security.oauth2.jwt.JwtDecoders;
import org.springframework.security.oauth2.jwt.NimbusJwtDecoder;
import org.springframework.security.web.SecurityFilterChain;
import uk.gov.justice.hmpps.kotlin.auth.AuthAwareTokenConverter;

import static org.springframework.security.oauth2.jose.jws.JwsAlgorithms.ES512;
import static org.springframework.security.oauth2.jose.jws.JwsAlgorithms.RS512;

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
                            "/process-un-resulted-cases"
                        ).permitAll()
                        .anyRequest().hasAnyRole("PREPARE_A_CASE", "SAR_DATA_ACCESS")
                    ).oauth2ResourceServer().jwt().jwtAuthenticationConverter(new AuthAwareTokenConverter());
        return http.build();
    }
}
