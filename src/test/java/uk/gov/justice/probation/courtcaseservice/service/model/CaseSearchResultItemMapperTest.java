package uk.gov.justice.probation.courtcaseservice.service.model;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.junit.jupiter.MockitoExtension;
import uk.gov.justice.probation.courtcaseservice.jpa.entity.CourtCaseEntity;
import uk.gov.justice.probation.courtcaseservice.jpa.entity.CourtEntity;
import uk.gov.justice.probation.courtcaseservice.jpa.entity.DefendantProbationStatus;
import uk.gov.justice.probation.courtcaseservice.jpa.entity.EntityHelper;
import uk.gov.justice.probation.courtcaseservice.jpa.entity.HearingDayEntity;
import uk.gov.justice.probation.courtcaseservice.jpa.entity.HearingDefendantEntity;
import uk.gov.justice.probation.courtcaseservice.jpa.entity.HearingEntity;
import uk.gov.justice.probation.courtcaseservice.jpa.entity.OffenceEntity;
import uk.gov.justice.probation.courtcaseservice.service.mapper.CaseSearchResultItemMapper;

import java.time.Clock;
import java.time.Instant;
import java.time.LocalDate;
import java.time.LocalTime;
import java.time.ZoneId;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@ExtendWith(MockitoExtension.class)
class CaseSearchResultItemMapperTest {

    static CourtEntity sheffieldCourt = CourtEntity.builder().name("Sheffield Mags").build();
    static CourtEntity leicesterCourt = CourtEntity.builder().name("Leicester Mags").build();

    static String testCrn = "X12345";
    static String defendantId1 = "defendant-id-1";
    static HearingDefendantEntity hearingDefendant1 = EntityHelper.aHearingDefendantEntity(defendantId1, testCrn)
        .withOffences(List.of(OffenceEntity.builder().title("offence title 1").build()));
    static HearingEntity hearingEntity1 = HearingEntity.builder()
        .hearingId("hearing-id-1")
        .hearingDays(List.of(
            HearingDayEntity.builder().day(LocalDate.of(2022, 10, 10)).time(LocalTime.of(9, 0, 0)).court(sheffieldCourt).build())
        )
        .hearingDefendants(List.of(hearingDefendant1))
        .build();

    static HearingDefendantEntity hearingDefendant21 = EntityHelper.aHearingDefendantEntity(defendantId1, testCrn)
        .withOffences(List.of(OffenceEntity.builder().title("offence title 3").build(),OffenceEntity.builder().title("offence title 2").build()));
    static HearingDefendantEntity hearingDefendant22 = EntityHelper.aHearingDefendantEntity("defendant-id-2", "Y12345")
        .withOffences(List.of(OffenceEntity.builder().title("offence title 4").build()));
    static HearingEntity hearingEntity2 = HearingEntity.builder()
        .hearingId("hearing-id-2")
        .hearingDays(List.of(
            HearingDayEntity.builder().day(LocalDate.of(2022, 11, 10)).time(LocalTime.of(9, 0, 0)).court(sheffieldCourt).build(),
            HearingDayEntity.builder().day(LocalDate.of(2022, 12, 10)).time(LocalTime.of(9, 0, 0)).court(sheffieldCourt).build())
        )
        .hearingDefendants(List.of(hearingDefendant21, hearingDefendant22))
        .build();

    static HearingDefendantEntity hearingDefendant3 = EntityHelper.aHearingDefendantEntity(defendantId1, testCrn)
        .withOffences(List.of(OffenceEntity.builder().title("offence title 1").build()));
    static HearingEntity hearingEntity3 = HearingEntity.builder()
        .hearingId("hearing-id-3")
        .hearingDays(List.of(
            HearingDayEntity.builder().day(LocalDate.of(2023, 02, 18)).time(LocalTime.of(9, 0, 0)).court(sheffieldCourt).build(),
            HearingDayEntity.builder().day(LocalDate.of(2023, 01, 23)).time(LocalTime.of(9, 0, 0)).court(leicesterCourt).build())
        )
        .hearingDefendants(List.of(hearingDefendant3))
        .build();
    private final Clock fixedClock = Clock.fixed(Instant.parse("2022-12-13T09:00:00.000Z"), ZoneId.systemDefault());
    private final CaseSearchResultItemMapper subject = new CaseSearchResultItemMapper(fixedClock);

    @Test
    void shouldMapCourtCaseEntitiesToSearchResult() {
        List<HearingEntity> hearings = List.of(hearingEntity1, hearingEntity2, hearingEntity3);

        CourtCaseEntity courtCaseEntity = CourtCaseEntity.builder().hearings(hearings).build();
        EntityHelper.refreshMappings(courtCaseEntity);
        var actual = subject.from(courtCaseEntity, testCrn);

        var result1 = CaseSearchResultItem.builder()
            .hearingId("hearing-id-1")
            .defendantId(defendantId1)
            .defendantName("Mr Gordon BENNETT")
            .crn(testCrn)
            .offenceTitles(List.of("offence title 1", "offence title 2", "offence title 3"))
            .lastHearingDate(LocalDate.of(2022, 12, 10))
            .lastHearingCourt("Sheffield Mags")
            .nextHearingDate(LocalDate.of(2023, 01, 23))
            .nextHearingCourt("Leicester Mags")
            .probationStatus(DefendantProbationStatus.PREVIOUSLY_KNOWN)
            .build();

        assertThat(actual).isEqualTo(result1);
    }

    @Test
    void givenNoLastHearing_shouldMapCourtCaseEntitiesToSearchResult_setLastHearingDetailsToNull() {
        List<HearingEntity> hearings = List.of(hearingEntity3);

        CourtCaseEntity courtCaseEntity = CourtCaseEntity.builder().hearings(hearings).build();
        EntityHelper.refreshMappings(courtCaseEntity);
        var actual = subject.from(courtCaseEntity, testCrn);

        var result = CaseSearchResultItem.builder()
            .hearingId("hearing-id-3")
            .defendantId(defendantId1)
            .defendantName("Mr Gordon BENNETT")
            .crn(testCrn)
            .offenceTitles(List.of("offence title 1"))
            .nextHearingDate(LocalDate.of(2023, 01, 23))
            .nextHearingCourt("Leicester Mags")
            .probationStatus(DefendantProbationStatus.PREVIOUSLY_KNOWN)
            .build();

        assertThat(actual).isEqualTo(result);
    }

    @Test
    void givenNoNextHearing_shouldMapCourtCaseEntitiesToSearchResult_setNextHearingDetailsToNull() {
        List<HearingEntity> hearings = List.of(hearingEntity1, hearingEntity2);

        CourtCaseEntity courtCaseEntity = CourtCaseEntity.builder().hearings(hearings).build();
        EntityHelper.refreshMappings(courtCaseEntity);
        var actual = subject.from(courtCaseEntity, testCrn);

        var result = CaseSearchResultItem.builder()
            .hearingId("hearing-id-1")
            .defendantId(defendantId1)
            .defendantName("Mr Gordon BENNETT")
            .crn(testCrn)
            .offenceTitles(List.of("offence title 1", "offence title 2", "offence title 3"))
            .lastHearingDate(LocalDate.of(2022, 12, 10))
            .lastHearingCourt("Sheffield Mags")
            .probationStatus(DefendantProbationStatus.PREVIOUSLY_KNOWN)
            .build();

        assertThat(actual).isEqualTo(result);
    }
}