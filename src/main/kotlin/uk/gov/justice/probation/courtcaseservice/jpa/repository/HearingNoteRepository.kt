package uk.gov.justice.probation.courtcaseservice.jpa.repository

import org.springframework.data.jpa.repository.Query
import org.springframework.data.repository.CrudRepository
import org.springframework.data.repository.query.Param
import org.springframework.stereotype.Repository
import uk.gov.justice.probation.courtcaseservice.jpa.entity.HearingDefendantEntity
import uk.gov.justice.probation.courtcaseservice.jpa.entity.HearingNoteEntity
import java.time.LocalDateTime

@Repository
interface HearingNoteRepository: CrudRepository<HearingNoteEntity, Long> {

    @Query(value = "select hn.* from hearing_notes hn " +
                "where hn.fk_hearing_defendant_id = :hearingDefendantId " +
                "and (cast(:fromDateTime as date) IS NULL OR hn.created > :fromDateTime) AND (cast(:toDateTime as date) IS NULL OR hn.created <= :toDateTime)",
        nativeQuery = true)
    fun findAllByHearingDefendantIdAndCreatedIsBetween(hearingDefendantId: Long, fromDateTime: LocalDateTime?, toDateTime: LocalDateTime?): List<HearingNoteEntity>

}