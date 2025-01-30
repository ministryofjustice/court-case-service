package uk.gov.justice.probation.courtcaseservice.config

import io.swagger.v3.oas.annotations.OpenAPIDefinition
import io.swagger.v3.oas.annotations.enums.SecuritySchemeIn
import io.swagger.v3.oas.annotations.enums.SecuritySchemeType
import io.swagger.v3.oas.annotations.info.License
import io.swagger.v3.oas.annotations.security.SecurityRequirement
import io.swagger.v3.oas.annotations.security.SecurityScheme
import org.springframework.context.annotation.Configuration
import org.springframework.http.HttpHeaders


@Configuration
@OpenAPIDefinition(
    info = io.swagger.v3.oas.annotations.info.Info(
        title = "Court Case Service API Documentation",
        description = "API to access court cases imported from HMCTS Libra court lists and Common Platform.",
        contact = io.swagger.v3.oas.annotations.info.Contact(
            name = "Probation In Court Team",
            email = "",
            url = "https://moj.enterprise.slack.com/archives/C01FR4HKS3A", // #pic-mafia Slack channel
        ),
        license = License(name = "The MIT License (MIT)", url = "https://github.com/ministryofjustice/court-case-service/blob/main/LICENSE"),
        version = "1.0",
    ),
    security = [SecurityRequirement(name = "hmpps-auth-token")],
)
@SecurityScheme(
    name = "hmpps-auth-token",
    scheme = "bearer",
    bearerFormat = "JWT",
    type = SecuritySchemeType.HTTP,
    `in` = SecuritySchemeIn.HEADER,
    paramName = HttpHeaders.AUTHORIZATION,
)
class OpenAPIConfiguration