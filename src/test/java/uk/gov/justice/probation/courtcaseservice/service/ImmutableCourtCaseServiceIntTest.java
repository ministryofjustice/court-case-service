package uk.gov.justice.probation.courtcaseservice.service;

import groovy.lang.Tuple3;
import jakarta.persistence.EntityManager;
import org.jetbrains.annotations.NotNull;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.dao.CannotAcquireLockException;
import org.springframework.test.context.jdbc.Sql;
import org.springframework.test.context.jdbc.SqlConfig;
import uk.gov.justice.probation.courtcaseservice.BaseIntTest;
import uk.gov.justice.probation.courtcaseservice.controller.model.CourtCaseResponse;
import uk.gov.justice.probation.courtcaseservice.controller.model.HearingSearchRequest;
import uk.gov.justice.probation.courtcaseservice.jpa.entity.*;
import uk.gov.justice.probation.courtcaseservice.jpa.repository.CourtRepository;
import uk.gov.justice.probation.courtcaseservice.jpa.repository.OffenderRepository;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.AssertionsForClassTypes.assertThatExceptionOfType;
import static org.mockito.Mockito.*;
import static org.springframework.test.context.jdbc.Sql.ExecutionPhase.AFTER_TEST_METHOD;
import static org.springframework.test.context.jdbc.Sql.ExecutionPhase.BEFORE_TEST_METHOD;
import static org.springframework.test.context.jdbc.SqlConfig.TransactionMode.ISOLATED;

@Sql(scripts = { "classpath:sql/before-common.sql", "classpath:sql/before-new-hearing-search.sql" }, config = @SqlConfig(transactionMode = ISOLATED), executionPhase = BEFORE_TEST_METHOD)
@Sql(scripts = "classpath:after-test.sql", config = @SqlConfig(transactionMode = ISOLATED), executionPhase = AFTER_TEST_METHOD)
public class ImmutableCourtCaseServiceIntTest extends BaseIntTest {
    private static final String CRN = "CRN";
    private static final HearingEntity COURT_CASE_ENTITY = EntityHelper.aHearingEntityWithCrn(CRN);
    private static final String COURT_CODE = "B10JQ";
    public static final String DEFENDANT_ID_2 = "9b165f40-ecc1-4cf1-bb69-41504de8c0d5";
    @Autowired
    private ImmutableCourtCaseService courtCaseService;
    @MockBean
    private CourtRepository courtRepository;
    @Autowired
    OffenderRepository offenderRepository;

    @Autowired
    EntityManager entityManager;

    @Test
    public void givenCannotAcquireLockExceptionThrown_whenCreateCase_thenRetry() {
        when(courtRepository.findByCourtCode(COURT_CODE)).thenThrow(CannotAcquireLockException.class);
        assertThatExceptionOfType(CannotAcquireLockException.class)
                .isThrownBy(() -> courtCaseService.createHearing("1234", COURT_CASE_ENTITY));

        verify(courtRepository, times(3)).findByCourtCode(COURT_CODE);
    }

    String DEFENDANT_ID_1 = "8e0263a1-bc3e-4dad-93c4-d333d32389ea";

