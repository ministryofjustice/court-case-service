package uk.gov.justice.probation.courtcaseservice.jpa.entity;

import java.util.List;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.AssertionsForClassTypes.assertThat;
import static uk.gov.justice.probation.courtcaseservice.jpa.entity.EntityHelper.DEFENDANT_ID;

class CourtCaseEntityTest {

    private final CourtCaseEntity courtCaseEntity =
        CourtCaseEntity.builder()
                    .defendants(List.of(DefendantEntity.builder()
                                            .defendantId("abc")
                                            .build(),
                                        DefendantEntity.builder()
                                            .defendantId(DEFENDANT_ID)
                                            .build()))
                    .build();

    @Test
    void givenCaseWithDefendants_thenReturn() {
        var defendant = courtCaseEntity.getDefendant(DEFENDANT_ID);
        assertThat(defendant.getDefendantId()).isEqualTo(DEFENDANT_ID);
    }

    @Test
    void givenCaseWithDefendants_whenRequestWrongId_thenReturnNull() {
        assertThat(courtCaseEntity.getDefendant("XXX")).isNull();
    }

    @Test
    void givenCaseWithNoDefendants_thenReturnNull() {
        var courtCase = CourtCaseEntity.builder().build();

        assertThat(courtCase.getDefendant("XXX")).isNull();
    }

}
