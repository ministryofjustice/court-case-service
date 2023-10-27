package uk.gov.justice.probation.courtcaseservice.client

import org.slf4j.Logger
import org.slf4j.LoggerFactory
import org.springframework.beans.factory.annotation.Qualifier
import org.springframework.http.HttpMethod
import org.springframework.http.HttpStatusCode
import org.springframework.stereotype.Component
import reactor.core.publisher.Mono
import uk.gov.justice.probation.courtcaseservice.client.exception.ExternalService
import uk.gov.justice.probation.courtcaseservice.client.model.DeliusOffenderDetail
import uk.gov.justice.probation.courtcaseservice.restclient.RestClientHelper

@Component
class OffenderDetailRestClient(
    @Qualifier("domainEventAndDeliusApiClient") val clientHelper: RestClientHelper) {

    companion object {
        val log: Logger = LoggerFactory.getLogger(this::class.java)
    }

    fun getOffenderDetail(detailUrl: String, crn: String?): Mono<DeliusOffenderDetail> {
        log.debug("Retrieving Offender Details for crn $crn")
        return clientHelper.get(detailUrl)
            .retrieve()
            .onStatus(HttpStatusCode::is4xxClientError) {
                log.error("${it.statusCode().value()} Error retrieving Offender Detail for crn: $crn")
                clientHelper.handleOffenderError(crn, it)
            }
            .onStatus(HttpStatusCode::is5xxServerError) {
                log.error("${it.statusCode().value()} Error retrieving Offender Detail for crn: $crn")
                handle5xxError(
                    "${it.statusCode().value()} Error retrieving Offender Detail for crn: $crn",
                    HttpMethod.GET,
                    detailUrl,
                    ExternalService.NEW_OFFENDER_DETAIL
                )
            }
            .bodyToMono(DeliusOffenderDetail::class.java)
    }

}