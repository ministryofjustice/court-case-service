package uk.gov.justice.probation.courtcaseservice.jpa.repository;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.context.jdbc.Sql;
import org.springframework.test.context.jdbc.SqlConfig;
import uk.gov.justice.probation.courtcaseservice.BaseIntTest;
import uk.gov.justice.probation.courtcaseservice.jpa.entity.CaseCommentEntity;
import uk.gov.justice.probation.courtcaseservice.jpa.entity.CourtCaseEntity;
import uk.gov.justice.probation.courtcaseservice.jpa.entity.DefendantEntity;
import uk.gov.justice.probation.courtcaseservice.jpa.entity.DefendantType;
import uk.gov.justice.probation.courtcaseservice.jpa.entity.EntityHelper;
import uk.gov.justice.probation.courtcaseservice.jpa.entity.HearingDefendantEntity;
import uk.gov.justice.probation.courtcaseservice.jpa.entity.HearingEntity;
import uk.gov.justice.probation.courtcaseservice.jpa.entity.NamePropertiesEntity;
import uk.gov.justice.probation.courtcaseservice.jpa.entity.OffenderEntity;
import uk.gov.justice.probation.courtcaseservice.service.HearingEntityInitService;
import uk.gov.justice.probation.courtcaseservice.service.OffenderEntityInitService;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.AssertionsForClassTypes.assertThatExceptionOfType;
import static org.springframework.test.context.jdbc.SqlConfig.TransactionMode.ISOLATED;
import static uk.gov.justice.probation.courtcaseservice.jpa.entity.EntityHelper.CASE_ID;
import static uk.gov.justice.probation.courtcaseservice.jpa.entity.EntityHelper.HEARING_ID;
import static uk.gov.justice.probation.courtcaseservice.jpa.entity.OffenderProbationStatus.CURRENT;

@Sql(scripts = {
        "classpath:sql/before-common.sql",
        "classpath:sql/before-HearingRepositoryFacadeIntTest.sql"
}, config = @SqlConfig(transactionMode = ISOLATED))
public class HearingRepositoryFacadeIntTest extends BaseIntTest {

    @Autowired
    private OffenderRepository offenderRepository;
    private OffenderRepositoryFacade offenderRepositoryFacade;
    @Autowired
    private OffenderEntityInitService offenderEntityInitService;
    @Autowired
    private HearingRepository hearingRepository;
    @Autowired
    private DefendantRepository defendantRepository;
    @Autowired
    private CaseCommentsRepository caseCommentsRepository;
    @Autowired
    private HearingEntityInitService hearingEntityInitService;

    private HearingRepositoryFacade hearingRepositoryFacade;

    @BeforeEach
    public void setUp() {
        offenderRepositoryFacade = new OffenderRepositoryFacade(offenderRepository, offenderEntityInitService);
        hearingRepositoryFacade = new HearingRepositoryFacade(offenderRepository, offenderRepositoryFacade, hearingRepository,
                hearingEntityInitService, defendantRepository, caseCommentsRepository);
    }

    @Test
    public void whenFindFirstByHearingIdOrderByIdDesc_thenReturnCorrectRecordWithOffender() {
        final var actual = hearingRepositoryFacade.findFirstByHearingId("5564cbfd-3d53-4f36-9508-437416b08738");

        assertIsFerrisBueller(actual);
    }

    @Test
    public void whenFindFirstByHearingIdAndCourtCaseId_thenReturnCorrectRecordWithOffender() {
        final var actual = hearingRepositoryFacade.findFirstByHearingIdAndCourtCaseId("5564cbfd-3d53-4f36-9508-437416b08738", "727af2a3-f9ec-4544-b5ef-2ec3ec0fcf2b");

        assertIsFerrisBueller(actual);
    }

    @Test
    public void whenFindFirstByHearingIdAndCourtCaseId_thenDoNotReturnRecord() {
        final var actual = hearingRepositoryFacade.findFirstByHearingIdAndCourtCaseId("5564cbfd-3d53-4f36-9508-437416b08738", "827af2a3-f9ec-4544-b5ef-2ec3ec0fcf2b");

        assertThat(actual).isEmpty();
    }

