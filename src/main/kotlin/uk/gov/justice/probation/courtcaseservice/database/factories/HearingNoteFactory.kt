package uk.gov.justice.probation.courtcaseservice.database.factories

import uk.gov.justice.probation.courtcaseservice.database.factories.framework.Factory
import uk.gov.justice.probation.courtcaseservice.jpa.entity.HearingEntity
import uk.gov.justice.probation.courtcaseservice.jpa.entity.HearingNoteEntity
import uk.gov.justice.probation.courtcaseservice.jpa.repository.HearingNoteRepository

class HearingNoteFactory(
  private val repository: HearingNoteRepository,
  val hearing: HearingEntity,
  val note: String = "An example note for the hearing",
  val authorName: String = "Test User",
  val authorId: String = "user_id",
  val isDraft: Boolean = false,
  val isLegacy: Boolean = false,
) {
  fun count(count: Int = 1) = Factory(
    newModel = {
      HearingNoteEntity.builder()
        .hearingId(hearing.hearingId)
        .author(authorName)
        .createdByUuid(authorId)
        .note(note)
        .draft(isDraft)
        .legacy(isLegacy)
        .build()
    },
    repository = repository,
    count = count,
  ).create()
}
