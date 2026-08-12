package uk.gov.justice.probation.courtcaseservice.database.factories

import uk.gov.justice.probation.courtcaseservice.database.factories.framework.Factory
import uk.gov.justice.probation.courtcaseservice.jpa.entity.HearingDayEntity
import uk.gov.justice.probation.courtcaseservice.jpa.entity.HearingEntity
import uk.gov.justice.probation.courtcaseservice.jpa.repository.HearingDayRepository
import java.time.LocalDate
import java.time.LocalTime

class HearingDayFactory(
  private val repository: HearingDayRepository,
  private var date: LocalDate = LocalDate.now(),
  private var time: LocalTime? = null,
  private var courtCode: String = "B10JQ",
  private var courtRoom: String = "",
  private var hearing: HearingEntity? = null,
) {

  fun count(count: Int = 1) : List<HearingDayEntity> {
    courtRoom.ifEmpty { courtRoom = listOf("1", "2").random() }
    if (time == null) {
      time = LocalTime.of(listOf(9, 13).random(), 0)
    }

    return Factory(
      newModel = {
        HearingDayEntity.builder()
          .id(null)
          .day(date)
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
}
