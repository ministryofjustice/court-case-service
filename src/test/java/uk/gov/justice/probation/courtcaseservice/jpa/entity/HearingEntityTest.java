package uk.gov.justice.probation.courtcaseservice.jpa.entity;

import org.junit.jupiter.api.Test;
import uk.gov.justice.probation.courtcaseservice.service.HearingOutcomeType;

import java.util.Collections;
import java.util.List;

import static org.assertj.core.api.AssertionsForClassTypes.assertThat;
import static uk.gov.justice.probation.courtcaseservice.jpa.entity.EntityHelper.DEFENDANT_ID;
import static uk.gov.justice.probation.courtcaseservice.jpa.entity.EntityHelper.getMutableList;

class HearingEntityTest {

    private final HearingEntity hearingEntity =
            HearingEntity.builder()
                    .hearingDefendants(List.of(HearingDefendantEntity.builder()
                                    .defendantId("abc")
                                    .defendant(DefendantEntity.builder()
                                            .defendantId("abc")
                                            .build())
                                    .build(),
                            HearingDefendantEntity.builder()
                                    .defendantId(DEFENDANT_ID)
                                    .defendant(DefendantEntity.builder()
                                            .defendantId(DEFENDANT_ID)
                                            .build())
                                    .build()))
                    .build();

    @Test
    void givenHearingWithDefendants_thenReturn() {
        var hearingDefendant = hearingEntity.getHearingDefendant(DEFENDANT_ID);
        assertThat(hearingDefendant.getDefendantId()).isEqualTo(DEFENDANT_ID);
        assertThat(hearingDefendant.getDefendant().getDefendantId()).isEqualTo(DEFENDANT_ID);
    }

    @Test
    void givenHearingWithDefendants_whenRequestWrongId_thenReturnNull() {
        assertThat(hearingEntity.getHearingDefendant("XXX")).isNull();
    }

    @Test
    void givenHearingWithNoDefendants_thenReturnNull() {
        var courtCase = HearingEntity.builder().build();
        assertThat(courtCase.getHearingDefendant("XXX")).isNull();
    }

    @Test
    void givenHearingToUpdate_shouldUpdateHearingDays() {
        var dbHearingDayEntity = HearingDayEntity.builder().hearing(HearingEntity.builder().build()).courtCode("Court-1").build();
        var dbHearing = HearingEntity.builder()
                .courtCase(CourtCaseEntity.builder().build())
                .hearingDefendants(Collections.emptyList())
                .hearingType("Trial")
                .hearingEventType(HearingEventType.UNKNOWN)
                .hearingDays(getMutableList(List.of(dbHearingDayEntity)))
                .listNo("1")
                .build();

        var newHearingDay = HearingDayEntity.builder().hearing(HearingEntity.builder().build()).courtCode("Court-2").build();
        var hearingUpdate = HearingEntity.builder()
                .courtCase(CourtCaseEntity.builder().build())
                .hearingDefendants(Collections.emptyList())
                .hearingType("Sentenced")
                .hearingEventType(HearingEventType.CONFIRMED_OR_UPDATED)
                .hearingDays(getMutableList(List.of(newHearingDay)))
                .listNo("2")
                .build();

        dbHearing.update(hearingUpdate);
        assertThat(dbHearing).isEqualTo(hearingUpdate);
        assertThat(dbHearingDayEntity.getHearing()).isNull();
        assertThat(newHearingDay.getHearing()).isEqualTo(hearingUpdate);
    }

    @Test
    void givenHearingToUpdate_shouldUpdateCourtCase() {
        var dbHearingDayEntity = HearingDayEntity.builder().hearing(HearingEntity.builder().build()).courtCode("Court-1").build();
        var dbHearing = HearingEntity.builder()
            .courtCase(CourtCaseEntity.builder().build())
            .hearingDefendants(Collections.emptyList())
            .hearingType("Trial")
            .hearingEventType(HearingEventType.UNKNOWN)
            .hearingDays(getMutableList(List.of(dbHearingDayEntity)))
            .listNo("1")
            .build();

        var newHearingDay = HearingDayEntity.builder().hearing(HearingEntity.builder().build()).courtCode("Court-2").build();
        var hearingUpdate = HearingEntity.builder()
            .courtCase(CourtCaseEntity.builder().urn("urn-one").build())
            .hearingDefendants(Collections.emptyList())
            .hearingType("Sentenced")
            .hearingEventType(HearingEventType.CONFIRMED_OR_UPDATED)
            .hearingDays(getMutableList(List.of(newHearingDay)))
            .listNo("2")
            .build();

        dbHearing.update(hearingUpdate);
        assertThat(dbHearing).isEqualTo(hearingUpdate);
        assertThat(dbHearingDayEntity.getHearing()).isNull();
        assertThat(newHearingDay.getHearing()).isEqualTo(hearingUpdate);
        assertThat(hearingUpdate.getCourtCase()).isEqualTo(hearingUpdate.getCourtCase());
    }

