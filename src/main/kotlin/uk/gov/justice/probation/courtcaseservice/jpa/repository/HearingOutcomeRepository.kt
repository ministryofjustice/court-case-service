package uk.gov.justice.probation.courtcaseservice.jpa.repository

import org.springframework.data.jpa.repository.Query
import org.springframework.data.repository.CrudRepository
import org.springframework.stereotype.Repository
import uk.gov.justice.probation.courtcaseservice.jpa.entity.HearingOutcomeEntity
import java.time.LocalDateTime
import java.util.*

@Repository
interface HearingOutcomeRepository: CrudRepository<HearingOutcomeEntity, Long> {

    fun findByHearingDefendantId(hearingDefendantId: Long?): List<HearingOutcomeEntity>
    fun findAllByHearingDefendantIdAndCreatedBefore(hearingDefendantId: Long?, toDateTime: LocalDateTime?): List<HearingOutcomeEntity>

    fun findAllByHearingDefendantIdAndCreatedAfter(hearingDefendantId: Long?, fromDateTime: LocalDateTime?): List<HearingOutcomeEntity>

    fun findAllByHearingDefendantIdAndCreatedBetween(hearingDefendantId: Long?, fromDateTime: LocalDateTime?, toDateTime: LocalDateTime?): List<HearingOutcomeEntity>
}