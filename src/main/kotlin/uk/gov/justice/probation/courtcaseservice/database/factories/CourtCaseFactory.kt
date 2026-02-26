package uk.gov.justice.probation.courtcaseservice.database.factories

import uk.gov.justice.probation.courtcaseservice.database.factories.framework.Factory
import uk.gov.justice.probation.courtcaseservice.jpa.entity.CaseMarkerEntity
import uk.gov.justice.probation.courtcaseservice.jpa.entity.CourtCaseEntity
import uk.gov.justice.probation.courtcaseservice.jpa.entity.SourceType
import uk.gov.justice.probation.courtcaseservice.jpa.repository.CourtCaseRepository
import java.util.UUID

class CourtCaseFactory(
  private val repository: CourtCaseRepository,
  var caseId: String = UUID.randomUUID().toString(),
  var caseNo: String = UUID.randomUUID().toString().take(10),
  var urn: String = "URN-${UUID.randomUUID()}",
  var sourceType: SourceType = SourceType.LIBRA,
  var caseMarkers: List<String> = emptyList(),
) {
  fun withCaseMarkers(vararg markers: String) = apply { this.caseMarkers = markers.toList() }
  fun withCaseMarkers(markers: List<String>) = apply { this.caseMarkers = markers }

  fun count(count: Int = 1) = Factory(
    newModel = {
      val markerEntities = caseMarkers.map { CaseMarkerEntity.builder().typeDescription(it).build() }
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
        .apply { addCaseMarkers(markerEntities) }
    },
    repository = repository,
    count = count,
  ).create()
}
