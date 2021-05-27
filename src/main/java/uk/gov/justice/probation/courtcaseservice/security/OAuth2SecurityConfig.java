package uk.gov.justice.probation.courtcaseservice.security;

import org.springframework.context.annotation.Profile;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configuration.WebSecurityConfigurerAdapter;
import org.springframework.security.config.http.SessionCreationPolicy;

@EnableWebSecurity
@Profile("!unsecured")
public class OAuth2SecurityConfig extends WebSecurityConfigurerAdapter {

    @Override
    protected void configure(HttpSecurity http) throws Exception {
        http
                // Can't have CSRF protection as requires session
                .csrf().disable()
                .sessionManagement()
                    .sessionCreationPolicy(SessionCreationPolicy.STATELESS)

                .and().oauth2Client()


                .and().authorizeRequests(auth ->
                    auth
                        .mvcMatchers(
                                "/health",
                                "/ping",
                                "/swagger-resources/**",
                                "/v2/api-docs",
                                "/swagger-ui.html",
                                "/swagger-ui/**",
                                "/webjars/springfox-swagger-ui/**"
                        ).permitAll()
                        .anyRequest().hasRole("PREPARE_A_CASE")
                ).oauth2ResourceServer().jwt().jwtAuthenticationConverter(new AuthAwareTokenConverter());
    }
}
