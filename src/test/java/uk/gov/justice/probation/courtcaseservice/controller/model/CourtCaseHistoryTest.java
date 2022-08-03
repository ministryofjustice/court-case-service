package uk.gov.justice.probation.courtcaseservice.controller.model;

import org.junit.jupiter.api.Test;
import uk.gov.justice.probation.courtcaseservice.jpa.entity.CourtCaseEntity;
import uk.gov.justice.probation.courtcaseservice.jpa.entity.DefendantEntity;
import uk.gov.justice.probation.courtcaseservice.jpa.entity.EntityHelper;
import uk.gov.justice.probation.courtcaseservice.jpa.entity.SourceType;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static uk.gov.justice.probation.courtcaseservice.jpa.entity.EntityHelper.aHearingEntity;

class CourtCaseHistoryTest {

    @Test
    void shouldMapToCourtCaseHistory() {
        String caseId = "1b45408c-e453-4319-908b-f80700c253ec";
        var testCourtCase = CourtCaseEntity.builder()
            .caseId(caseId)
            .caseNo("test-case-no")
            .urn("test-urn")
            .sourceType(SourceType.LIBRA)
            .hearings(List.of(aHearingEntity(caseId).withHearingId("hearingIdOne"),
                aHearingEntity(caseId).withHearingId("hearingIdTwo"),
                aHearingEntity(caseId).withHearingId("hearingIdOne"),
                aHearingEntity(caseId).withHearingId("hearingIdTwo"),
                aHearingEntity(caseId).withHearingId("hearingIdThree"))
            ).build();

        List<DefendantEntity> defendantEntities = List.of(EntityHelper.aDefendantEntity("defendant-id-one", "crn-one"),
            EntityHelper.aDefendantEntity("defendant-id-two", "crn-two"));

        var actual = CourtCaseHistory.of(testCourtCase, defendantEntities);

        assertThat(actual.getCaseId()).isEqualTo(testCourtCase.getCaseId());
        assertThat(actual.getCaseNo()).isEqualTo(testCourtCase.getCaseNo());
        assertThat(actual.getSource()).isEqualTo(SourceType.LIBRA.name());
        assertThat(actual.getUrn()).isEqualTo(testCourtCase.getUrn());
        assertThat(actual.getDefendants()).isEqualTo(defendantEntities);
        assertThat(actual.getHearings()).hasSize(3);
    }
}