    @Test
    public void whenFindByHearingIdAndDefendantId_thenReturnCorrectRecordWithOffender() {
        final var actual = hearingRepositoryFacade.findByHearingIdAndDefendantId("5564cbfd-3d53-4f36-9508-437416b08738", "0048297a-fd9c-4c96-8c03-8122b802a54d");

        assertIsFerrisBueller(actual);

        var hearingEntity = actual.get();
        List<CaseCommentEntity> caseComments = hearingEntity.getCourtCase().getCaseComments();
        assertThat(caseComments).hasSize(1);
        assertThat(caseComments.get(0).getCaseId()).isEqualTo("727af2a3-f9ec-4544-b5ef-2ec3ec0fcf2b");
        assertThat(caseComments.get(0).getId()).isEqualTo(-1700028900);
        assertThat(caseComments.get(0).getComment()).isEqualTo("PSR in progress");
        assertThat(caseComments.get(0).getAuthor()).isEqualTo("Author One");
        assertThat(caseComments.get(0).getCreatedByUuid()).isEqualTo("fb9a3bbf-360b-48d1-bdd6-b9292f9a0d81");
    }

    @Test
    public void whenFindByCourtCodeCaseNoAndListNo_thenReturnCorrectRecordWithOffender() {
        final var actual = hearingRepositoryFacade.findByCourtCodeAndCaseNo("B33HU", "1600028888", "3rd");

        assertIsFerrisBueller(actual);
    }

    @Test
    public void givenNoCaseWithListNo_whenFindByCourtCodeCaseNoAndListNo_fallbackAndReadCaseWithoutListNo() {
        final var actual = hearingRepositoryFacade.findByCourtCodeAndCaseNo("B33HU", "1600028888", "10th");

        assertIsFerrisBueller(actual, "fe657c3a-b674-4e17-8772-7281c99e4f9f", "fe657c3a-b674-4e17-8772-7281c99e4f9f");
    }

    @Test
    public void whenFindByCourtCodeAndCaseNo_andNoListNo_thenReturnCorrectRecordWithOffender() {
        final var actual = hearingRepositoryFacade.findByCourtCodeAndCaseNo("B33HU", "1600028888", null);

        assertIsFerrisBueller(actual, "fe657c3a-b674-4e17-8772-7281c99e4f9f", "fe657c3a-b674-4e17-8772-7281c99e4f9f");
    }

    @Test
    @Disabled("This validation has been removed but want to check with John")
    public void whenFindFirstByHearingIdOrderByIdDesc_andOffenderDoesNotExist_thenThrow() {
        assertThatExceptionOfType(RuntimeException.class)
                .isThrownBy(() -> hearingRepositoryFacade.findFirstByHearingId("db63e9b5-6263-4235-9c4e-a99e200ae33e"))
                .withMessageContaining("Unexpected state: Offender with CRN 'NOT_EXTANT' is specified on defendant 'd1a4f1b7-b153-4740-b68a-2b84feff6996' but it does not exist");
    }

    @Test
    public void whenFindByCourtCodeAndHearingDay_thenReturnCorrectCases() {
        final var actual = hearingRepositoryFacade.findByCourtCodeAndHearingDay("B14LO", LocalDate.of(2022, 3, 23));

        assertIsCorrectCaseList(actual);
    }

    @Test
    public void whenFindByCourtCodeAndHearingDayWithParams_thenReturnCorrectCases() {
        final var actual = hearingRepositoryFacade.findByCourtCodeAndHearingDay("B14LO", LocalDate.of(2022, 3, 23),
                LocalDateTime.of(LocalDate.EPOCH, LocalTime.MIDNIGHT), LocalDateTime.of(LocalDate.of(2125, 1, 1), LocalTime.MIDNIGHT));

        assertIsCorrectCaseList(actual);
    }

