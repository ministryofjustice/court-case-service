package uk.gov.justice.probation.courtcaseservice.service;

import com.amazonaws.auth.AWSStaticCredentialsProvider;
import com.amazonaws.auth.BasicAWSCredentials;
import com.amazonaws.services.sns.AmazonSNS;
import com.amazonaws.services.sns.AmazonSNSClientBuilder;
import groovy.lang.Tuple3;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.context.annotation.Bean;
import org.springframework.dao.CannotAcquireLockException;
import org.springframework.test.context.jdbc.Sql;
import org.springframework.test.context.jdbc.SqlConfig;
import org.testcontainers.containers.localstack.LocalStackContainer;
import uk.gov.justice.probation.courtcaseservice.BaseIntTest;
import uk.gov.justice.probation.courtcaseservice.jpa.entity.CourtCaseEntity;
import uk.gov.justice.probation.courtcaseservice.jpa.entity.DefendantEntity;
import uk.gov.justice.probation.courtcaseservice.jpa.entity.EntityHelper;
import uk.gov.justice.probation.courtcaseservice.jpa.entity.HearingDefendantEntity;
import uk.gov.justice.probation.courtcaseservice.jpa.entity.HearingEntity;
import uk.gov.justice.probation.courtcaseservice.jpa.entity.HearingEventType;
import uk.gov.justice.probation.courtcaseservice.jpa.repository.CourtRepository;

import javax.persistence.EntityManager;
import java.util.List;
import java.util.stream.Collectors;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.AssertionsForClassTypes.assertThatExceptionOfType;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.springframework.test.context.jdbc.Sql.ExecutionPhase.AFTER_TEST_METHOD;
import static org.springframework.test.context.jdbc.Sql.ExecutionPhase.BEFORE_TEST_METHOD;
import static org.springframework.test.context.jdbc.SqlConfig.TransactionMode.ISOLATED;

@Sql(scripts = "classpath:sql/before-common.sql", config = @SqlConfig(transactionMode = ISOLATED), executionPhase = BEFORE_TEST_METHOD)
@Sql(scripts = "classpath:after-test.sql", config = @SqlConfig(transactionMode = ISOLATED), executionPhase = AFTER_TEST_METHOD)
public class ImmutableCourtCaseServiceIntTest extends BaseIntTest {
    private static final String CRN = "CRN";
    private static final HearingEntity COURT_CASE_ENTITY = EntityHelper.aHearingEntityWithCrn(CRN);
    private static final String COURT_CODE = "B10JQ";
    @Autowired
    private ImmutableCourtCaseService courtCaseService;
    @MockBean
    private CourtRepository courtRepository;
    @MockBean
    private DomainEventService domainEventService;

    @Autowired
    EntityManager entityManager;

    @Test
    public void givenCannotAcquireLockExceptionThrown_whenCreateCase_thenRetry() {
        when(courtRepository.findByCourtCode(COURT_CODE)).thenThrow(CannotAcquireLockException.class);
        assertThatExceptionOfType(CannotAcquireLockException.class)
                .isThrownBy(() -> courtCaseService.createHearing("1234", COURT_CASE_ENTITY));

        verify(courtRepository, times(3)).findByCourtCode(COURT_CODE);
    }

    @Bean
    public static AmazonSNS createAmazonSnsClient() {
        return AmazonSNSClientBuilder
                .standard()
                .withEndpointConfiguration(localstack.getEndpointConfiguration(LocalStackContainer.Service.SNS))
                .withCredentials(new AWSStaticCredentialsProvider(new BasicAWSCredentials(
                        localstack.getAccessKey(), localstack.getSecretKey()
                )))
                .withRegion("eu-west-2")
                .build();
    }

    String DEFENDANT_ID_1 = "8e0263a1-bc3e-4dad-93c4-d333d32389ea";

