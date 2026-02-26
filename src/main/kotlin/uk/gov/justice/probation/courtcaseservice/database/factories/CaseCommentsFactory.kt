package uk.gov.justice.probation.courtcaseservice.database.factories

import uk.gov.justice.probation.courtcaseservice.database.factories.framework.Factory
import uk.gov.justice.probation.courtcaseservice.jpa.entity.CaseCommentEntity
import uk.gov.justice.probation.courtcaseservice.jpa.repository.CaseCommentsRepository
import java.util.UUID

class CaseCommentsFactory(
  private val repository: CaseCommentsRepository,
  private var caseId: String = UUID.randomUUID().toString(),
  private var defendantId: String = UUID.randomUUID().toString(),
  private var createdByUuid: String = UUID.randomUUID().toString(),
  private var comment: String = "Sample case comment",
  private var author: String = "Test Author",
  private var draft: Boolean = true,
  private var legacy: Boolean = false,
) {

  fun count(count: Int = 1) = Factory(
    newModel = {
      CaseCommentEntity.builder()
        .id(null)
        .caseId(caseId)
        .defendantId(defendantId)
        .createdByUuid(createdByUuid)
        .comment(comment)
        .author(author)
        .draft(draft)
        .legacy(legacy)
        .build()
    },
    repository = repository,
    count = count,
  ).create()
}
