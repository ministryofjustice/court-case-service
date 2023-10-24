package uk.gov.justice.probation.courtcaseservice.client

import org.slf4j.Logger
import org.slf4j.LoggerFactory
import org.springframework.beans.factory.annotation.Qualifier
import org.springframework.beans.factory.annotation.Value
import org.springframework.http.HttpMethod
import org.springframework.http.HttpStatusCode
import org.springframework.stereotype.Component
import uk.gov.justice.probation.courtcaseservice.client.exception.ExternalService
import uk.gov.justice.probation.courtcaseservice.restclient.RestClientHelper

@Component
class ManageOffencesRestClient(
    @Qualifier("manageOffencesApiClient") val clientHelper: RestClientHelper,
    @Value("\${manage-offences-api.get-home-office-offence-code}") val getHomeOfficeOffenceCodePath: String) {

    companion object {
        val log: Logger = LoggerFactory.getLogger(this::class.java)
    }

    fun getHomeOfficeOffenceCodeByCJSCode(cjsCode : String) : String? {
        log.debug("Retrieving home office offence code for CJS code $cjsCode")
        return clientHelper.get(String.format(getHomeOfficeOffenceCodePath, cjsCode))
            .retrieve()
            .onStatus(HttpStatusCode::is4xxClientError) {
                log.error("${it.statusCode().value()} Error retrieving Home Office Offence code for CJS code: $cjsCode")
                handle4xxError(
                    it,
                    HttpMethod.GET,
                    getHomeOfficeOffenceCodePath,
                    ExternalService.MANAGE_OFFENCES) }
            .onStatus(HttpStatusCode::is5xxServerError) {
                log.error("${it.statusCode().value()} Error retrieving Home Office Offence code for CJS code: $cjsCode")
                handle5xxError(
                    "${it.statusCode().value()} Error retrieving Home Office Offence code for CJS code: $cjsCode",
                    HttpMethod.GET,
                    getHomeOfficeOffenceCodePath,
                    ExternalService.MANAGE_OFFENCES) }
            .bodyToMono(String::class.java)
            .block()
    }
}