    @Test
    void givenHearingToUpdateWithCaseMarkers_shouldUpdateCourtCase() {
        var dbHearingDayEntity = HearingDayEntity.builder().hearing(HearingEntity.builder().build()).courtCode("Court-1").build();
        var dbHearing = HearingEntity.builder()
                .courtCase(CourtCaseEntity.builder().caseMarkers(getMutableList(Collections.emptyList())).build())
                .hearingDefendants(Collections.emptyList())
                .hearingType("Trial")
                .hearingEventType(HearingEventType.UNKNOWN)
                .hearingDays(getMutableList(List.of(dbHearingDayEntity)))
                .listNo("1")
                .build();

        var hearingUpdate = HearingEntity.builder()
                .courtCase(CourtCaseEntity.builder().urn("urn-one")
                        .caseMarkers(List.of(CaseMarkerEntity.builder()
                                .typeDescription("description 1")
                                .build()))
                        .build())
                .hearingDefendants(Collections.emptyList())
                .hearingType("Sentenced")
                .hearingDays(getMutableList(List.of(dbHearingDayEntity)))
                .hearingEventType(HearingEventType.CONFIRMED_OR_UPDATED)
                .listNo("2")
                .build();

       var updatedEntity =  dbHearing.update(hearingUpdate);

        assertThat(dbHearing).isEqualTo(hearingUpdate);
        assertThat(hearingUpdate.getCourtCase()).isEqualTo(updatedEntity.getCourtCase());
    }

    @Test
    void givenHearingWithRemovedDefendant_shouldRemoveHearingDefendantFromExistingHearing() {
        var dbHearingDefendant1 = HearingDefendantEntity.builder().hearing(HearingEntity.builder().build()).offences(Collections.emptyList()).defendantId("existing-defendant-1").defendant(DefendantEntity.builder().build()).build();
        var dbHearingDefendant2 = HearingDefendantEntity.builder().hearing(HearingEntity.builder().build()).offences(Collections.emptyList()).defendantId("existing-defendant-2").defendant(DefendantEntity.builder().build()).build();
        var dbHearingEntity = HearingEntity.builder()
                .courtCase(CourtCaseEntity.builder().build())
                .hearingDays(Collections.emptyList())
                .hearingType("Trial")
                .hearingEventType(HearingEventType.UNKNOWN)
                .hearingDefendants(getMutableList(List.of(dbHearingDefendant1, dbHearingDefendant2)))
                .listNo("1")
                .build();

        var hearingUpdate = HearingEntity.builder()
                .courtCase(CourtCaseEntity.builder().build())
                .hearingDays(Collections.emptyList())
                .hearingType("Trial")
                .hearingEventType(HearingEventType.UNKNOWN)
                .hearingDefendants(getMutableList(List.of(dbHearingDefendant1)))
                .listNo("1")
                .build();

        dbHearingEntity.update(hearingUpdate);

        assertThat(dbHearingEntity.getHearingDefendants()).isEqualTo(List.of(dbHearingDefendant1));
        assertThat(dbHearingDefendant2.getHearing()).isNull();
    }

    @Test
    void givenHearingWithNewDefendant_addNewHearingDefendant_shouldAddNewHearingDefendant() {
        var dbHearingEntity = HearingEntity.builder()
                .courtCase(CourtCaseEntity.builder().build())
                .hearingDays(Collections.emptyList())
                .hearingType("Trial")
                .hearingEventType(HearingEventType.UNKNOWN)
                .listNo("1")
                .build();

        var hearingUpdate = HearingEntity.builder()
                .courtCase(CourtCaseEntity.builder().build())
                .hearingDays(Collections.emptyList())
                .hearingType("Trial")
                .hearingEventType(HearingEventType.UNKNOWN)
                .listNo("1")
                .build();

        var dbHearingDefendant1 = HearingDefendantEntity.builder().offences(Collections.emptyList()).hearing(dbHearingEntity).defendantId("existing-defendant-1").defendant(DefendantEntity.builder().build()).build();
        var newHearingDefendant = HearingDefendantEntity.builder().offences(Collections.emptyList()).defendantId("existing-defendant-2").defendant(DefendantEntity.builder().build()).build();

        dbHearingEntity = dbHearingEntity.withHearingDefendants(getMutableList(List.of(dbHearingDefendant1)));
        hearingUpdate = hearingUpdate.withHearingDefendants(getMutableList(List.of(dbHearingDefendant1, newHearingDefendant)));

        dbHearingEntity.update(hearingUpdate);

        assertThat(dbHearingEntity.getHearingDefendants()).isEqualTo(getMutableList(List.of(dbHearingDefendant1, newHearingDefendant)));
        assertThat(newHearingDefendant.getHearing()).isEqualTo(dbHearingEntity);
    }

    @Test
    void givenHearingOutcomeType_shouldAddToHearing() {
        var hearingEntity = HearingEntity.builder().build();
        hearingEntity.addHearingOutcome(HearingOutcomeType.REPORT_REQUESTED);
        assertThat(hearingEntity.getHearingOutcome().getOutcomeType()).isEqualTo(HearingOutcomeType.REPORT_REQUESTED.name());
        assertThat(hearingEntity.getHearingOutcome().getOutcomeDate()).isNotNull();
    }
}
