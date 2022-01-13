package uk.gov.justice.probation.courtcaseservice.service.mapper;

import org.junit.jupiter.api.Test;
import uk.gov.justice.probation.courtcaseservice.jpa.entity.BaseImmutableEntity;
import uk.gov.justice.probation.courtcaseservice.jpa.entity.CourtCaseEntity;
import uk.gov.justice.probation.courtcaseservice.jpa.entity.EntityHelper;
import uk.gov.justice.probation.courtcaseservice.jpa.entity.NamePropertiesEntity;
import uk.gov.justice.probation.courtcaseservice.jpa.entity.OffenderEntity;

import java.util.List;
import java.util.Objects;

import static org.assertj.core.api.Assertions.assertThat;
import static uk.gov.justice.probation.courtcaseservice.jpa.entity.EntityHelper.CASE_ID;
import static uk.gov.justice.probation.courtcaseservice.jpa.entity.EntityHelper.COURT_ROOM;
import static uk.gov.justice.probation.courtcaseservice.jpa.entity.EntityHelper.CRN;
import static uk.gov.justice.probation.courtcaseservice.jpa.entity.EntityHelper.DEFENDANT_ADDRESS;
import static uk.gov.justice.probation.courtcaseservice.jpa.entity.EntityHelper.DEFENDANT_ID;
import static uk.gov.justice.probation.courtcaseservice.jpa.entity.EntityHelper.OFFENCE_SUMMARY;
import static uk.gov.justice.probation.courtcaseservice.jpa.entity.EntityHelper.OFFENCE_TITLE;
import static uk.gov.justice.probation.courtcaseservice.jpa.entity.EntityHelper.SESSION_START_TIME;
import static uk.gov.justice.probation.courtcaseservice.jpa.entity.EntityHelper.aHearingEntity;
import static uk.gov.justice.probation.courtcaseservice.jpa.entity.EntityHelper.anOffence;

class CourtCaseMapperTest {
    @Test
    void whenCreateDefendantOffence_thenReturnNew() {

        var offence = EntityHelper.aDefendantOffence();

        var newEntity = CourtCaseMapper.createDefendantOffence(offence);

        assertThat(newEntity).isNotSameAs(offence);
        assertThat(newEntity.getId()).isNull();
        assertThat(newEntity.getTitle()).isEqualTo(OFFENCE_TITLE);
        assertThat(newEntity.getSummary()).isEqualTo(OFFENCE_SUMMARY);
        assertThat(newEntity.getSequence()).isEqualTo(1);
    }

    @Test
    void whenCreateHearings_thenReturnList() {

        var hearingEntity1 = EntityHelper.aHearingEntity();
        var hearingEntity2 = EntityHelper.aHearingEntity(SESSION_START_TIME.plusDays(1));

        var hearings = CourtCaseMapper.createHearings(List.of(hearingEntity1, hearingEntity2));

        assertThat(hearings).hasSize(2);
        assertThat(hearings.get(0)).isNotSameAs(hearingEntity1);
        assertThat(hearings.get(1)).isNotSameAs(hearingEntity2);
        assertThat(hearings).extracting("id").allMatch(Objects::isNull);
        assertThat(hearings).extracting("courtRoom").containsOnly(COURT_ROOM);
    }

    @Test
    void givenNullInput_whenCreateHearings_thenReturnEmptyList() {
        assertThat(CourtCaseMapper.createHearings(null)).isEmpty();
    }

