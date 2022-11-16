package uk.gov.justice.probation.courtcaseservice.jpa.entity;

import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static uk.gov.justice.probation.courtcaseservice.jpa.entity.EntityHelper.getMutableList;

class CourtCaseEntityTest {
    @Test
    void shouldAddHearingToListOfHearings() {
        HearingEntity existingHearing = HearingEntity.builder().hearingId("h1").build();
        var courtCase = CourtCaseEntity.builder()
            .hearings(getMutableList(List.of(existingHearing)))
            .build();
        var newHearing = HearingEntity.builder().build();

        courtCase.addHearing(newHearing);

        assertThat(courtCase.getHearings()).isEqualTo(List.of(existingHearing, newHearing));
        assertThat(newHearing.getCourtCase()).isEqualTo(courtCase);
    }

    @Test
    void shouldUpdateRequiredValuesFromCaseUpdate() {
        var courtCase = CourtCaseEntity.builder()
            .build();
        var caseUpdate = CourtCaseEntity.builder().urn("urn-one").build();

        courtCase.update(caseUpdate);

        assertThat(courtCase.getUrn()).isEqualTo("urn-one");
    }
}