    @Test
    public void givenNewCase_createOrUpdateHearing_createNewCase() {
        var caseId = "fd907836-324f-42a2-8a7a-e3d956b9d1e7";
        var hearingId = "f069bfcd-29d6-4ab0-82f4-5df1ffd47f33";
        final var newHearingEntity = EntityHelper.aHearingEntityWithHearingId(caseId, hearingId, DEFENDANT_ID_1);

        HearingEntity savedHearing = courtCaseService.createOrUpdateHearingByHearingId(newHearingEntity.getHearingId(), newHearingEntity).block();
        assertThat(savedHearing.withId(null)).isEqualTo(newHearingEntity.withId(null));
        assertThat(savedHearing.getCourtCaseId()).isEqualTo(newHearingEntity.getCourtCaseId());

        assertThatHearingIsNotImmutable(caseId, hearingId, DEFENDANT_ID_1);
        var hearingAudits = findAllAuditByHearingId(hearingId);
        assertThat(hearingAudits).hasSize(1);
    }
    @Test
    public void givenNewCase_createOrUpdateHearing_createNewCase_givenAFollowingUpdate_mutateTheExistingCase() {
        var caseId = "fd907836-324f-42a2-8a7a-e3d956b9d1e7";
        var hearingId = "f069bfcd-29d6-4ab0-82f4-5df1ffd47f33";
        final var newHearingEntity = EntityHelper.aHearingEntityWithHearingId(caseId, hearingId, DEFENDANT_ID_1);

        HearingEntity savedHearing = courtCaseService.createOrUpdateHearingByHearingId(newHearingEntity.getHearingId(), newHearingEntity).block();
        assertThat(savedHearing.withId(null)).isEqualTo(newHearingEntity.withId(null));

        assertThatHearingIsNotImmutable(caseId, hearingId, DEFENDANT_ID_1);
        var hearingAudits = findAllAuditByHearingId(hearingId);
        assertThat(hearingAudits).hasSize(1);

        // update - 1
        HearingEntity hearingUpdate = getHearingUpdate(newHearingEntity);

        HearingEntity savedUpdate = courtCaseService.createOrUpdateHearingByHearingId(hearingUpdate.getHearingId(), hearingUpdate).block();
        assertThat(savedUpdate.withId(null)).isEqualTo(hearingUpdate);

        var updatedData = assertThatHearingIsNotImmutable(caseId, hearingId, DEFENDANT_ID_1);
        assertThat(updatedData.getV1().getHearingEventType()).isEqualTo(HearingEventType.RESULTED);
        assertThat(updatedData.getV3().getDefendantName()).isEqualTo("Mr. Updated Name");

        HearingEntity hearingUpdateTwo = EntityHelper.aHearingEntityWithHearingId(caseId, hearingId, DEFENDANT_ID_1).withHearingEventType(HearingEventType.CONFIRMED_OR_UPDATED);
        EntityHelper.refreshMappings(hearingUpdateTwo);
        var savedUpdateTwo = courtCaseService.createOrUpdateHearingByHearingId(newHearingEntity.getHearingId(), hearingUpdateTwo).block();

        hearingAudits = findAllAuditByHearingId(hearingId);
        assertThat(hearingAudits).hasSize(3);
    }
  @Test
  public void givenNewCaseWithNoDefendantOffender_createOrUpdateHearingWithOffender_shouldSaveOffender() {
        var caseId = "fd907836-324f-42a2-8a7a-e3d956b9d1e7";
        var hearingId = "f069bfcd-29d6-4ab0-82f4-5df1ffd47f33";
        final var newHearingEntity = EntityHelper.aHearingEntityWithHearingId(caseId, hearingId, DEFENDANT_ID_1);
        newHearingEntity.getHearingDefendants().get(0).setDefendant(EntityHelper.aDefendantEntity(DEFENDANT_ID_1, null));

        HearingEntity savedHearing = courtCaseService.createOrUpdateHearingByHearingId(newHearingEntity.getHearingId(), newHearingEntity).block();
        assertThat(savedHearing.withId(null)).isEqualTo(newHearingEntity.withId(null));

        assertThatHearingIsNotImmutable(caseId, hearingId, DEFENDANT_ID_1);
        var hearingAudits = findAllAuditByHearingId(hearingId);
        assertThat(hearingAudits).hasSize(1);

        // update - 1
        HearingEntity hearingUpdate = getHearingUpdate(newHearingEntity);
        DefendantEntity defendantUpdate = EntityHelper.aDefendantEntity(DEFENDANT_ID_1, EntityHelper.CRN).withDefendantName("Mr. Updated Name");
        hearingUpdate.getHearingDefendants().get(0).setDefendant(defendantUpdate);

        HearingEntity savedUpdate = courtCaseService.createOrUpdateHearingByHearingId(hearingUpdate.getHearingId(), hearingUpdate).block();
        assertThat(savedUpdate.withId(null)).isEqualTo(hearingUpdate);
        var dbHearing = assertThatHearingIsNotImmutable(caseId, hearingId, DEFENDANT_ID_1);
        assertThat(dbHearing.getV3().getCrn()).isEqualTo(EntityHelper.CRN);
        assertThat(offenderRepository.findByCrn(EntityHelper.CRN).get().withId(null)).isEqualTo(defendantUpdate.getOffender().withId(null));

        var updatedData = assertThatHearingIsNotImmutable(caseId, hearingId, DEFENDANT_ID_1);
        assertThat(updatedData.getV1().getHearingEventType()).isEqualTo(HearingEventType.RESULTED);
        assertThat(updatedData.getV3().getDefendantName()).isEqualTo("Mr. Updated Name");
    }

