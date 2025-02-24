package uk.gov.justice.probation.courtcaseservice.jpa.repository

import org.springframework.data.repository.CrudRepository
import org.springframework.stereotype.Repository
import uk.gov.justice.probation.courtcaseservice.jpa.entity.HearingNoteEntity
import java.time.LocalDateTime

@Repository
interface HearingNoteRepository: CrudRepository<HearingNoteEntity, Long> {
    fun findAllByHearingDefendantIdAndDeletedFalseAndDraftFalseAndLegacyFalseAndCreatedBefore(hearingDefendantId: Long, toDateTime: LocalDateTime?): List<HearingNoteEntity>

    fun findAllByHearingDefendantIdAndDeletedFalseAndDraftFalseAndLegacyFalseAndCreatedAfter(hearingDefendantId: Long, fromDateTime: LocalDateTime?): List<HearingNoteEntity>

    fun findAllByHearingDefendantIdAndDeletedFalseAndDraftFalseAndLegacyFalseAndCreatedBetween(hearingDefendantId: Long, fromDateTime: LocalDateTime?, toDateTime: LocalDateTime?): List<HearingNoteEntity>

    fun findByHearingDefendantId(hearingDefendantId: Long): List<HearingNoteEntity>
}