package uk.gov.justice.digital.probation.court.list.courtlistservice;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.security.oauth2.config.annotation.web.configuration.EnableResourceServer;

@SpringBootApplication
@EnableResourceServer
public class CourtListServiceApplication {

    public static void main(String[] args) {
        SpringApplication.run(CourtListServiceApplication.class, args);
    }

}