    @Test
    public void whenSave_thenPersistHearingAndDefendantsAndOffenders() {
        final var offenderEntity = OffenderEntity.builder()
                .crn("X25827")
                .awaitingPsr(false)
                .defendants(new ArrayList<DefendantEntity>())
                .preSentenceActivity(false)
                .suspendedSentenceOrder(false)
                .preSentenceActivity(false)
                .previouslyKnownTerminationDate(null)
                .build();

        final var defendantEntity = DefendantEntity.builder()
                .defendantId("d1eefed2-04df-11ec-b2d8-0242ac130002")
                .defendantName("Ferris Bueller")
                .name(NamePropertiesEntity.builder()
                        .forename1("Ferris")
                        .surname("Bueller")
                        .build())
                .hearingDefendants(new ArrayList<HearingDefendantEntity>())
                .type(DefendantType.PERSON)
                .personId(UUID.randomUUID().toString())
                .offender(offenderEntity)
                .build();

        final var hearingDefendantEntity = HearingDefendantEntity.builder()
                .defendantId("d1eefed2-04df-11ec-b2d8-0242ac130002")
                .defendant(defendantEntity)
                .notes(List.of())
                .offences(List.of())
                .build();

        final var hearingEntity = HearingEntity.builder()
                .courtCase(CourtCaseEntity.builder()
                        .caseDefendants(List.of())
                        .caseMarkers(List.of())
                        .hearings(new ArrayList<HearingEntity>())
                        .caseId(CASE_ID)
                        .build())
                .hearingId("b229b992-02d2-4393-affd-3878f2c7d61e")
                .hearingDays(List.of())
                .hearingDefendants(Collections.singletonList(
                        hearingDefendantEntity
                ))
                .build();

        hearingDefendantEntity.setHearing(hearingEntity);

        hearingEntity.getCourtCase().addHearing(hearingEntity);

        hearingDefendantEntity.getDefendant().addHearingDefendant(hearingDefendantEntity);
        assertThat(hearingRepositoryFacade.save(hearingEntity)).isEqualTo(hearingEntity);

        assertThat(hearingEntityInitService.findFirstByHearingIdFullyInitialised("b229b992-02d2-4393-affd-3878f2c7d61e").orElseThrow())
                .usingRecursiveComparison().ignoringFields("created", "firstCreated", "lastUpdated")
                .ignoringFieldsMatchingRegexes("hearingDefendants*.defendant.offender.defendants", "hearingDefendants*.hearing.firstCreated", "hearingDefendants[*].defendant.created", "hearingDefendants[*].created", ".*lastUpdated", ".*created")
                .isEqualTo(hearingEntity);
    }

    private static HearingEntity getHearingEntity(HearingDefendantEntity hearingDefendantEntity) {
        final var hearingEntity = HearingEntity.builder()
                .courtCase(CourtCaseEntity.builder()
                        .caseDefendants(List.of())
                        .caseMarkers(List.of())
                        .hearings(new ArrayList<HearingEntity>())
                        .caseId(CASE_ID)
                        .build())
                .hearingId(HEARING_ID)
                .hearingDays(List.of())
                .hearingDefendants(Collections.singletonList(
                        hearingDefendantEntity
                ))
                .build();
        return hearingEntity;
    }

    @Test
    public void givenAnExistingOffender_whenSave_thenPersistOffender() {

        final var offenderEntity = OffenderEntity.builder()
                .crn("X25827")
                .awaitingPsr(false)
                .defendants(new ArrayList<DefendantEntity>())
                .preSentenceActivity(false)
                .suspendedSentenceOrder(false)
                .preSentenceActivity(false)
                .previouslyKnownTerminationDate(null)
                .build();

        final var defendantEntity = DefendantEntity.builder()
                .defendantId("d1eefed2-04df-11ec-b2d8-0242ac130002")
                .defendantName("Ferris Bueller")
                .name(NamePropertiesEntity.builder()
                        .forename1("Ferris")
                        .surname("Bueller")
                        .build())
                .hearingDefendants(new ArrayList<HearingDefendantEntity>())
                .type(DefendantType.PERSON)
                .personId(UUID.randomUUID().toString())
                .offender(offenderEntity)
                .build();

        final var hearingDefendantEntity = HearingDefendantEntity.builder()
                .defendantId("d1eefed2-04df-11ec-b2d8-0242ac130002")
                .defendant(defendantEntity)
                .notes(List.of())
                .offences(List.of())
                .build();

        final var hearingEntity = HearingEntity.builder()
                .courtCase(CourtCaseEntity.builder()
                        .caseDefendants(List.of())
                        .caseMarkers(List.of())
                        .hearings(new ArrayList<HearingEntity>())
                        .caseId(CASE_ID)
                        .build())
                .hearingId(HEARING_ID)
                .hearingDays(List.of())
                .hearingDefendants(Collections.singletonList(
                        hearingDefendantEntity
                ))
                .build();

        hearingDefendantEntity.setHearing(hearingEntity);

        hearingEntity.getCourtCase().addHearing(hearingEntity);

        hearingDefendantEntity.getDefendant().addHearingDefendant(hearingDefendantEntity);

        assertThat(hearingRepositoryFacade.save(hearingEntity)).isEqualTo(hearingEntity);

        var updatedHearingEntity = hearingEntityInitService.findFirstByHearingIdFullyInitialised(HEARING_ID).orElseThrow();
        assertThat(updatedHearingEntity)
                .usingRecursiveComparison().ignoringFields("created", "firstCreated", "lastUpdated")
                .ignoringFieldsMatchingRegexes("hearingDefendants*.defendant.offender.defendants", "hearingDefendants*.hearing.firstCreated", ".*lastUpdated", ".*created")
                .isEqualTo(hearingEntity);
        assertThat(updatedHearingEntity.getHearingDefendants().getFirst().getDefendant().getOffender().getDefendants().size()).isEqualTo(1);
    }

