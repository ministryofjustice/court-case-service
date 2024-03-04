package uk.gov.justice.probation.courtcaseservice.jpa.entity;

import org.junit.jupiter.api.Test;

import java.util.Collections;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static uk.gov.justice.probation.courtcaseservice.jpa.entity.EntityHelper.getMutableList;

class CourtCaseEntityTest {
    @Test
    void shouldAddHearingToListOfHearings() {
        HearingEntity existingHearing = HearingEntity.builder().hearingId("h1").build();
        var courtCase = CourtCaseEntity.builder()
                .hearings(getMutableList(List.of(existingHearing)))
                .caseMarkers(List.of(CaseMarkerEntity.builder()
                        .typeDescription("description")
                        .build()))
                .build();
        var newHearing = HearingEntity.builder().build();

        courtCase.addHearing(newHearing);

        assertThat(courtCase.getHearings()).isEqualTo(List.of(existingHearing, newHearing));
        assertThat(newHearing.getCourtCase()).isEqualTo(courtCase);
    }

    @Test
    void shouldUpdateRequiredValuesFromCaseUpdate() {
        var courtCase = CourtCaseEntity.builder()
                .caseMarkers(getMutableList(Collections.emptyList()))
                .build();
        var caseUpdate = CourtCaseEntity.builder().urn("urn-one")
                .caseMarkers(List.of(CaseMarkerEntity.builder()
                                .typeDescription("description 1")
                        .build()))
                .build();

        courtCase.update(caseUpdate);

        assertThat(courtCase.getUrn()).isEqualTo("urn-one");
        assertThat(courtCase.getCaseMarkers()).hasSize(1);
        assertThat(courtCase.getCaseMarkers().get(0).getTypeDescription()).isEqualTo("description 1");

    }

    @Test
    void shouldCreateCaseDocumentCorrectly() {
        // TODO PIC-3683
    }
}