    @Test
    public void givenNewCase_createOrUpdateHearing_createNewCase_givenAFollowingUpdate_mutateTheExistingCase() {
        var caseId = "fd907836-324f-42a2-8a7a-e3d956b9d1e7";
        var hearingId = "f069bfcd-29d6-4ab0-82f4-5df1ffd47f33";
        final var newHearingEntity = EntityHelper.aHearingEntityWithHearingId(caseId, hearingId, DEFENDANT_ID_1);

        HearingEntity savedHearing = courtCaseService.createOrUpdateHearingByHearingId(newHearingEntity.getHearingId(), newHearingEntity).block();
        assertThat(savedHearing.withId(null)).isEqualTo(newHearingEntity.withId(null));

        var dbData = assertThatHearingIsNotImmutable(caseId, hearingId, DEFENDANT_ID_1);
        // update
        var hearingUpdate = EntityHelper.aHearingEntityWithHearingId(caseId, hearingId, DEFENDANT_ID_1).withHearingEventType(HearingEventType.RESULTED);

        HearingEntity savedUpdate = courtCaseService.createOrUpdateHearingByHearingId(hearingUpdate.getHearingId(), hearingUpdate).block();
        assertThat(savedUpdate.withId(null)).isEqualTo(hearingUpdate);

        var updatedData = assertThatHearingIsNotImmutable(caseId, hearingId, DEFENDANT_ID_1);
        assertThat(updatedData.getV1().getHearingEventType()).isEqualTo(HearingEventType.RESULTED);
    }

    @Test
    public void givenHearingUpdateWitNewDefendant_addNewDefendantToExistingHearing() {
        var caseId = "fd907836-324f-42a2-8a7a-e3d956b9d1e7";
        var hearingId = "f069bfcd-29d6-4ab0-82f4-5df1ffd47f33";
        final var newHearingEntity = EntityHelper.aHearingEntityWithHearingId(caseId, hearingId, DEFENDANT_ID_1);
        var hearingDefendantEntity = newHearingEntity.getHearingDefendants();
        var defendant = hearingDefendantEntity.get(0).getDefendant();
        var defendantId2 = "9b165f40-ecc1-4cf1-bb69-41504de8c0d5";

        HearingEntity savedHearing = courtCaseService.createOrUpdateHearingByHearingId(newHearingEntity.getHearingId(), newHearingEntity).block();
        assertThat(savedHearing.withId(null)).isEqualTo(newHearingEntity.withId(null));
        assertThatHearingIsNotImmutable(caseId, hearingId, DEFENDANT_ID_1);

        // update
        var hearingUpdateWithNewDefendant = EntityHelper.aHearingEntityWithHearingId(caseId, hearingId, DEFENDANT_ID_1);
        HearingDefendantEntity newHearingDefendant = EntityHelper.aHearingDefendantEntity(defendantId2, null);
        hearingUpdateWithNewDefendant.getHearingDefendants().add(newHearingDefendant);

        HearingEntity savedUpdate = courtCaseService.createOrUpdateHearingByHearingId(hearingUpdateWithNewDefendant.getHearingId(), hearingUpdateWithNewDefendant).block();
        assertThat(savedUpdate.withId(null)).isEqualTo(hearingUpdateWithNewDefendant);

        var updatedData = assertThatHearingIsNotImmutable(caseId, hearingId, DEFENDANT_ID_1);
        HearingEntity hearingAfterUpdate = updatedData.getV1();
        assertThat(hearingAfterUpdate.getHearingDefendants()).hasSize(2);
        var updatedDefendant1 = updatedData.getV3();

        assertThat(hearingAfterUpdate.getHearingDefendants().stream().map(hearingDefendantEntity1 -> findAllByDefendantId(hearingDefendantEntity1.getDefendantId()).get(0)
            ).collect(Collectors.toList()))
            .isEqualTo(List.of(defendant, newHearingDefendant.getDefendant()));
    }