    @Test
    void whenMerge_thenRestoreDefendantsAndHearings() {
        final var hearings = List.of(aHearingEntity().withId(100L), aHearingEntity(SESSION_START_TIME.plusDays(1)).withId(101L));

        // Update comes in with a new CRN and name
        var newName = NamePropertiesEntity.builder().surname("STUBBS").forename1("Una").build();
        var updatedDefendant = EntityHelper.aDefendantEntity(DEFENDANT_ADDRESS, newName)
            .withOffender(OffenderEntity.builder().crn("D99999").build());
        var updatedEntity = CourtCaseEntity.builder()
            .defendants(List.of(updatedDefendant))
            .hearings(hearings)
            .offences(List.of(anOffence(), anOffence()))
            .caseId(CASE_ID)
            .build();

        // Existing has defendants and hearings, all with ids which must be removed
        var existingNonUpdatedDefendant = EntityHelper.aDefendantEntity()
            .withDefendantId("904aeafa-b3db-41be-99ba-b2fbbf344d8b")
            .withId(100L);
        var existingUpdatedDefendant = EntityHelper.aDefendantEntity()
            .withId(101L);
        var existingCourtCaseEntity = EntityHelper.aCourtCaseEntity(CASE_ID)
            .withDefendants(List.of(existingUpdatedDefendant, existingNonUpdatedDefendant))
            .withHearings(hearings);

        var newEntity = CourtCaseMapper.mergeDefendantsOnCase(existingCourtCaseEntity, updatedEntity, DEFENDANT_ID);

        checkCollection(newEntity.getHearings(), newEntity);
        checkCollection(newEntity.getOffences(), newEntity);
        checkCollection(newEntity.getDefendants(), newEntity);

        assertThat(newEntity.getDefendants()).extracting("crn").containsExactlyInAnyOrder("D99999", CRN);
        assertThat(newEntity.getDefendants()).filteredOn(d -> d.getCrn().equals("D99999"))
            .allSatisfy(defendant -> {
                assertThat(defendant.getName().getSurname()).isEqualTo("STUBBS");
                assertThat(defendant.getName().getForename1()).isEqualTo("Una");
                assertThat(defendant.getCourtCase()).isSameAs(newEntity);
            });
        assertThat(newEntity.getDefendants()).filteredOn(d -> d.getCrn().equals("X340906"))
            .allSatisfy(defendant -> {
                assertThat(defendant.getName().getSurname()).isEqualTo("BENNETT");
                assertThat(defendant.getName().getForename1()).isEqualTo("Gordon");
                assertThat(defendant.getCourtCase()).isSameAs(newEntity);
                var offences = defendant.getOffences();
                assertThat(offences.get(0).getDefendant()).isSameAs(defendant);
            });
    }

    @Test
    void givenExistingCaseWithSingleDefendant_whenMerge_thenRestoreHearings() {
        final var hearings = List.of(aHearingEntity().withId(100L), aHearingEntity(SESSION_START_TIME.plusDays(1)).withId(101L));

        // Update comes in with a new CRN and name and one hearing
        var newName = NamePropertiesEntity.builder().surname("STUBBS").forename1("Una").build();
        var updatedDefendant = EntityHelper.aDefendantEntity(DEFENDANT_ADDRESS, newName);
        var updatedEntity = CourtCaseEntity.builder()
            .defendants(List.of(updatedDefendant))
            .offences(List.of(anOffence(), anOffence()))
            .caseId(CASE_ID)
            .build();

        // Existing has defendants and hearings, all with ids which must be removed
        var existingUpdatedDefendant = EntityHelper.aDefendantEntity()
            .withId(101L);
        var existingCourtCaseEntity = EntityHelper.aCourtCaseEntity(CASE_ID)
            .withDefendants(List.of(existingUpdatedDefendant))
            .withHearings(hearings);

        var newEntity = CourtCaseMapper.mergeDefendantsOnCase(existingCourtCaseEntity, updatedEntity, DEFENDANT_ID);

        checkCollection(newEntity.getHearings(), newEntity);
        checkCollection(newEntity.getOffences(), newEntity);

        assertThat(newEntity.getDefendants()).hasSize(1);
        assertThat(newEntity.getDefendants()).extracting("crn").containsExactly(CRN);
        assertThat(newEntity.getDefendants().get(0).getName().getForename1()).isEqualTo("Una");
        assertThat(newEntity.getDefendants().get(0).getName().getSurname()).isEqualTo("STUBBS");
        assertThat(newEntity.getDefendants().get(0).getCourtCase()).isSameAs(newEntity);
    }

    private void checkCollection(List<? extends BaseImmutableEntity> childEntities, CourtCaseEntity courtCase) {
        assertThat(childEntities).hasSize(2);
        assertThat(childEntities).extracting("id").allMatch(Objects::isNull);
        assertThat(childEntities).extracting("courtCase").containsExactlyInAnyOrder(courtCase, courtCase);
    }
}
