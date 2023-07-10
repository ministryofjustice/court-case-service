package uk.gov.justice.probation.courtcaseservice.client

import org.slf4j.Logger
import org.slf4j.LoggerFactory
import org.springframework.beans.factory.annotation.Qualifier
import org.springframework.beans.factory.annotation.Value
import org.springframework.http.HttpMethod
import org.springframework.http.HttpStatus
import org.springframework.stereotype.Component
import reactor.core.publisher.Mono
import uk.gov.justice.probation.courtcaseservice.client.exception.ExternalService
import uk.gov.justice.probation.courtcaseservice.client.model.DeliusProbationStatusDetail
import uk.gov.justice.probation.courtcaseservice.restclient.RestClientHelper
import uk.gov.justice.probation.courtcaseservice.service.model.ProbationStatusDetail

@Component
class ProbationStatusDetailRestClient(
    @Qualifier("courtCaseDeliusApiClient") val clientHelper: RestClientHelper,
    @Value("\${court-case-and-delius-api.probation-status-by-crn}") val probationStatusByCrnPath: String) {

    companion object {
        val log: Logger = LoggerFactory.getLogger(this::class.java)
    }

    fun getProbationStatusByCrn(crn : String) : Mono<ProbationStatusDetail> {
        log.debug("Retrieving Probation Status for crn $crn")
        return clientHelper.get(String.format(probationStatusByCrnPath, crn))
            .retrieve()
            .onStatus(HttpStatus::is4xxClientError) {
                log.error("${it.statusCode().value()} Error retrieving Probation Status for crn: $crn")
                clientHelper.handleOffenderError(crn, it)
            }
            .onStatus(HttpStatus::is5xxServerError) {
                log.error("${it.statusCode().value()} Error retrieving Probation Status for crn: $crn")
                handle5xxError(
                    "${it.statusCode().value()} Error retrieving Probation Status for crn: $crn",
                    HttpMethod.GET,
                    probationStatusByCrnPath,
                    ExternalService.PROBATION_STATUS_DETAIL)
            }
            .bodyToMono(DeliusProbationStatusDetail::class.java)
            .map { DeliusProbationStatusDetail.from(it) }
    }

}