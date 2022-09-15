package uk.gov.justice.probation.courtcaseservice.service.model;


import org.assertj.core.api.Assertions;
import org.junit.jupiter.api.Test;
import uk.gov.justice.probation.courtcaseservice.jpa.entity.CourtCaseEntity;
import uk.gov.justice.probation.courtcaseservice.jpa.entity.CourtEntity;
import uk.gov.justice.probation.courtcaseservice.jpa.entity.EntityHelper;
import uk.gov.justice.probation.courtcaseservice.jpa.entity.HearingEntity;

import java.time.LocalDateTime;
import java.util.List;

import static uk.gov.justice.probation.courtcaseservice.jpa.entity.CourtSession.MORNING;
import static uk.gov.justice.probation.courtcaseservice.jpa.entity.SourceType.COMMON_PLATFORM;
import static uk.gov.justice.probation.courtcaseservice.jpa.entity.SourceType.LIBRA;

class CaseProgressHearingTest {

    @Test
    void givenCPCase_shouldMapToCaseProgressHearing() {
        var hearingDayEntity1 = EntityHelper.aHearingDayEntity(LocalDateTime.of(2022, 2, 26, 9, 0)).withCourtRoom("Room 1").withCourt(CourtEntity.builder().name("Leeds mags court").build());
        var hearingDayEntity2 = EntityHelper.aHearingDayEntity(LocalDateTime.of(2022, 5, 5, 9, 0)).withCourtRoom("Room 2").withCourt(CourtEntity.builder().name("Sheffield mags court").build());;

        var hearingEntity = HearingEntity.builder()
            .hearingId("test-hearing-id")
            .hearingType("Sentence")
            .courtCase(CourtCaseEntity.builder().sourceType(COMMON_PLATFORM).build())
            .hearingDays(List.of(hearingDayEntity2, hearingDayEntity1))
            .build();

        Assertions.assertThat(CaseProgressHearing.of(hearingEntity)).isEqualTo(
            CaseProgressHearing.builder().
            hearingDateTime(LocalDateTime.of(2022, 2, 26, 9, 0))
                .court("Leeds mags court")
                .courtRoom("Room 1")
                .hearingTypeLabel("Sentence")
                .session(MORNING.name())
                .hearingId("test-hearing-id")
            .build()
        );
    }

    @Test
    void givenLibraCase_shouldMapToCaseProgressHearing() {
        var hearingDayEntity1 = EntityHelper.aHearingDayEntity(LocalDateTime.of(2022, 2, 26, 9, 0))
                                                    .withCourtRoom("Room 1").withListNo("1st").withCourt(CourtEntity.builder().name("Leeds mags court").build());
        var hearingDayEntity2 = EntityHelper.aHearingDayEntity(LocalDateTime.of(2022, 5, 5, 9, 0))
                                                    .withCourtRoom("Room 2").withListNo("2nd").withCourt(CourtEntity.builder().name("Sheffield mags court").build());;

        var hearingEntity = HearingEntity.builder()
            .hearingId("test-hearing-id")
            .hearingType("Sentence")
            .courtCase(CourtCaseEntity.builder().sourceType(LIBRA).build())
            .hearingDays(List.of(hearingDayEntity2, hearingDayEntity1))
            .build();

        Assertions.assertThat(CaseProgressHearing.of(hearingEntity)).isEqualTo(
            CaseProgressHearing.builder().
            hearingDateTime(LocalDateTime.of(2022, 2, 26, 9, 0))
                .court("Leeds mags court")
                .courtRoom("Room 1")
                .hearingTypeLabel("1st hearing")
                .session(MORNING.name())
                .hearingId("test-hearing-id")
            .build()
        );
    }

}