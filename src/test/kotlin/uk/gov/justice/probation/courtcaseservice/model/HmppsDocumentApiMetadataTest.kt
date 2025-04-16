package uk.gov.justice.probation.courtcaseservice.model

import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.Test
import uk.gov.justice.probation.courtcaseservice.controller.model.HmppsDocumentApiMetadata

class HmppsDocumentApiMetadataTest {

  @Test
  fun shouldStoreCaseURNDefendantId() {
    val caseUrn = "case-urn"
    val defendantId = "defendant-id"
    val metaData = HmppsDocumentApiMetadata(caseUrn, defendantId)

    assertThat(metaData.caseUrn).isEqualTo(caseUrn)
    assertThat(metaData.defendantId).isEqualTo(defendantId)
  }

  @Test
  fun `given Null case_urn, should store case_urn and defendant_id`() {
    val caseUrn = null
    val defendantId = "defendant-id"
    val metaData = HmppsDocumentApiMetadata(caseUrn, defendantId)

    assertThat(metaData.caseUrn).isNull()
    assertThat(metaData.defendantId).isEqualTo(defendantId)
  }
}
