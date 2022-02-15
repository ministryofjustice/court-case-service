package uk.gov.justice.probation.courtcaseservice.jpa.entity;

import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.AssertionsForClassTypes.assertThat;
import static uk.gov.justice.probation.courtcaseservice.jpa.entity.EntityHelper.DEFENDANT_ID;

class HearingEntityTest {

    private final HearingEntity courtCaseEntity =
        HearingEntity.builder()
                    .defendants(List.of(DefendantEntity.builder()
                                            .defendantId("abc")
                                            .build(),
                                        DefendantEntity.builder()
                                            .defendantId(DEFENDANT_ID)
                                            .build()))
                    .build();

    @Test
    void givenHearingWithDefendants_thenReturn() {
        var defendant = courtCaseEntity.getDefendant(DEFENDANT_ID);
        assertThat(defendant.getDefendantId()).isEqualTo(DEFENDANT_ID);
    }

    @Test
    void givenHearingWithDefendants_whenRequestWrongId_thenReturnNull() {
        assertThat(courtCaseEntity.getDefendant("XXX")).isNull();
    }

    @Test
    void givenHearingWithNoDefendants_thenReturnNull() {
        var courtCase = HearingEntity.builder().build();

        assertThat(courtCase.getDefendant("XXX")).isNull();
    }

}
