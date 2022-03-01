package uk.gov.justice.probation.courtcaseservice.jpa.entity;

import org.junit.jupiter.api.Test;

import static org.assertj.core.api.AssertionsForClassTypes.assertThat;
import static uk.gov.justice.probation.courtcaseservice.jpa.entity.DefendantProbationStatus.CONFIRMED_NO_RECORD;
import static uk.gov.justice.probation.courtcaseservice.jpa.entity.DefendantProbationStatus.UNCONFIRMED_NO_RECORD;


class HearingDefendantEntityTest {

    @Test
    void givenStandard_whenGetSurname_thenReturn() {
        var defendantEntity = HearingDefendantEntity.builder().defendantName("Mr Jeff BRIDGES").build();

        assertThat(defendantEntity.getDefendantSurname()).isEqualTo("BRIDGES");
    }

    @Test
    void givenSingleString_whenGetSurname_thenReturn() {
        var defendantEntity = HearingDefendantEntity.builder().defendantName("BRIDGES").build();

        assertThat(defendantEntity.getDefendantSurname()).isEqualTo("BRIDGES");
    }

    @Test
    void givenNullDefendantName_whenGetSurname_thenReturnEmptyString() {
        var defendantEntity = HearingDefendantEntity.builder().build();
        assertThat(defendantEntity.getDefendantSurname()).isEmpty();
    }

    @Test
    void givenEmptyDefendantName_whenGetSurname_thenReturnEmptyString() {
        var defendantEntity = HearingDefendantEntity.builder().defendantName("   ").build();
        assertThat(defendantEntity.getDefendantSurname()).isEmpty();
    }

    @Test
    void givenNullOffender_andNoOffenderConfirmed_whenGetProbationStatus_thenReturnUnconfirmedNoRecord(){
        final HearingDefendantEntity defendant = HearingDefendantEntity.builder()
                .offender(null)
                .offenderConfirmed(false)
                .build();

        assertThat(defendant.getProbationStatusForDisplay()).isEqualTo(UNCONFIRMED_NO_RECORD);
    }

    @Test
    void givenNullOffender_andOffenderConfirmed_whenGetProbationStatus_thenReturnConfirmedNoRecord(){
        final HearingDefendantEntity defendant = HearingDefendantEntity.builder()
                .offender(null)
                .offenderConfirmed(true)
                .build();

        assertThat(defendant.getProbationStatusForDisplay()).isEqualTo(CONFIRMED_NO_RECORD);
    }

    @Test
    void givenOffender_whenGetProbationStatus_thenReturnOffenderProbationStatus(){
        final HearingDefendantEntity defendant = HearingDefendantEntity.builder()
                .offender(OffenderEntity.builder()
                        .probationStatus(OffenderProbationStatus.CURRENT)
                        .build())
                .build();

        assertThat(defendant.getProbationStatusForDisplay()).isEqualTo(DefendantProbationStatus.CURRENT);
    }

}
