package uk.gov.justice.probation.courtcaseservice.database.factories

import uk.gov.justice.probation.courtcaseservice.database.data.Faker
import uk.gov.justice.probation.courtcaseservice.database.factories.framework.Factory
import uk.gov.justice.probation.courtcaseservice.jpa.entity.CaseMarkerEntity
import uk.gov.justice.probation.courtcaseservice.jpa.entity.CourtCaseEntity
import uk.gov.justice.probation.courtcaseservice.jpa.entity.SourceType
import uk.gov.justice.probation.courtcaseservice.jpa.repository.CourtCaseRepository
import java.util.UUID

class CourtCaseFactory(
  private val repository: CourtCaseRepository,
  var caseId: String = UUID.randomUUID().toString(),
  var caseNo: String = "",
  var urn: String = "",
  var sourceType: SourceType? = null,
  var caseMarkers: List<String> = emptyList(),
) {
  private val faker = Faker()

  fun withCaseMarkers(vararg markers: String) = apply { this.caseMarkers = markers.toList() }
  fun withCaseMarkers(markers: List<String>) = apply { this.caseMarkers = markers }

  fun count(count: Int = 1): List<CourtCaseEntity> {
    sourceType = sourceType ?: randomSource()
    caseNo = caseNo.ifEmpty { faker.number().numberBetween(1000000000, 9999999999).toString() }

    // When you have a Libra record case, you shouldn't have a URN available
    // URN is 11 characters, two numbers, two capital letters, six numbers
    if (sourceType != SourceType.LIBRA) {
      urn = urn.ifEmpty { faker.regexify("URN-[0-9]{2}[A-Z]{2}[0-9]{6}") }
    } else {
      urn = ""
    }

    return Factory(
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

  private fun randomSource(): SourceType? = faker.options().option(SourceType.COMMON_PLATFORM, SourceType.LIBRA)
}