    @NotNull
    private HearingEntity getHearingUpdate(HearingEntity hearingEntity) {

        var updatedSession = LocalDateTime.of(2040, 8, 6, 11, 30);
        ArrayList<HearingDayEntity> updatedHearingDays = EntityHelper.getMutableList(List.of(HearingDayEntity.builder()
            .day(updatedSession.toLocalDate())
            .time(updatedSession.toLocalTime())
            .courtRoom("7")
            .courtCode("B33HU")
            .build()));

        var defendant = hearingEntity.getHearingDefendants().get(0).getDefendant();
        var hearingDefendant = EntityHelper.aHearingDefendantEntity(defendant.getDefendantId(), defendant.getCrn())
            .withOffences(List.of(EntityHelper.aDefendantOffence("Updated offence title", 1)))
            .withDefendant(defendant.withDefendantName("Mr. Updated Name"));

        var hearingUpdate = EntityHelper.aHearingEntityWithHearingId(hearingEntity.getCaseId(), hearingEntity.getHearingId(), DEFENDANT_ID_1)
            .withHearingEventType(HearingEventType.RESULTED)
            .withHearingDefendants(EntityHelper.getMutableList(List.of(hearingDefendant)))
            .withHearingDays(updatedHearingDays);
        EntityHelper.refreshMappings(hearingUpdate);
        return hearingUpdate;
    }

    @Test
    public void shouldUpdateHearingWithNewDefendantWhenProvidedWithNewDefendantInHearingUpdate() {
        // Given
        var caseId = "fd907836-324f-42a2-8a7a-e3d956b9d1e7";
        var hearingId = "f069bfcd-29d6-4ab0-82f4-5df1ffd47f33";
        final var newHearingEntity = EntityHelper.aHearingEntityWithHearingId(caseId, hearingId, DEFENDANT_ID_1);

        // create the initial hearing
        HearingEntity savedHearing = courtCaseService.createOrUpdateHearingByHearingId(newHearingEntity.getHearingId(), newHearingEntity).block();
        assertThat(savedHearing.withId(null)).isEqualTo(newHearingEntity.withId(null));
        assertThatHearingIsNotImmutable(caseId, hearingId, DEFENDANT_ID_1);

        // add an additional defendant to the existing hearing
        var hearingEntityWithNewDefendant = EntityHelper.aHearingEntityWithHearingId(caseId, hearingId, DEFENDANT_ID_1);
        HearingDefendantEntity newHearingDefendant = EntityHelper.aHearingDefendantEntity(DEFENDANT_ID_2, null);
        hearingEntityWithNewDefendant.getHearingDefendants().add(newHearingDefendant);

        // When (persist the changes)
        HearingEntity savedUpdate = courtCaseService.createOrUpdateHearingByHearingId(hearingEntityWithNewDefendant.getHearingId(), hearingEntityWithNewDefendant).block();

        // Then
        assertThat(savedUpdate.withId(null)).isEqualTo(hearingEntityWithNewDefendant);
        var updatedData = assertThatHearingIsNotImmutable(caseId, hearingId, DEFENDANT_ID_1);
        HearingEntity hearingAfterUpdate = updatedData.getV1();
        assertThat(hearingAfterUpdate.getHearingDefendants()).hasSize(2);
        assertThat(hearingAfterUpdate.getHearingDefendants()).filteredOn(d -> d.getDefendantId().equals(DEFENDANT_ID_2));
    }

