package uk.gov.justice.probation.courtcaseservice.service.mapper;

import java.util.List;
import java.util.Objects;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import uk.gov.justice.probation.courtcaseservice.jpa.entity.BaseImmutableEntity;
import uk.gov.justice.probation.courtcaseservice.jpa.entity.CourtCaseEntity;
import uk.gov.justice.probation.courtcaseservice.jpa.entity.DefendantEntity;
import uk.gov.justice.probation.courtcaseservice.jpa.entity.EntityHelper;
import uk.gov.justice.probation.courtcaseservice.jpa.entity.NamePropertiesEntity;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertNotSame;
import static uk.gov.justice.probation.courtcaseservice.jpa.entity.EntityHelper.CASE_ID;
import static uk.gov.justice.probation.courtcaseservice.jpa.entity.EntityHelper.CASE_NO;
import static uk.gov.justice.probation.courtcaseservice.jpa.entity.EntityHelper.COURT_CODE;
import static uk.gov.justice.probation.courtcaseservice.jpa.entity.EntityHelper.COURT_ROOM;
import static uk.gov.justice.probation.courtcaseservice.jpa.entity.EntityHelper.CRN;
import static uk.gov.justice.probation.courtcaseservice.jpa.entity.EntityHelper.DEFENDANT_ADDRESS;
import static uk.gov.justice.probation.courtcaseservice.jpa.entity.EntityHelper.DEFENDANT_ID;
import static uk.gov.justice.probation.courtcaseservice.jpa.entity.EntityHelper.OFFENCE_SUMMARY;
import static uk.gov.justice.probation.courtcaseservice.jpa.entity.EntityHelper.OFFENCE_TITLE;
import static uk.gov.justice.probation.courtcaseservice.jpa.entity.EntityHelper.SESSION_START_TIME;
import static uk.gov.justice.probation.courtcaseservice.jpa.entity.EntityHelper.SOURCE;
import static uk.gov.justice.probation.courtcaseservice.jpa.entity.EntityHelper.aHearingEntity;
import static uk.gov.justice.probation.courtcaseservice.jpa.entity.EntityHelper.anOffence;

class CourtCaseMapperTest {

    @Test
    void givenUpdateForSameCrn_whenCreateNewCourtCase_thenChangeProbationStatus() {

        var defendantEntity1 = EntityHelper.aDefendantEntity();
        var defendantEntity2 = DefendantEntity.builder().crn("X99999").probationStatus(null).build();
        var existingCourtCase = CourtCaseEntity.builder().caseId(CASE_ID)
            .caseNo(CASE_NO)
            .courtCode(COURT_CODE)
            .sourceType(SOURCE)
            .defendants(List.of(defendantEntity1, defendantEntity2))
            .offences(List.of(EntityHelper.anOffence()))
            .hearings(List.of(EntityHelper.aHearingEntity(), EntityHelper.aHearingEntity(SESSION_START_TIME.plusMinutes(120))))
            .build();

        var newEntity = CourtCaseMapper.create(existingCourtCase, CRN, "CURRENT");

        assertNotSame(newEntity, existingCourtCase);
        assertThat(newEntity.getDefendants()).hasSize(2);
        assertThat(newEntity.getProbationStatus()).isEqualTo("CURRENT");
        assertThat(newEntity.getSourceType()).isEqualTo(SOURCE);
        assertThat(newEntity.getCourtCode()).isEqualTo(COURT_CODE);
        assertThat(newEntity.getOffences()).hasSize(1);
        assertThat(newEntity.getHearings()).hasSize(2);

        newEntity.getDefendants()
            .stream()
            .filter(defendantEntity -> defendantEntity.getCrn().equals(CRN))
            .findFirst()
            .ifPresentOrElse((entity) -> assertThat(entity.getProbationStatus()).isEqualTo("CURRENT"), Assertions::fail);
        // The other defendant is untouched
        newEntity.getDefendants()
            .stream()
            .filter(defendantEntity -> defendantEntity.getCrn().equals("X99999"))
            .findFirst()
            .ifPresentOrElse((entity) -> assertThat(entity.getProbationStatus()).isNull(), Assertions::fail);
    }

