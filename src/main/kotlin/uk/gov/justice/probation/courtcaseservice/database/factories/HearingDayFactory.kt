package uk.gov.justice.probation.courtcaseservice.database.factories

import uk.gov.justice.probation.courtcaseservice.database.factories.framework.Factory
import uk.gov.justice.probation.courtcaseservice.jpa.entity.HearingDayEntity
import uk.gov.justice.probation.courtcaseservice.jpa.entity.HearingEntity
import uk.gov.justice.probation.courtcaseservice.jpa.repository.HearingDayRepository
import java.time.LocalDate
import java.time.LocalTime

class HearingDayFactory(
  private val repository: HearingDayRepository,
  private var day: LocalDate = LocalDate.now(),
  private var time: LocalTime = LocalTime.now(),
  private var courtCode: String = "B10JQ",
  private var courtRoom: String = "1",
  private var hearing: HearingEntity? = null,
) {

  fun count(count: Int = 1) = Factory(
    newModel = {
      HearingDayEntity.builder()
        .id(null)
        .day(day)
        .time(time)
        .courtCode(courtCode)
        .courtRoom(courtRoom)
        .hearing(hearing)
        .build()
    },
    repository = repository,
    count = count,
  ).create()
}
