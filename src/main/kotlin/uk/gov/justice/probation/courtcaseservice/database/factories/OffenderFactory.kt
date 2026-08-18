package uk.gov.justice.probation.courtcaseservice.database.factories

import uk.gov.justice.probation.courtcaseservice.database.data.Faker
import uk.gov.justice.probation.courtcaseservice.database.factories.framework.Factory
import uk.gov.justice.probation.courtcaseservice.jpa.entity.OffenderEntity
import uk.gov.justice.probation.courtcaseservice.jpa.entity.OffenderProbationStatus
import uk.gov.justice.probation.courtcaseservice.jpa.repository.OffenderRepository

class OffenderFactory(
  private val repository: OffenderRepository,
  private val crn: String = "",
  private val pnc: String = "",
  private val cro: String = "",
) {
  private val faker = Faker()

  fun count(count: Int = 1): List<OffenderEntity> = Factory(
    newModel = {
      val cro = cro.ifEmpty { faker.cro() }
      val crn = crn.ifEmpty { faker.crn() }
      val pnc = pnc.ifEmpty { faker.pnc() }

      OffenderEntity.builder()
        .crn(crn)
        .pnc(pnc)
        .cro(cro)
        .probationStatus(getRandomProbationStatus())
        .awaitingPsr(listOf(true, false).random())
        .breach(getProbableBreachStatus())
        // .suspendedSentenceOrder(false)
        // .previouslyKnownTerminationDate(LocalDate.now())
        // .preSentenceActivity(false)
        .build()
    },
    repository = repository,
    count = count,
  ).create()

  fun getRandomProbationStatus() = listOf(
    OffenderProbationStatus.CURRENT,
    OffenderProbationStatus.PREVIOUSLY_KNOWN,
    OffenderProbationStatus.NOT_SENTENCED,
  ).random()

  // 10% chance of breach of bail
  fun getProbableBreachStatus(): Boolean = (1..100).random() <= 10
}