    @Test
    public void givenHearingWithTwoDefendants_followWithAnUpdateWithOneDefendant_shouldRemoveExistingDefendant() {
        var caseId = "fd907836-324f-42a2-8a7a-e3d956b9d1e7";
        var hearingId = "f069bfcd-29d6-4ab0-82f4-5df1ffd47f33";
        final var newHearingEntity = EntityHelper.aHearingEntityWithHearingId(caseId, hearingId, DEFENDANT_ID_1);
        var hearingDefendantEntity = newHearingEntity.getHearingDefendants().get(0);

        var DEFENDANT_ID_2 = "9b165f40-ecc1-4cf1-bb69-41504de8c0d5";
        HearingDefendantEntity hearingDefendant2 = EntityHelper.aHearingDefendantEntity(DEFENDANT_ID_2, null);
        hearingDefendant2.setHearing(newHearingEntity);
        newHearingEntity.getHearingDefendants().add(hearingDefendant2);
        EntityHelper.refreshMappings(newHearingEntity);

        HearingEntity savedHearing = courtCaseService.createOrUpdateHearingByHearingId(newHearingEntity.getHearingId(), newHearingEntity).block();
        assertThat(savedHearing.withId(null)).isEqualTo(newHearingEntity.withId(null));
        var dbData = assertThatHearingIsNotImmutable(caseId, hearingId, DEFENDANT_ID_1);
        assertThat(dbData.getV1().withId(null)).isEqualTo(newHearingEntity.withId(null));
        assertThat(dbData.getV1().getHearingDefendants()).hasSize(2);

        // update
        var hearingUpdateWithOnlyOneDefendant = EntityHelper.aHearingEntityWithHearingId(caseId, hearingId, DEFENDANT_ID_2);

        HearingEntity savedUpdate = courtCaseService.createOrUpdateHearingByHearingId(hearingUpdateWithOnlyOneDefendant.getHearingId(), hearingUpdateWithOnlyOneDefendant).block();
        assertThat(savedUpdate.withId(null)).isEqualTo(hearingUpdateWithOnlyOneDefendant);
        DefendantEntity defendantUpdate = hearingUpdateWithOnlyOneDefendant.getHearingDefendants().get(0).getDefendant();

        var updatedData = assertThatHearingIsNotImmutable(caseId, hearingId, DEFENDANT_ID_2);
        HearingEntity hearingAfterUpdate = updatedData.getV1();
        assertThat(hearingAfterUpdate.getHearingDefendants()).hasSize(1);

        assertThat(hearingAfterUpdate.getHearingDefendants().stream().map(HearingDefendantEntity::getDefendantId)
            .map(this::findAllByDefendantId).map(defendantEntities -> defendantEntities.get(0).withId(null)).collect(Collectors.toList()))
            .isEqualTo(List.of(defendantUpdate));
    }

    @Test
    public void givenExistingCase_createNewHearing_addHearingToExistingCase() {
        var caseId = "fd907836-324f-42a2-8a7a-e3d956b9d1e7";
        var hearingId = "f069bfcd-29d6-4ab0-82f4-5df1ffd47f33";
        var hearingId2 = "23c2ed19-51f7-410f-8eec-d78e747bc5c7";

        final var newHearingEntity = EntityHelper.aHearingEntityWithHearingId(caseId, hearingId, DEFENDANT_ID_1);
        var hearingDefendantEntity = newHearingEntity.getHearingDefendants();

        HearingEntity savedHearing = courtCaseService.createOrUpdateHearingByHearingId(newHearingEntity.getHearingId(), newHearingEntity).block();
        assertThat(savedHearing).isEqualTo(newHearingEntity);
        var dbData = assertThatHearingIsNotImmutable(caseId, hearingId, DEFENDANT_ID_1);
        assertThat(dbData.getV1().getHearingDefendants()).hasSize(1);

        // update
        var addHearing = EntityHelper.aHearingEntityWithHearingId(caseId, hearingId2, DEFENDANT_ID_1);
        var offenderUpdate = EntityHelper.anOffender(EntityHelper.CRN).withAwaitingPsr(true).withBreach(false);
        var defendantUpdate = EntityHelper.aDefendantEntity(DEFENDANT_ID_1, EntityHelper.CRN).withDefendantName("Mr. Updated Name");
        defendantUpdate.setOffender(offenderUpdate);
        addHearing.getHearingDefendants().get(0).setDefendant(defendantUpdate);
        EntityHelper.refreshMappings(addHearing);

        HearingEntity savedUpdate = courtCaseService.createOrUpdateHearingByHearingId(hearingId2, addHearing).block();
        assertThat(savedUpdate.withId(null)).isEqualTo(addHearing.withId(null));

        var hearing1DbResult = assertThatHearingIsNotImmutable(caseId, hearingId, DEFENDANT_ID_1);
        var hearing1 = hearing1DbResult.getV1();
        var hearing2DbResult = assertThatHearingIsNotImmutable(caseId, hearingId2, DEFENDANT_ID_1);
        var hearing2 = hearing2DbResult.getV1();

        assertThat(hearing1.getCourtCase()).isEqualTo(hearing2.getCourtCase());
        var courtCaseUpdate = hearing2DbResult.getV2();
        assertThat(new ArrayList<>(courtCaseUpdate.getHearings())).isEqualTo(List.of(hearing1, hearing2));
    }

