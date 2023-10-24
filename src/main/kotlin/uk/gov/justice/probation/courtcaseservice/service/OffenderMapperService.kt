package uk.gov.justice.probation.courtcaseservice.service

import org.slf4j.Logger
import org.slf4j.LoggerFactory
import org.springframework.stereotype.Service
import uk.gov.justice.probation.courtcaseservice.client.model.DeliusOffenderDetail
import uk.gov.justice.probation.courtcaseservice.jpa.repository.DefendantRepository

@Service
class OffenderMapperService (val defendantRepository: DefendantRepository){

    private companion object {
        val log: Logger = LoggerFactory.getLogger(this::class.java)
    }

    fun mapOffenderToMatchingDefendants(deliusOffenderDetail: DeliusOffenderDetail?) {
        log.debug("Mapping new offender with details {}", deliusOffenderDetail)
        defendantRepository.findMatchingDefendants(
            deliusOffenderDetail?.name?.foreName,
            deliusOffenderDetail?.name?.surName,
            deliusOffenderDetail?.identifiers?.pnc,
            deliusOffenderDetail?.dateOfBirth
        ).forEach { defendantEntity ->
                deliusOffenderDetail?.identifiers?.crn.also { defendantEntity.crn = it }
                defendantRepository.save(defendantEntity)
            }
    }
}