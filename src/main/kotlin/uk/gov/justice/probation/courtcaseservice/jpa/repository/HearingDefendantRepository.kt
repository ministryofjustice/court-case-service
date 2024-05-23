package uk.gov.justice.probation.courtcaseservice.jpa.repository

import org.springframework.data.jpa.repository.Query
import org.springframework.data.repository.CrudRepository
import org.springframework.stereotype.Repository
import uk.gov.justice.probation.courtcaseservice.jpa.entity.HearingDefendantEntity
import uk.gov.justice.probation.courtcaseservice.jpa.entity.HearingNoteEntity
import java.time.LocalDateTime

@Repository
interface HearingDefendantRepository : CrudRepository<HearingDefendantEntity, Long> {
    fun findAllByDefendantCrn(crn: String) : List<HearingDefendantEntity>
}