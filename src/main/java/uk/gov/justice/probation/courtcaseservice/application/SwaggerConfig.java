package uk.gov.justice.probation.courtcaseservice.application;

import static springfox.documentation.builders.PathSelectors.regex;

import com.google.common.base.Predicates;
import java.time.LocalDateTime;
import java.time.ZonedDateTime;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Optional;
import java.util.Properties;
import org.springframework.beans.BeansException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.info.BuildProperties;
import org.springframework.context.ApplicationContext;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import springfox.documentation.builders.RequestHandlerSelectors;
import springfox.documentation.service.ApiInfo;
import springfox.documentation.service.Contact;
import springfox.documentation.service.StringVendorExtension;
import springfox.documentation.service.VendorExtension;
import springfox.documentation.spi.DocumentationType;
import springfox.documentation.spring.web.plugins.Docket;
import springfox.documentation.swagger2.annotations.EnableSwagger2;

@Configuration
@EnableSwagger2
public class SwaggerConfig {

    @Autowired
    private ApplicationContext applicationContext;

    @Bean
    public Docket courtCaseServiceSwagger() {
        Docket docket = new Docket(DocumentationType.SWAGGER_2)
                .useDefaultResponseMessages(false)
                .apiInfo(apiInfo())
                .select()
                .apis(RequestHandlerSelectors.any())
                .paths(Predicates.or(List.of(
                        regex("(\\/ping)"),
                        regex("(\\/feature-flags)"),
                        regex("(\\/info)"),
                        regex("(\\/health)"),
                        regex("(\\/case/.*)"),
                        regex("(\\/offender/.*)"),
                        regex("(\\/offenders/.*)"),
                        regex("(\\/court/.*)"))))
                .build();

        docket.genericModelSubstitutes(Optional.class);
        return docket;
    }

    private BuildProperties getVersion() {
        try {
            return (BuildProperties) applicationContext.getBean("buildProperties");
        } catch (BeansException be) {
            Properties properties = new Properties();
            properties.put("version", "?");
            return new BuildProperties(properties);
        }
    }

    private Contact contactInfo() {
        return new Contact(
                "HMPPS Probation in Court Team",
                "",
                "@digital.justice.gov.uk");
    }

    @SuppressWarnings("rawtypes")
        private ApiInfo apiInfo() {
        final StringVendorExtension vendorExtension = new StringVendorExtension("", "");
        final Collection<VendorExtension> vendorExtensions = new ArrayList<>();
        vendorExtensions.add(vendorExtension);

        return new ApiInfo(
                "Court Case Service API Documentation",
                "REST service for accessing court case information",
                getVersion().getVersion(),
                "https://gateway.nomis-api.service.justice.gov.uk/auth/terms",
                contactInfo(),
                "Open Government Licence v3.0", "https://www.nationalarchives.gov.uk/doc/open-government-licence/version/3/", vendorExtensions);
    }
}
