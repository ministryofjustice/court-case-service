package uk.gov.justice.probation.courtcaseservice.database.factories

import uk.gov.justice.probation.courtcaseservice.database.factories.framework.Factory
import uk.gov.justice.probation.courtcaseservice.jpa.entity.CourtCaseEntity
import uk.gov.justice.probation.courtcaseservice.jpa.entity.SourceType
import uk.gov.justice.probation.courtcaseservice.jpa.repository.CourtCaseRepository
import java.util.UUID

class CourtCaseFactory(
  private val repository: CourtCaseRepository,
  private var caseId: String = UUID.randomUUID().toString(),
  private var caseNo: String = UUID.randomUUID().toString().take(10),
  private var urn: String = "URN-${UUID.randomUUID()}",
  private var sourceType: SourceType = SourceType.LIBRA,
) {

  fun withCaseId(value: String) = apply { this.caseId = value }
  fun withCaseNo(value: String) = apply { this.caseNo = value }
  fun withUrn(value: String) = apply { this.urn = value }
  fun withSourceType(value: SourceType) = apply { this.sourceType = value }

  fun count(count: Int = 1) = Factory(
    newModel = {
      CourtCaseEntity.builder()
        .id(null)
        .caseId(caseId)
        .caseNo(caseNo)
        .urn(urn)
        .hearings(mutableListOf())
        .caseComments(mutableListOf())
        .sourceType(sourceType)
        .caseMarkers(mutableListOf())
        .caseDefendants(mutableListOf())
        .build()
    },
    repository = repository,
    count = count,
  ).create()
}
