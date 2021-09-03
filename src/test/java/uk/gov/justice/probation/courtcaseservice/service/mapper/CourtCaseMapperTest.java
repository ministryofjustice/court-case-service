package uk.gov.justice.probation.courtcaseservice.service.mapper;

import org.junit.jupiter.api.Test;
import uk.gov.justice.probation.courtcaseservice.jpa.entity.EntityHelper;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertNotSame;
import static uk.gov.justice.probation.courtcaseservice.jpa.entity.EntityHelper.COURT_CODE;
import static uk.gov.justice.probation.courtcaseservice.jpa.entity.EntityHelper.CRN;
import static uk.gov.justice.probation.courtcaseservice.jpa.entity.EntityHelper.OFFENCE_TITLE;
import static uk.gov.justice.probation.courtcaseservice.jpa.entity.EntityHelper.SOURCE;

class CourtCaseMapperTest {

    @Test
    public void givenSimpleEntity_whenCreateNewThenChangeProbationStatus() {

        var existingCourtCase = EntityHelper.aCourtCaseEntity(CRN);

        var newEntity = CourtCaseMapper.create(existingCourtCase, "Current");

        assertNotSame(newEntity, existingCourtCase);
        assertThat(newEntity.getProbationStatus()).isEqualTo("Current");
        assertThat(newEntity.getSourceType()).isEqualTo(SOURCE);
        assertThat(newEntity.getCourtCode()).isEqualTo(COURT_CODE);
        assertThat(newEntity.getOffences()).hasSize(1);

        var newOffence = newEntity.getOffences().get(0);
        assertThat(newOffence.getOffenceTitle()).isEqualTo(OFFENCE_TITLE);
        assertThat(newOffence.getCourtCase()).isSameAs(newEntity);
    }

}