    @Test
    public void shouldTransformCaseListResultToResponse() {
        var req = new HearingSearchRequest(
            List.of(),
            List.of(),
                List.of(),
                List.of(),
                false,
                false,
                null,
                LocalDate.of(2023, 7, 3),
                1,
                5,
                null,
                null,
                null,
                null,
                null,
                null,
                null
        );
        var result = courtCaseService.filterHearings("B14LO", req);
        assertThat(result.getCases().size()).isEqualTo(5);
        assertThat(result.getTotalElements()).isEqualTo(11);
        assertThat(result.getTotalPages()).isEqualTo(3);
        assertThat(result.getPossibleMatchesCount()).isEqualTo(2);
        assertThat(result.getRecentlyAddedCount()).isEqualTo(2);
        assertThat(result.getCourtRoomFilters()).containsAll(List.of("01", "03", "04", "05", "1", "Crown Court 5-1"));

        assertThat(result.getCases().stream().map(CourtCaseResponse::getDefendantName).toList().containsAll(List.of("Mr Jeff Blogs", "Miss Esther Egge")));
    }

    private Tuple3<HearingEntity, CourtCaseEntity, DefendantEntity> assertThatHearingIsNotImmutable(String caseId, String hearingId, String defendantId) {
        var allByHearingIdCreateResult = findAllByHearingId(hearingId);
        var allByCaseIdCreateResult = findAllByCaseId(caseId);
        var allByDefendantId = findAllByDefendantId(defendantId);

        assertThat(allByHearingIdCreateResult).hasSize(1);
        assertThat(allByCaseIdCreateResult).hasSize(1);
        assertThat(allByDefendantId).hasSize(1);
        return Tuple3.tuple(allByHearingIdCreateResult.get(0), allByCaseIdCreateResult.get(0), allByDefendantId.get(0));
    }

    private List<CourtCaseEntity> findAllByCaseId(String caseId) {
        return entityManager.createQuery("select c from CourtCaseEntity c JOIN FETCH c.hearings h where c.caseId = :caseId", CourtCaseEntity.class)
            .setParameter("caseId", caseId)
            .getResultList();
    }

    private List<HearingEntity> findAllByHearingId(String hearingId) {
        return entityManager.createQuery("select h from HearingEntity h JOIN FETCH h.hearingDefendants hd where h.hearingId = :hearingId")
            .setParameter("hearingId", hearingId)
            .getResultList();
    }
    private List findAllAuditByHearingId(String hearingId) {
        return entityManager.createNativeQuery("select * from hearing_AUD h where h.hearing_id = :hearingId")
            .setParameter("hearingId", hearingId)
            .getResultList();
    }
    private List<DefendantEntity> findAllByDefendantId(String defendantId) {
        return entityManager.createQuery("select d from DefendantEntity d where d.defendantId = :defendantId", DefendantEntity.class)
            .setParameter("defendantId", defendantId)
            .getResultList();
    }
}
