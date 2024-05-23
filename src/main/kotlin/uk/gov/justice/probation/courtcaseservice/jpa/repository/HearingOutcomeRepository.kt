package uk.gov.justice.probation.courtcaseservice.jpa.repository

import org.springframework.data.jpa.repository.Query
import org.springframework.data.repository.CrudRepository
import org.springframework.stereotype.Repository
import uk.gov.justice.probation.courtcaseservice.jpa.entity.HearingOutcomeEntity
import java.time.LocalDateTime
import java.util.*

@Repository
interface HearingOutcomeRepository: CrudRepository<HearingOutcomeEntity, Long> {

    @Query(
        value = "select ho.* from hearing_outcome ho " +
                "inner join hearing_defendant hd on ho.fk_hearing_defendant_id = hd.id " +
                "and ho.fk_hearing_defendant_id = :hearingDefendantId " +
                "and (cast(:fromDate as date) IS NULL OR ho.created > :fromDate) AND (cast(:toDate as date) IS NULL OR ho.created <= :toDate)",
                nativeQuery = true
        )
    fun findAllByHearingDefendantIdAndOutcomeDateBetween(hearingDefendantId: Long, fromDate: LocalDateTime?, toDate: LocalDateTime?): List<HearingOutcomeEntity>

}