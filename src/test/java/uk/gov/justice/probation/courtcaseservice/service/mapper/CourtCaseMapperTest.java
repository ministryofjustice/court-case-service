package uk.gov.justice.probation.courtcaseservice.service.mapper;

import org.junit.jupiter.api.Test;
import uk.gov.justice.probation.courtcaseservice.jpa.entity.BaseImmutableEntity;
import uk.gov.justice.probation.courtcaseservice.jpa.entity.CourtCaseEntity;
import uk.gov.justice.probation.courtcaseservice.jpa.entity.EntityHelper;
import uk.gov.justice.probation.courtcaseservice.jpa.entity.HearingDefendantEntity;
import uk.gov.justice.probation.courtcaseservice.jpa.entity.HearingEntity;
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
        final var hearingDays = List.of(aHearingEntity().withId(100L), aHearingEntity(SESSION_START_TIME.plusDays(1)).withId(101L));

        // Update comes in with a new CRN and name
        var newName = NamePropertiesEntity.builder().surname("STUBBS").forename1("Una").build();
        var updatedHearingDefendant = EntityHelper.aHearingDefendant(newName, OffenderEntity.builder().crn("D99999").build());
        var updatedEntity = HearingEntity.builder()
                .hearingDefendants(List.of(updatedHearingDefendant))
                .hearingDays(hearingDays)
                .courtCase(CourtCaseEntity.builder()
                        .caseId(CASE_ID)
                        .build())
                .build();

        // Existing has defendants and hearings, all with ids which must be removed
        var existingNonUpdatedDefendant = EntityHelper.aDefendantEntity(100L, "904aeafa-b3db-41be-99ba-b2fbbf344d8b");
        var existingUpdatedDefendant = EntityHelper.aDefendantEntity(101L, DEFENDANT_ID);
        var existingCourtCaseEntity = EntityHelper.aHearingEntity(CASE_ID)
            .withHearingDefendants(List.of(existingUpdatedDefendant, existingNonUpdatedDefendant))
            .withHearingDays(hearingDays);

        var newEntity = CourtCaseMapper.mergeDefendantsOnHearing(existingCourtCaseEntity, updatedEntity, DEFENDANT_ID);

        checkCollection(newEntity.getHearingDays(), newEntity);
        checkCollection(newEntity.getHearingDefendants(), newEntity);

        assertThat(newEntity.getHearingDefendants()).extracting("crn").containsExactlyInAnyOrder("D99999", CRN);
        assertThat(newEntity.getHearingDefendants()).map(HearingDefendantEntity::getDefendant).filteredOn(d -> d.getCrn().equals("D99999"))
            .allSatisfy(defendant -> {
                assertThat(defendant.getName().getSurname()).isEqualTo("STUBBS");
                assertThat(defendant.getName().getForename1()).isEqualTo("Una");
            });
        assertThat(newEntity.getHearingDefendants()).filteredOn(d -> d.getCrn().equals("X340906"))
            .allSatisfy(defendant -> {
                assertThat(defendant.getDefendant().getName().getSurname()).isEqualTo("BENNETT");
                assertThat(defendant.getDefendant().getName().getForename1()).isEqualTo("Gordon");
                assertThat(defendant.getHearing()).isSameAs(newEntity);
                var offences = defendant.getOffences();
                assertThat(offences.get(0).getHearingDefendant()).isSameAs(defendant);
            });
    }

    @Test
    void givenExistingCaseWithSingleDefendant_whenMerge_thenRestoreHearings() {
        final var hearings = List.of(aHearingEntity().withId(100L), aHearingEntity(SESSION_START_TIME.plusDays(1)).withId(101L));

        // Update comes in with a new CRN and name and one hearing
        var newName = NamePropertiesEntity.builder().surname("STUBBS").forename1("Una").build();
        var updatedDefendant = EntityHelper.aDefendantEntity(DEFENDANT_ADDRESS, newName);
        var updatedEntity = HearingEntity.builder()
            .hearingDefendants(List.of(updatedDefendant))
            .courtCase(CourtCaseEntity.builder()
                .caseId(CASE_ID)
            .build())
            .build();

        // Existing has defendants and hearings, all with ids which must be removed
        var existingUpdatedDefendant = EntityHelper.aDefendantEntity()
            .withId(101L);
        var existingCourtCaseEntity = EntityHelper.aHearingEntity(CASE_ID)
            .withHearingDefendants(List.of(existingUpdatedDefendant))
            .withHearingDays(hearings);

        var newEntity = CourtCaseMapper.mergeDefendantsOnHearing(existingCourtCaseEntity, updatedEntity, DEFENDANT_ID);

        checkCollection(newEntity.getHearingDays(), newEntity);

        assertThat(newEntity.getHearingDefendants()).hasSize(1);
        assertThat(newEntity.getHearingDefendants()).extracting("crn").containsExactly(CRN);
        assertThat(newEntity.getHearingDefendants().get(0).getDefendant().getName().getForename1()).isEqualTo("Una");
        assertThat(newEntity.getHearingDefendants().get(0).getDefendant().getName().getSurname()).isEqualTo("STUBBS");
        assertThat(newEntity.getHearingDefendants().get(0).getHearing()).isSameAs(newEntity);
    }

    private void checkCollection(List<? extends BaseImmutableEntity> childEntities, HearingEntity hearing) {
        assertThat(childEntities).hasSize(2);
        assertThat(childEntities).extracting("id").allMatch(Objects::isNull);
        assertThat(childEntities).extracting("hearing").containsExactlyInAnyOrder(hearing, hearing);
    }
}
