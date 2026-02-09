package uk.gov.justice.probation.courtcaseservice.database.factories

import uk.gov.justice.probation.courtcaseservice.database.factories.framework.Factory
import uk.gov.justice.probation.courtcaseservice.jpa.entity.CourtCaseEntity
import uk.gov.justice.probation.courtcaseservice.jpa.entity.HearingEntity
import uk.gov.justice.probation.courtcaseservice.jpa.entity.HearingEventType
import uk.gov.justice.probation.courtcaseservice.jpa.repository.HearingRepository
import java.util.UUID

class HearingFactory(
  private val repository: HearingRepository,
  private var courtCase: CourtCaseEntity,
  private var hearingId: String = UUID.randomUUID().toString(),
  private var caseId: String = UUID.randomUUID().toString(),
  private var hearingType: HearingEventType = HearingEventType.UNKNOWN,
) {

  fun count(count: Int = 1) = Factory(
    newModel = {
      HearingEntity.builder()
        .id(null)
        .hearingId(hearingId)
        .courtCaseId(caseId)
        .hearingType(hearingType.toString())
        .courtCase(courtCase)
        .hearingDays(mutableListOf())
        .hearingDefendants(mutableListOf())
        .build()
    },
    repository = repository,
    count = count,
  ).create()
}