    private void assertIsCorrectCaseList(List<HearingEntity> actual) {
        assertThat(actual).extracting("hearingId").containsExactlyInAnyOrder(
                "1bfff8b7-fbc6-413f-8545-8299c26f75bd",
                "7c1ccb0e-399a-4a28-a866-f52c139735f6",
                "c12be6d5-2d6e-432b-a147-94c49171ef1d",
                "961f6b9d-ae7e-4998-9d5d-4f56ceadce99"
        );

        final var hearing = actual
                .stream()
                .filter(hearingEntity -> "c12be6d5-2d6e-432b-a147-94c49171ef1d".equals(hearingEntity.getHearingId()))
                .findFirst()
                .orElseThrow();

        assertThat(hearing
                .getHearingDefendants()).extracting("defendantId")
                .containsExactlyInAnyOrder("b564248c-d97a-490e-914b-3c04ada4a99e", "50110880-1573-4fc9-8b16-8bb85413d37e");

        final var hearingDefendant = hearing.getHearingDefendant("b564248c-d97a-490e-914b-3c04ada4a99e");
        assertThat(hearingDefendant.getDefendant().getCrn()).isEqualTo("X12345");
        assertThat(hearingDefendant.getDefendant().getOffender().getCrn()).isEqualTo("X12345");
        assertThat(hearingDefendant.getDefendant().getOffender().getProbationStatus()).isEqualTo(CURRENT);

        final var hearingDefendant2 = hearing.getHearingDefendant("50110880-1573-4fc9-8b16-8bb85413d37e");
        assertThat(hearingDefendant2.getDefendant().getCrn()).isNull();
        assertThat(hearingDefendant2.getDefendant().getOffender()).isNull();
    }

    @SuppressWarnings("OptionalUsedAsFieldOrParameterType")
    private void assertIsFerrisBueller(Optional<HearingEntity> actual) {
        assertIsFerrisBueller(actual, "5564cbfd-3d53-4f36-9508-437416b08738", "727af2a3-f9ec-4544-b5ef-2ec3ec0fcf2b");
    }

    @SuppressWarnings("OptionalUsedAsFieldOrParameterType")
    private void assertIsFerrisBueller(Optional<HearingEntity> actual, String hearingId, String caseId) {
        assertThat(actual).isPresent();

        final var hearingEntity = actual.get();
        assertThat(hearingEntity.getHearingId()).isEqualTo(hearingId);
        assertThat(hearingEntity.getCaseId()).isEqualTo(caseId);
        assertThat(hearingEntity.getCaseNo()).isEqualTo("1600028888");
        assertThat(hearingEntity.getHearingDefendants().size()).isEqualTo(1);

        assertThat(hearingEntity.getHearingDays().size()).isEqualTo(1);
        assertThat(hearingEntity.getHearingDays().get(0).getCourtCode()).isEqualTo("B33HU");
        assertThat(hearingEntity.getHearingDays().get(0).getDay()).isEqualTo(LocalDate.of(2022, 3, 25));

        final var hearingDefendant = hearingEntity.getHearingDefendants().get(0);
        assertThat(hearingDefendant.getDefendantId()).isEqualTo("0048297a-fd9c-4c96-8c03-8122b802a54d");
        assertThat(hearingDefendant.getDefendant().getDefendantId()).isEqualTo("0048297a-fd9c-4c96-8c03-8122b802a54d");
        assertThat(hearingDefendant.getDefendant().getName().getForename2()).isEqualTo("Antimony");
        assertThat(hearingDefendant.getDefendant().getCrn()).isEqualTo("X25829");
        assertThat(hearingDefendant.getDefendant().getOffender().getCrn()).isEqualTo("X25829");
        assertThat(hearingDefendant.getDefendant().getOffender().getProbationStatus()).isEqualTo(CURRENT);

        assertThat(hearingDefendant.getOffences().size()).isEqualTo(1);
        assertThat(hearingDefendant.getOffences().get(0).getTitle()).isEqualTo("Theft from a garage");
    }