    @Test
    public void givenHearingWiTwoDefendant_addAUpdateWithOneDefendant_shouldRemoveExistingDefendant() {
        var caseId = "fd907836-324f-42a2-8a7a-e3d956b9d1e7";
        var hearingId = "f069bfcd-29d6-4ab0-82f4-5df1ffd47f33";
        final var newHearingEntity = EntityHelper.aHearingEntityWithHearingId(caseId, hearingId, DEFENDANT_ID_1);
        var hearingDefendantEntity = newHearingEntity.getHearingDefendants().get(0);
        var defendant = hearingDefendantEntity.getDefendant();
        var DEFENDANT_ID_2 = "9b165f40-ecc1-4cf1-bb69-41504de8c0d5";
        HearingDefendantEntity hearingDefendant2 = EntityHelper.aHearingDefendantEntity(DEFENDANT_ID_2, null);
        hearingDefendant2.setHearing(newHearingEntity);
        newHearingEntity.getHearingDefendants().add(hearingDefendant2);
        hearingDefendant2.getOffences().forEach(offenceEntity -> offenceEntity.setHearingDefendant(hearingDefendant2));

        HearingEntity savedHearing = courtCaseService.createOrUpdateHearingByHearingId(newHearingEntity.getHearingId(), newHearingEntity).block();
//        assertThat(savedHearing.withId(null)).isEqualTo(newHearingEntity);
        var dbData = assertThatHearingIsNotImmutable(caseId, hearingId, DEFENDANT_ID_1);
        assertThat(dbData.getV1().withId(null)).isEqualTo(newHearingEntity.withId(null));
        assertThat(dbData.getV1().getHearingDefendants()).hasSize(2);

        // update
        var hearingUpdateWithOnyOneDefendant = EntityHelper.aHearingEntityWithHearingId(caseId, hearingId, DEFENDANT_ID_2);

        HearingEntity savedUpdate = courtCaseService.createOrUpdateHearingByHearingId(hearingUpdateWithOnyOneDefendant.getHearingId(), hearingUpdateWithOnyOneDefendant).block();
        assertThat(savedUpdate.withId(null)).isEqualTo(hearingUpdateWithOnyOneDefendant);
        DefendantEntity newDefendant = hearingUpdateWithOnyOneDefendant.getHearingDefendants().get(0).getDefendant();

        var updatedData = assertThatHearingIsNotImmutable(caseId, hearingId, DEFENDANT_ID_2);
        HearingEntity hearingAfterUpdate = updatedData.getV1();
        assertThat(hearingAfterUpdate.getHearingDefendants()).hasSize(1);

        assertThat(hearingAfterUpdate.getHearingDefendants().stream().map(HearingDefendantEntity::getDefendantId)
            .map(this::findAllByDefendantId).map(defendantEntities -> defendantEntities.get(0)).collect(Collectors.toList()))
            .isEqualTo(List.of(newDefendant.withCrn(null)));
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

        HearingEntity savedUpdate = courtCaseService.createOrUpdateHearingByHearingId(hearingId2, addHearing).block();
        assertThat(savedUpdate.withId(null)).isEqualTo(addHearing.withId(null));

        var hearing1DbResult = assertThatHearingIsNotImmutable(caseId, hearingId, DEFENDANT_ID_1);
        var hearing1 = hearing1DbResult.getV1();
        var hearing2DbResult = assertThatHearingIsNotImmutable(caseId, hearingId2, DEFENDANT_ID_1);
        var hearing2 = hearing2DbResult.getV1();

        assertThat(hearing1.getCourtCase()).isEqualTo(hearing2.getCourtCase());
        var courtCaseUpdate = hearing2DbResult.getV2();
        assertThat(courtCaseUpdate.getHearings().stream().collect(Collectors.toList())).isEqualTo(List.of(hearing1, hearing2));
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
        return entityManager.createQuery("select c from CourtCaseEntity c where c.caseId = :caseId", CourtCaseEntity.class)
            .setParameter("caseId", caseId)
            .getResultList();
    }

    private List<HearingEntity> findAllByHearingId(String hearingId) {
        return entityManager.createQuery("select h from HearingEntity h where h.hearingId = :hearingId", HearingEntity.class)
            .setParameter("hearingId", hearingId)
            .getResultList();
    }
    private List<DefendantEntity> findAllByDefendantId(String defendantId) {
        return entityManager.createQuery("select h from DefendantEntity h where h.defendantId = :defendantId", DefendantEntity.class)
            .setParameter("defendantId", defendantId)
            .getResultList();
    }
}
