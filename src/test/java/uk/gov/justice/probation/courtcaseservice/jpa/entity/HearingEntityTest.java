package uk.gov.justice.probation.courtcaseservice.jpa.entity;

import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.AssertionsForClassTypes.assertThat;
import static uk.gov.justice.probation.courtcaseservice.jpa.entity.EntityHelper.DEFENDANT_ID;

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

}