    @Test
    public void whenFindFirstByHearingIdOrderByIdDesc_thenReturnJudicialResultsWithCorrectOrder() {
        final var offenderEntity = OffenderEntity.builder()
                .crn("X340906")
                .awaitingPsr(false)
                .defendants(new ArrayList<DefendantEntity>())
                .preSentenceActivity(false)
                .suspendedSentenceOrder(false)
                .preSentenceActivity(false)
                .previouslyKnownTerminationDate(null)
                .build();

        final var defendantEntity = DefendantEntity.builder()
                .defendantId("d1eefed2-04df-11ec-b2d8-0242ac130002")
                .defendantName("Ferris Bueller")
                .name(NamePropertiesEntity.builder()
                        .forename1("Ferris")
                        .surname("Bueller")
                        .build())
                .hearingDefendants(new ArrayList<HearingDefendantEntity>())
                .type(DefendantType.PERSON)
                .personId(UUID.randomUUID().toString())
                .offender(offenderEntity)
                .build();

        final var offenceEntity = EntityHelper.aDefendantOffenceWithJudicialResults();

        final var hearingDefendantEntity = HearingDefendantEntity.builder()
                .defendantId("d1eefed2-04df-11ec-b2d8-0242ac130002")
                .defendant(defendantEntity)
                .notes(List.of())
                .offences(List.of(offenceEntity))
                .build();


        final var hearingEntity = HearingEntity.builder()
                .courtCase(CourtCaseEntity.builder()
                        .caseDefendants(List.of())
                        .caseNo("4000033")
                        .caseMarkers(List.of())
                        .hearings(new ArrayList<HearingEntity>())
                        .caseId(CASE_ID)
                        .build())
                .hearingId(HEARING_ID)
                .hearingDays(List.of())
                .hearingDefendants(Collections.singletonList(
                        hearingDefendantEntity
                ))
                .build();

        offenceEntity.setHearingDefendant(hearingDefendantEntity);
        hearingDefendantEntity.setHearing(hearingEntity);

        hearingEntity.getCourtCase().addHearing(hearingEntity);

        hearingDefendantEntity.getDefendant().addHearingDefendant(hearingDefendantEntity);

        assertThat(hearingRepositoryFacade.save(hearingEntity)).isEqualTo(hearingEntity);

        var updatedHearingEntity = hearingEntityInitService.findFirstByHearingIdFullyInitialised(HEARING_ID).orElseThrow();


        assertThat(hearingRepositoryFacade.save(hearingEntity)).isEqualTo(updatedHearingEntity);

        assertThat(hearingEntity)
                .usingRecursiveComparison().ignoringFields("created", "firstCreated", "lastUpdated")
                .ignoringFieldsMatchingRegexes("hearingDefendants*.defendant.offender.defendants", "hearingDefendants*.hearing.firstCreated", ".*lastUpdated", ".*created")
                .isEqualTo(updatedHearingEntity);

        assertJudicialResultsOrder(Optional.of(updatedHearingEntity));
    }

    private void assertJudicialResultsOrder(Optional<HearingEntity> actual) {
        assertThat(actual).isPresent();

        final var hearingEntity = actual.get();
        final var hearingDefendant = hearingEntity.getHearingDefendants().get(0);

        assertThat(hearingDefendant.getOffences().size()).isEqualTo(1);
        final var judicialResults = hearingDefendant.getOffences().get(0).getJudicialResults();
        assertThat(judicialResults.size()).isEqualTo(4);

        assertThat(judicialResults).extracting("label")
                .containsExactly("id1", "id3", "id2", "id4");
    }

}