    @Test
    void givenUpdateForOtherCrn_whenCreateDefendant_thenPopulateAllFieldsLeaveCrn() {

        var defendant = EntityHelper.aDefendantEntity();

        var newEntity = CourtCaseMapper.createDefendant(defendant, "OTHER_CRN", "CURRENT");

        assertThat(newEntity).isNotSameAs(defendant);
        assertThat(newEntity.getProbationStatus()).isNotEqualTo("CURRENT");
        assertThat(newEntity.getDefendantId()).isEqualTo(defendant.getDefendantId());
        assertThat(newEntity.getId()).isNull();
        assertThat(newEntity.getOffences()).hasSize(1);
        assertThat(newEntity.getDefendantName()).isEqualTo(defendant.getDefendantName());
        assertThat(newEntity.getName()).isEqualTo(defendant.getName());
        assertThat(newEntity.getDefendantId()).isEqualTo(defendant.getDefendantId());
        assertThat(newEntity.getType()).isEqualTo(defendant.getType());
        assertThat(newEntity.getAddress()).isEqualTo(defendant.getAddress());
        assertThat(newEntity.getCrn()).isEqualTo(defendant.getCrn());
        assertThat(newEntity.getPnc()).isEqualTo(defendant.getPnc());
        assertThat(newEntity.getCro()).isEqualTo(defendant.getCro());
        assertThat(newEntity.getDateOfBirth()).isEqualTo(defendant.getDateOfBirth());
        assertThat(newEntity.getSex()).isEqualTo(defendant.getSex());
        assertThat(newEntity.getNationality1()).isEqualTo(defendant.getNationality1());
        assertThat(newEntity.getNationality2()).isEqualTo(defendant.getNationality2());
        assertThat(newEntity.getPreviouslyKnownTerminationDate()).isEqualTo(defendant.getPreviouslyKnownTerminationDate());
        assertThat(newEntity.getSuspendedSentenceOrder()).isEqualTo(defendant.getSuspendedSentenceOrder());
        assertThat(newEntity.getBreach()).isEqualTo(defendant.getBreach());
        assertThat(newEntity.getPreSentenceActivity()).isEqualTo(defendant.getPreSentenceActivity());
        assertThat(newEntity.getAwaitingPsr()).isEqualTo(defendant.getAwaitingPsr());
        assertThat(newEntity.isManualUpdate()).isEqualTo(defendant.isManualUpdate());
    }

    @Test
    void givenUpdateForSameCrn_whenCreateNew_thenPopulateAllFieldsAndUpdateProbationStatus() {

        var defendant = EntityHelper.aDefendantEntity();

        var newEntity = CourtCaseMapper.createDefendant(defendant, CRN, "CURRENT");

        assertThat(newEntity).isNotSameAs(defendant);
        assertThat(newEntity.getProbationStatus()).isEqualTo("CURRENT");
    }

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

        // Update comes in with a new CRN and name
        var newName = NamePropertiesEntity.builder().surname("STUBBS").forename1("Una").build();
        var updatedDefendant = EntityHelper.aDefendantEntity(DEFENDANT_ADDRESS, newName)
            .withCrn("D99999");
        var updatedEntity = CourtCaseEntity.builder().courtCode("B33HU")
            .defendants(List.of(updatedDefendant))
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
            .withHearings(List.of(aHearingEntity().withId(100L), aHearingEntity(SESSION_START_TIME.plusDays(1)).withId(101L)));

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

        // Update comes in with a new CRN and name and one hearing
        var newName = NamePropertiesEntity.builder().surname("STUBBS").forename1("Una").build();
        var updatedDefendant = EntityHelper.aDefendantEntity(DEFENDANT_ADDRESS, newName)
            .withCrn("D99999");
        var updatedEntity = CourtCaseEntity.builder().courtCode("B33HU")
            .defendants(List.of(updatedDefendant))
            .offences(List.of(anOffence(), anOffence()))
            .caseId(CASE_ID)
            .build();

        // Existing has defendants and hearings, all with ids which must be removed
        var existingUpdatedDefendant = EntityHelper.aDefendantEntity()
            .withId(101L);
        var existingCourtCaseEntity = EntityHelper.aCourtCaseEntity(CASE_ID)
            .withDefendants(List.of(existingUpdatedDefendant))
            .withHearings(List.of(aHearingEntity().withId(100L), aHearingEntity(SESSION_START_TIME.plusDays(1)).withId(101L)));

        var newEntity = CourtCaseMapper.mergeDefendantsOnCase(existingCourtCaseEntity, updatedEntity, DEFENDANT_ID);

        checkCollection(newEntity.getHearings(), newEntity);
        checkCollection(newEntity.getOffences(), newEntity);

        assertThat(newEntity.getDefendants()).hasSize(1);
        assertThat(newEntity.getDefendants()).extracting("crn").containsExactly("D99999");
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
