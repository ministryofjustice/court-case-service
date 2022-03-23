package uk.gov.justice.probation.courtcaseservice.jpa.repository;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.context.jdbc.Sql;
import org.springframework.test.context.jdbc.SqlConfig;
import uk.gov.justice.probation.courtcaseservice.jpa.entity.HearingEntity;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.AssertionsForClassTypes.assertThatExceptionOfType;
import static org.springframework.test.context.jdbc.SqlConfig.TransactionMode.ISOLATED;
import static uk.gov.justice.probation.courtcaseservice.jpa.entity.OffenderProbationStatus.CURRENT;

@Sql(scripts = {
        "classpath:sql/before-common.sql",
        "classpath:sql/before-HearingRepositoryFacadeIntTest.sql"
}, config = @SqlConfig(transactionMode = ISOLATED))
public class HearingRepositoryFacadeIntTest extends BaseRepositoryIntTest {
    @Autowired
    private OffenderRepository offenderRepository;
    @Autowired
    private HearingRepository hearingRepository;
    @Autowired
    private DefendantRepository defendantRepository;

    private HearingRepositoryFacade hearingRepositoryFacade;

    @BeforeEach
    public void setUp() {
        hearingRepositoryFacade = new HearingRepositoryFacade(offenderRepository, hearingRepository, defendantRepository);
    }

    @Test
    public void whenFindFirstByHearingIdOrderByIdDesc_thenReturnCorrectRecordWithOffender() {
        final var actual = hearingRepositoryFacade.findFirstByHearingIdOrderByIdDesc("5564cbfd-3d53-4f36-9508-437416b08738");

        assertIsFerrisBueller(actual);
    }

    @Test
    public void whenFindByCourtCodeAndCaseNo_thenReturnCorrectRecordWithOffender() {
        final var actual = hearingRepositoryFacade.findByCourtCodeAndCaseNo("B33HU", "1600028888");

        assertIsFerrisBueller(actual);
    }

    @Test
    public void whenFindByCaseId_thenReturnCorrectRecordWithOffender() {
        // NOTE: This is actually the hearingId being passed - see deprecation notice for more info
        final var actual = hearingRepositoryFacade.findByCaseId("5564cbfd-3d53-4f36-9508-437416b08738");

        assertIsFerrisBueller(actual);
    }

    @Test
    public void whenFindByCaseIdAndDefendantId_thenReturnCorrectRecordWithOffender() {
        // NOTE: This is actually the hearingId being passed - see deprecation notice for more info
        final var actual = hearingRepositoryFacade.findByCaseIdAndDefendantId("5564cbfd-3d53-4f36-9508-437416b08738", "0048297a-fd9c-4c96-8c03-8122b802a54d");

        assertIsFerrisBueller(actual);
    }

    @Test
    public void whenFindFirstByHearingIdOrderByIdDesc_andOffenderDoesNotExist_thenThrow() {
        assertThatExceptionOfType(RuntimeException.class)
                .isThrownBy(() -> hearingRepositoryFacade.findFirstByHearingIdOrderByIdDesc("db63e9b5-6263-4235-9c4e-a99e200ae33e"))
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

    private void assertIsCorrectCaseList(List<HearingEntity> actual) {
        assertThat(actual).extracting("hearingId").containsExactlyInAnyOrder(
                "1bfff8b7-fbc6-413f-8545-8299c26f75bd",
                "7c1ccb0e-399a-4a28-a866-f52c139735f6",
                "c12be6d5-2d6e-432b-a147-94c49171ef1d",
                "961f6b9d-ae7e-4998-9d5d-4f56ceadce99",
                "e7fa5afa-55ed-4029-9414-614a406d4938" // <-- Known issue, this one should not be included, see PIC-2010
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
        assertThat(actual).isPresent();

        final var hearingEntity = actual.get();
        assertThat(hearingEntity.getHearingId()).isEqualTo("5564cbfd-3d53-4f36-9508-437416b08738");
        assertThat(hearingEntity.getCaseId()).isEqualTo("727af2a3-f9ec-4544-b5ef-2ec3ec0fcf2b");
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

}
