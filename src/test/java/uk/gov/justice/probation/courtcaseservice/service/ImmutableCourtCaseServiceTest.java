package uk.gov.justice.probation.courtcaseservice.service;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Captor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.lang.NonNull;
import uk.gov.justice.probation.courtcaseservice.controller.exceptions.ConflictingInputException;
import uk.gov.justice.probation.courtcaseservice.jpa.entity.CourtCaseEntity;
import uk.gov.justice.probation.courtcaseservice.jpa.entity.CourtEntity;
import uk.gov.justice.probation.courtcaseservice.jpa.entity.EntityHelper;
import uk.gov.justice.probation.courtcaseservice.jpa.entity.GroupedOffenderMatchesEntity;
import uk.gov.justice.probation.courtcaseservice.jpa.entity.OffenderMatchEntity;
import uk.gov.justice.probation.courtcaseservice.jpa.repository.CourtCaseRepository;
import uk.gov.justice.probation.courtcaseservice.jpa.repository.CourtRepository;
import uk.gov.justice.probation.courtcaseservice.jpa.repository.GroupedOffenderMatchRepository;
import uk.gov.justice.probation.courtcaseservice.service.exceptions.EntityNotFoundException;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatExceptionOfType;
import static org.assertj.core.api.ThrowableAssert.catchThrowable;
import static org.mockito.Mockito.eq;
import static org.mockito.Mockito.lenient;
import static org.mockito.Mockito.timeout;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoMoreInteractions;
import static org.mockito.Mockito.when;
import static uk.gov.justice.probation.courtcaseservice.jpa.entity.EntityHelper.CASE_ID;
import static uk.gov.justice.probation.courtcaseservice.jpa.entity.EntityHelper.COURT_CODE;
import static uk.gov.justice.probation.courtcaseservice.jpa.entity.EntityHelper.CRN;
import static uk.gov.justice.probation.courtcaseservice.jpa.entity.EntityHelper.DEFENDANT_ID;
import static uk.gov.justice.probation.courtcaseservice.jpa.entity.EntityHelper.PROBATION_STATUS;

@ExtendWith(MockitoExtension.class)
class ImmutableCourtCaseServiceTest {

    private static final String CASE_NO = "1600028912";
    private static final LocalDateTime CREATED_BEFORE = LocalDateTime.of(2020, 11, 9, 12, 50);

    @Mock
    private CourtRepository courtRepository;
    @Mock
    private CourtCaseRepository courtCaseRepository;
    @Mock
    private CourtEntity courtEntity;
    @Mock
    private GroupedOffenderMatchRepository groupedOffenderMatchRepository;
    @Mock
    private TelemetryService telemetryService;

    private ImmutableCourtCaseService service;

    @ExtendWith(MockitoExtension.class)
    @Nested
    @DisplayName("Tests for createCase by court and case no")
    class CreateUpdateByCourtAndCaseNoTest {

        private CourtCaseEntity courtCase;

        @BeforeEach
        void setup() {
            service = new ImmutableCourtCaseService(courtRepository, courtCaseRepository, telemetryService, groupedOffenderMatchRepository, false);
            courtCase = EntityHelper.aCourtCaseEntity(CRN, CASE_NO);
        }

        @Test
        public void givenNoExistingCase_whenCreateOrUpdateCaseCalledWithCrn_thenLogCreatedAndLinkedEvent() {
            when(courtRepository.findByCourtCode(COURT_CODE)).thenReturn(Optional.of(courtEntity));
            when(courtCaseRepository.findFirstByCaseNoOrderByCreatedDesc(COURT_CODE, CASE_NO)).thenReturn(Optional.empty());
            var otherCourtCaseToUpdate = EntityHelper.aCourtCaseEntity(CRN, "999", LocalDateTime.now().plusDays(1), "Current");
            when(courtCaseRepository.findOtherCurrentCasesByCrn(CRN, CASE_NO)).thenReturn(List.of(otherCourtCaseToUpdate));
            when(courtCaseRepository.save(courtCase)).thenReturn(courtCase);

            var savedCourtCase = service.createCase(COURT_CODE, CASE_NO, courtCase).block();

            verify(telemetryService).trackCourtCaseEvent(TelemetryEventType.COURT_CASE_CREATED, courtCase);
            verify(telemetryService).trackCourtCaseEvent(TelemetryEventType.DEFENDANT_LINKED, courtCase);
            verify(courtCaseRepository).save(courtCase);
            assertThat(savedCourtCase).isNotNull();
            var otherCourtCaseUpdated = EntityHelper.aCourtCaseEntity(CRN, "999", LocalDateTime.now().plusDays(1), PROBATION_STATUS);
            verify(courtCaseRepository, timeout(2000)).saveAll(List.of(otherCourtCaseUpdated));
            verifyNoMoreInteractions(courtCaseRepository, telemetryService);
        }

        @Test
        public void givenNoExistingCase_whenCreateOrUpdateCaseCalledWithoutCrn_thenLogOnlyCreatedEvent() {
            courtCase = EntityHelper.aCourtCaseEntity(null, CASE_NO);
            when(courtRepository.findByCourtCode(COURT_CODE)).thenReturn(Optional.of(courtEntity));
            when(courtCaseRepository.findFirstByCaseNoOrderByCreatedDesc(COURT_CODE, CASE_NO)).thenReturn(Optional.empty());
            when(courtCaseRepository.save(courtCase)).thenReturn(courtCase);

            var savedCourtCase = service.createCase(COURT_CODE, CASE_NO, courtCase).block();

            verify(telemetryService).trackCourtCaseEvent(TelemetryEventType.COURT_CASE_CREATED, courtCase);
            verify(courtCaseRepository).save(courtCase);
            assertThat(savedCourtCase).isNotNull();
        }

        @Test
        public void givenExistingCase_whenCreateOrUpdateCaseCalled_thenLogUpdatedEvent() {
            when(courtRepository.findByCourtCode(COURT_CODE)).thenReturn(Optional.of(courtEntity));
            when(courtCaseRepository.findFirstByCaseNoOrderByCreatedDesc(COURT_CODE, CASE_NO)).thenReturn(Optional.of(courtCase));
            when(courtCaseRepository.save(courtCase)).thenReturn(courtCase);

            var savedCourtCase = service.createCase(COURT_CODE, CASE_NO, courtCase).block();

            verify(telemetryService).trackCourtCaseEvent(TelemetryEventType.COURT_CASE_UPDATED, courtCase);
            verify(courtCaseRepository).save(courtCase);
            assertThat(savedCourtCase).isNotNull();
        }

        @Test
        public void givenExistingCaseWithNullCrn_whenCreateOrUpdateCaseCalledWithCrn_thenLogLinkedEvent() {
            when(courtRepository.findByCourtCode(COURT_CODE)).thenReturn(Optional.of(courtEntity));
            var existingCase = EntityHelper.aCourtCaseEntity(null, CASE_NO);
            when(courtCaseRepository.findFirstByCaseNoOrderByCreatedDesc(COURT_CODE, CASE_NO)).thenReturn(Optional.of(existingCase));
            when(courtCaseRepository.save(existingCase)).thenReturn(existingCase);

            var savedCourtCase = service.createCase(COURT_CODE, CASE_NO, EntityHelper.aCourtCaseEntity(CRN, CASE_NO)).block();

            verify(telemetryService).trackCourtCaseEvent(TelemetryEventType.COURT_CASE_UPDATED, existingCase);
            verify(telemetryService).trackCourtCaseEvent(TelemetryEventType.DEFENDANT_LINKED, courtCase);
            verify(courtCaseRepository).save(existingCase);
            assertThat(savedCourtCase).isNotNull();
            verifyNoMoreInteractions(telemetryService);
        }

        @Test
        public void givenExistingCaseWithCrn_whenCreateOrUpdateCaseCalledWithNullCrn_thenLogUnLinkedEvent() {

            when(courtRepository.findByCourtCode(COURT_CODE)).thenReturn(Optional.of(courtEntity));
            when(courtCaseRepository.findFirstByCaseNoOrderByCreatedDesc(COURT_CODE, CASE_NO)).thenReturn(Optional.of(courtCase));
            when(courtCaseRepository.save(courtCase)).thenReturn(courtCase);

            var updatedCourtCase = EntityHelper.aCourtCaseEntity(null, CASE_NO);

            var savedCourtCase = service.createCase(COURT_CODE, CASE_NO, updatedCourtCase).block();

            verify(telemetryService).trackCourtCaseEvent(TelemetryEventType.COURT_CASE_UPDATED, courtCase);
            verify(telemetryService).trackCourtCaseEvent(TelemetryEventType.DEFENDANT_UNLINKED, courtCase);
            verify(courtCaseRepository).save(updatedCourtCase);
            assertThat(savedCourtCase).isNotNull();
            verifyNoMoreInteractions(telemetryService);
        }
    }

    @ExtendWith(MockitoExtension.class)
    @Nested
    @DisplayName("Tests for createCase by case ID")
    class CreateUpdateByCaseIdTest {

        private CourtCaseEntity courtCase;

        @BeforeEach
        void setup() {
            service = new ImmutableCourtCaseService(courtRepository, courtCaseRepository, telemetryService, groupedOffenderMatchRepository, false);
            lenient().when(courtRepository.findByCourtCode(COURT_CODE)).thenReturn(Optional.of(courtEntity));
            courtCase = EntityHelper.aCourtCaseEntity(CRN, CASE_NO);
        }

        @Test
        public void givenNoExistingCase_whenCreateOrUpdateCaseCalledWithCrn_thenLogCreatedAndLinkedEvent() {
            var otherCourtCaseToUpdate = EntityHelper.aCourtCaseEntity(CRN, "999", LocalDateTime.now().plusDays(1), "Current");
            when(courtCaseRepository.findFirstByCaseIdOrderByIdDesc(CASE_ID)).thenReturn(Optional.empty());
            when(courtCaseRepository.findOtherCurrentCasesByCrnNotCaseId(CRN, CASE_ID)).thenReturn(List.of(otherCourtCaseToUpdate));
            when(courtCaseRepository.save(courtCase)).thenReturn(courtCase);

            var savedCourtCase = service.createCase(CASE_ID, courtCase).block();

            verify(telemetryService).trackCourtCaseEvent(TelemetryEventType.COURT_CASE_CREATED, courtCase);
            verify(telemetryService).trackCourtCaseEvent(TelemetryEventType.DEFENDANT_LINKED, courtCase);
            verify(courtCaseRepository).save(courtCase);
            assertThat(savedCourtCase).isNotNull();
            var otherCourtCaseUpdated = EntityHelper.aCourtCaseEntity(CRN, "999", LocalDateTime.now().plusDays(1), PROBATION_STATUS);
            verify(courtCaseRepository, timeout(2000)).saveAll(List.of(otherCourtCaseUpdated));
            verifyNoMoreInteractions(courtCaseRepository, telemetryService);
        }

        @Test
        public void givenNoExistingCase_whenCreateCaseCalledWithoutCrn_thenLogOnlyCreatedEvent() {

            courtCase = EntityHelper.aCourtCaseEntity(null, CASE_NO);
            when(courtCaseRepository.findFirstByCaseIdOrderByIdDesc(CASE_ID)).thenReturn(Optional.empty());
            when(courtCaseRepository.save(courtCase)).thenReturn(courtCase);

            var savedCourtCase = service.createCase(CASE_ID, courtCase).block();

            verify(telemetryService).trackCourtCaseEvent(TelemetryEventType.COURT_CASE_CREATED, courtCase);
            verify(courtCaseRepository).save(courtCase);
            assertThat(savedCourtCase).isNotNull();
        }

        @Test
        public void givenExistingCase_whenCreateOrUpdateCaseCalled_thenLogUpdatedEvent() {
            when(courtCaseRepository.findFirstByCaseIdOrderByIdDesc(CASE_ID)).thenReturn(Optional.of(courtCase));
            when(courtCaseRepository.save(courtCase)).thenReturn(courtCase);

            var savedCourtCase = service.createCase(CASE_ID, courtCase).block();

            verify(telemetryService).trackCourtCaseEvent(TelemetryEventType.COURT_CASE_UPDATED, courtCase);
            verify(courtCaseRepository).save(courtCase);
            assertThat(savedCourtCase).isNotNull();
            verifyNoMoreInteractions(telemetryService);
        }

        @Test
        public void givenExistingCaseWithNullCrn_whenCreateOrUpdateCaseCalledWithCrn_thenLogLinkedEvent() {
            var existingCase = EntityHelper.aCourtCaseEntity(null, CASE_NO);
            when(courtCaseRepository.findFirstByCaseIdOrderByIdDesc(CASE_ID)).thenReturn(Optional.of(existingCase));
            when(courtCaseRepository.save(existingCase)).thenReturn(existingCase);

            var savedCourtCase = service.createCase(CASE_ID, EntityHelper.aCourtCaseEntity(CRN, CASE_NO)).block();

            verify(telemetryService).trackCourtCaseEvent(TelemetryEventType.COURT_CASE_UPDATED, existingCase);
            verify(telemetryService).trackCourtCaseEvent(TelemetryEventType.DEFENDANT_LINKED, courtCase);
            verify(courtCaseRepository).save(existingCase);
            assertThat(savedCourtCase).isNotNull();
            verifyNoMoreInteractions(telemetryService);
        }

        @Test
        public void givenExistingCaseWithCrn_whenCreateOrUpdateCaseCalledWithNullCrn_thenLogUnLinkedEvent() {
            when(courtCaseRepository.findFirstByCaseIdOrderByIdDesc(CASE_ID)).thenReturn(Optional.of(courtCase));
            when(courtCaseRepository.save(courtCase)).thenReturn(courtCase);

            var updatedCourtCase = EntityHelper.aCourtCaseEntity(null, CASE_NO);

            var savedCourtCase = service.createCase(CASE_ID, updatedCourtCase).block();

            verify(telemetryService).trackCourtCaseEvent(TelemetryEventType.COURT_CASE_UPDATED, courtCase);
            verify(telemetryService).trackCourtCaseEvent(TelemetryEventType.DEFENDANT_UNLINKED, courtCase);
            verify(courtCaseRepository).save(updatedCourtCase);
            assertThat(savedCourtCase).isNotNull();
            verifyNoMoreInteractions(telemetryService);
        }

        @Test
        public void givenUnknownCourtCode_whenCreateOrUpdateCase_thenThrowException() {
            when(courtRepository.findByCourtCode("XXX")).thenThrow(new EntityNotFoundException("not found court"));
            courtCase = CourtCaseEntity.builder()
                    .courtCode("XXX")
                        .caseId(CASE_ID)
                            .build();

            Assertions.assertThrows(EntityNotFoundException.class, () -> {
                service.createCase(CASE_ID, courtCase).block();
            });
            verify(courtRepository).findByCourtCode("XXX");
            verifyNoMoreInteractions(courtRepository, courtCaseRepository, telemetryService);
        }

        @Test
        public void givenCaseIdMismatch_whenCreateOrUpdateCase_thenThrowException() {
            courtCase = CourtCaseEntity.builder()
                .courtCode(COURT_CODE)
                .caseId("xcx")
                .build();
            Assertions.assertThrows(ConflictingInputException.class, () -> {
                service.createCase(CASE_ID, courtCase).block();
            });
            verify(courtRepository).findByCourtCode(COURT_CODE);
            verifyNoMoreInteractions(courtRepository, courtCaseRepository, telemetryService);
        }
    }

    @ExtendWith(MockitoExtension.class)
    @Nested
    @DisplayName("Tests for filterCasesByCourtAndDate")
    class FilterTest {
        private final LocalDateTime CREATED_AFTER = LocalDateTime.of(2020, 10, 9, 12, 50);
        private final LocalDate SEARCH_DATE = LocalDate.of(2020, 1, 16);

        @Mock
        private List<CourtCaseEntity> caseList;

        @BeforeEach
        void setup() {
            service = new ImmutableCourtCaseService(courtRepository, courtCaseRepository, telemetryService, groupedOffenderMatchRepository, false);
        }

        @Test
        void givenCreatedBeforeIsNull_filterByDateShouldRetrieveCourtCasesFromRepository() {
            when(courtRepository.findByCourtCode(COURT_CODE)).thenReturn(Optional.of(courtEntity));
            when(courtEntity.getCourtCode()).thenReturn(COURT_CODE);
            var startTime = LocalDateTime.of(SEARCH_DATE, LocalTime.MIDNIGHT);
            var endTime = startTime.plusDays(1);
            when(courtCaseRepository.findByCourtCodeAndSessionStartTime(COURT_CODE, startTime, endTime, CREATED_AFTER, CREATED_BEFORE))
                .thenReturn(caseList);

            var courtCaseEntities = service.filterCases(COURT_CODE, SEARCH_DATE, CREATED_AFTER, CREATED_BEFORE);

            assertThat(courtCaseEntities).isEqualTo(caseList);
            verify(courtCaseRepository).findByCourtCodeAndSessionStartTime(COURT_CODE, startTime, endTime, CREATED_AFTER, CREATED_BEFORE);
            verifyNoMoreInteractions(courtCaseRepository);
        }

        @Test
        void givenUseExtendedCases_filterByHearingDayShouldRetrieveCourtCasesFromRepository() {
            service = new ImmutableCourtCaseService(courtRepository, courtCaseRepository, telemetryService, groupedOffenderMatchRepository, true);
            when(courtRepository.findByCourtCode(COURT_CODE)).thenReturn(Optional.of(courtEntity));
            when(courtEntity.getCourtCode()).thenReturn(COURT_CODE);
            when(courtCaseRepository.findByCourtCodeAndHearingDay(COURT_CODE, SEARCH_DATE, CREATED_AFTER, CREATED_BEFORE))
                .thenReturn(caseList);

            var courtCaseEntities = service.filterCases(COURT_CODE, SEARCH_DATE, CREATED_AFTER, CREATED_BEFORE);

            assertThat(courtCaseEntities).isEqualTo(caseList);
            verify(courtCaseRepository).findByCourtCodeAndHearingDay(COURT_CODE, SEARCH_DATE, CREATED_AFTER, CREATED_BEFORE);
            verifyNoMoreInteractions(courtCaseRepository);
        }

        @Test
        void givenCreatedBeforeIsNotNull_filterByDateShouldRetrieveCourtCasesFromRepository() {
            when(courtRepository.findByCourtCode(COURT_CODE)).thenReturn(Optional.of(courtEntity));
            when(courtEntity.getCourtCode()).thenReturn(COURT_CODE);
            var startTime = LocalDateTime.of(SEARCH_DATE, LocalTime.MIDNIGHT);
            var endTime = startTime.plusDays(1);
            when(courtCaseRepository.findByCourtCodeAndSessionStartTime(eq(COURT_CODE), eq(startTime), eq(endTime), eq(CREATED_AFTER), eq(CREATED_BEFORE)))
                .thenReturn(caseList);

            var courtCaseEntities = service.filterCases(COURT_CODE, SEARCH_DATE, CREATED_AFTER, CREATED_BEFORE);

            assertThat(courtCaseEntities).isEqualTo(caseList);
        }

        @Test
        void givenCreatedBeforeIsNull_filterByDateShouldThrowNotFoundExceptionIfCourtCodeNotFound() {
            when(courtRepository.findByCourtCode(COURT_CODE)).thenReturn(Optional.empty());

            var exception = catchThrowable(() ->
                service.filterCases(COURT_CODE, SEARCH_DATE, CREATED_AFTER, null));
            assertThat(exception).isInstanceOf(EntityNotFoundException.class)
                .hasMessageContaining("Court " + COURT_CODE + " not found");

        }

        @Test
        void whenFilterByCourtAndDateForLastModified_thenReturn() {
            final var expectedLastModified = LocalDateTime.of(2021, 6, 1, 16, 59, 59);
            final var startTime = LocalDateTime.of(SEARCH_DATE, LocalTime.MIDNIGHT);
            final var endTime = LocalDateTime.of(SEARCH_DATE.plusDays(1), LocalTime.MIDNIGHT);
            when(courtCaseRepository.findLastModified(COURT_CODE, startTime, endTime))
                    .thenReturn(Optional.of(expectedLastModified));

            var lastModified = service.filterCasesLastModified(COURT_CODE, SEARCH_DATE);

            assertThat(lastModified).isPresent();
            assertThat(lastModified.get()).isEqualTo(expectedLastModified);
        }

        @Test
        void whenFilterByCourtAndDateForLastModified_andNoneFound_thenReturnEmpty() {
            final var startTime = LocalDateTime.of(SEARCH_DATE, LocalTime.MIDNIGHT);
            final var endTime = LocalDateTime.of(SEARCH_DATE.plusDays(1), LocalTime.MIDNIGHT);
            when(courtCaseRepository.findLastModified(COURT_CODE, startTime, endTime))
                    .thenReturn(Optional.empty());

            var lastModified = service.filterCasesLastModified(COURT_CODE, SEARCH_DATE);

            assertThat(lastModified).isEmpty();
        }
    }

    @ExtendWith(MockitoExtension.class)
    @Nested
    @DisplayName("Tests for getting case by case no or case id")
    class GetCaseByCaseNoOrCaseIdTest {

        @BeforeEach
        void setup() {
            service = new ImmutableCourtCaseService(courtRepository, courtCaseRepository, telemetryService, groupedOffenderMatchRepository, false);
        }

        @Test
        void getCourtCaseShouldRetrieveCaseFromRepository() {
            when(courtRepository.findByCourtCode(COURT_CODE)).thenReturn(Optional.of(courtEntity));
            when(courtCaseRepository.findByCourtCodeAndCaseNo(COURT_CODE, CASE_NO)).thenReturn(Optional.of(EntityHelper.aCourtCaseEntity(CRN, CASE_NO)));

            service.getCaseByCaseNumber(COURT_CODE, CASE_NO);
            verify(courtCaseRepository).findByCourtCodeAndCaseNo(COURT_CODE, CASE_NO);
        }

        @Test
        void getCourtCaseShouldThrowNotFoundException() {
            when(courtRepository.findByCourtCode(COURT_CODE)).thenReturn(Optional.of(courtEntity));
            when(courtCaseRepository.findByCourtCodeAndCaseNo(COURT_CODE, CASE_NO)).thenReturn(Optional.empty());

            var exception = catchThrowable(() ->
                service.getCaseByCaseNumber(COURT_CODE, CASE_NO)
            );
            assertThat(exception).isInstanceOf(EntityNotFoundException.class)
                .hasMessageContaining("Case " + CASE_NO + " not found for court " + COURT_CODE);
        }

        @Test
        void getCourtCaseShouldThrowIncorrectCourtException() {
            when(courtRepository.findByCourtCode(COURT_CODE)).thenReturn(Optional.empty());

            var exception = catchThrowable(() ->
                service.getCaseByCaseNumber(COURT_CODE, CASE_NO)
            );
            assertThat(exception).isInstanceOf(EntityNotFoundException.class)
                .hasMessageContaining("Court " + COURT_CODE + " not found");
        }

        @Test
        void givenExistingCaseId_getCourtCaseByCaseId_thenRetrieve() {
            var caseEntityFromRepo = EntityHelper.aCourtCaseEntity(CRN);
            when(courtCaseRepository.findByCaseId(CASE_ID)).thenReturn(Optional.of(caseEntityFromRepo));

            var caseEntity = service.getCaseByCaseId(CASE_ID);
            assertThat(caseEntity).isSameAs(caseEntityFromRepo);
            verify(courtCaseRepository).findByCaseId(CASE_ID);
        }

        @Test
        void givenNonExistentCaseId_whenGetCourtCaseByCaseId_thenThrow() {
            when(courtCaseRepository.findByCaseId(CASE_ID)).thenReturn(Optional.empty());

            var exception = catchThrowable(() ->
                service.getCaseByCaseId(CASE_ID)
            );
            assertThat(exception).isInstanceOf(EntityNotFoundException.class)
                .hasMessageContaining("Case " + CASE_ID + " not found");
        }

        @Test
        void whenGetCourtCaseByIdAndDefendantId_shouldRetrieveCaseFromRepository() {
            final var courtCaseEntity = EntityHelper.aCourtCaseEntity(CRN);
            when(courtCaseRepository.findByCaseIdAndDefendantId(CASE_ID, DEFENDANT_ID)).thenReturn(Optional.of(courtCaseEntity));

            final var entity = service.getCaseByCaseIdAndDefendantId(CASE_ID, DEFENDANT_ID);

            assertThat(entity).isSameAs(courtCaseEntity);
            verify(courtCaseRepository).findByCaseIdAndDefendantId(CASE_ID, DEFENDANT_ID);
            verifyNoMoreInteractions(courtCaseRepository);
        }

        @Test
        void givenNoMatch_whenGetCourtCaseByIdAndDefendantId_shouldThrowException() {
            when(courtCaseRepository.findByCaseIdAndDefendantId(CASE_ID, DEFENDANT_ID)).thenReturn(Optional.empty());

            var exception = catchThrowable(() ->
                service.getCaseByCaseIdAndDefendantId(CASE_ID, DEFENDANT_ID)
            );
            assertThat(exception).isInstanceOf(EntityNotFoundException.class)
                .hasMessageContaining("Case " + CASE_ID + " not found for defendant " + DEFENDANT_ID);
        }

    }

    @ExtendWith(MockitoExtension.class)
    @Nested
    @DisplayName("Tests for createCase where offender matches processing is happening")
    class OffenderMatchesTest {

        @Captor
        private ArgumentCaptor<GroupedOffenderMatchesEntity> matchesCaptor;

        @BeforeEach
        void setup() {
            service = new ImmutableCourtCaseService(courtRepository, courtCaseRepository, telemetryService, groupedOffenderMatchRepository, false);
        }

        @Test
        void givenMismatchInputCourtCode_whenUpdateByCourtAndCase_ThenThrowInputMismatch() {
            when(courtRepository.findByCourtCode(COURT_CODE)).thenReturn(Optional.of(courtEntity));
            var misMatchCourt = "NWS";
            assertThatExceptionOfType(ConflictingInputException.class).isThrownBy(() ->
                service.createCase(misMatchCourt, CASE_NO, EntityHelper.aCourtCaseEntity(CRN, CASE_NO))
            ).withMessage(
                "Case No " + CASE_NO + " and Court Code " + misMatchCourt + " do not match with values from body " + CASE_NO + " and " + COURT_CODE);
        }

        @Test
        void givenMismatchInputCaseNo_whenUpdateByCourtAndCase_ThenThrowInputMismatch() {
            when(courtRepository.findByCourtCode(COURT_CODE)).thenReturn(Optional.of(courtEntity));
            var misMatchCaseNo = "999";
            assertThatExceptionOfType(ConflictingInputException.class).isThrownBy(() ->
                service.createCase(COURT_CODE, misMatchCaseNo, EntityHelper.aCourtCaseEntity(CRN, CASE_NO))
            ).withMessage("Case No " + misMatchCaseNo + " and Court Code " + COURT_CODE + " do not match with values from body " + CASE_NO + " and "
                + COURT_CODE);
        }

        @Test
        public void givenOffenderMatchesExistForCase_whenCrnUpdated_thenUpdateMatches() {
            when(courtRepository.findByCourtCode(COURT_CODE)).thenReturn(Optional.of(courtEntity));
            var existingCase = EntityHelper.aCourtCaseEntity(CRN, CASE_NO);
            when(groupedOffenderMatchRepository.findByCourtCodeAndCaseNo(COURT_CODE, CASE_NO)).thenReturn(buildOffenderMatches());
            var caseToUpdate = EntityHelper.aCourtCaseEntity(CRN, CASE_NO);

            when(courtCaseRepository.findFirstByCaseNoOrderByCreatedDesc(COURT_CODE, CASE_NO)).thenReturn(Optional.of(existingCase));
            when(courtCaseRepository.save(existingCase)).thenReturn(existingCase);

            service.createCase(COURT_CODE, CASE_NO, caseToUpdate).block();

            verify(groupedOffenderMatchRepository).save(matchesCaptor.capture());

            final var groupedOffenderMatches = matchesCaptor.getValue();

            var correctMatch = groupedOffenderMatches.getOffenderMatches().get(0);
            assertThat(correctMatch.getCrn()).isEqualTo(CRN);
            assertThat(correctMatch.getConfirmed()).isEqualTo(true);
            assertThat(correctMatch.getRejected()).isEqualTo(false);

            var rejectedMatch1 = groupedOffenderMatches.getOffenderMatches().get(1);
            assertThat(rejectedMatch1.getCrn()).isEqualTo("Rejected CRN 1");
            assertThat(rejectedMatch1.getConfirmed()).isEqualTo(false);
            assertThat(rejectedMatch1.getRejected()).isEqualTo(true);
        }

        @Test
        public void givenOffenderMatchesExistForCase_whenCrnRemoved_thenRejectAllMatches() {
            when(courtRepository.findByCourtCode(COURT_CODE)).thenReturn(Optional.of(courtEntity));
            when(groupedOffenderMatchRepository.findByCourtCodeAndCaseNo(COURT_CODE, CASE_NO)).thenReturn(buildOffenderMatches());
            var caseToUpdate = EntityHelper.aCourtCaseEntity(null, CASE_NO);
            var existingCase = EntityHelper.aCourtCaseEntity(CRN, CASE_NO);

            when(courtCaseRepository.findFirstByCaseNoOrderByCreatedDesc(COURT_CODE, CASE_NO)).thenReturn(Optional.of(existingCase));
            when(courtCaseRepository.save(existingCase)).thenReturn(existingCase);

            service.createCase(COURT_CODE, CASE_NO, caseToUpdate).block();

            verify(groupedOffenderMatchRepository).save(matchesCaptor.capture());

            final var groupedOffenderMatches = matchesCaptor.getValue();

            var rejectedMatch1 = groupedOffenderMatches.getOffenderMatches().get(0);
            assertThat(rejectedMatch1.getCrn()).isEqualTo(CRN);
            assertThat(rejectedMatch1.getConfirmed()).isEqualTo(false);
            assertThat(rejectedMatch1.getRejected()).isEqualTo(true);

            var rejectedMatch2 = groupedOffenderMatches.getOffenderMatches().get(1);
            assertThat(rejectedMatch2.getCrn()).isEqualTo("Rejected CRN 1");
            assertThat(rejectedMatch2.getConfirmed()).isEqualTo(false);
            assertThat(rejectedMatch2.getRejected()).isEqualTo(true);

        }

        @Test
        public void whenUpdateMatches_thenLogRejectedAndConfirmedEvents() {
            when(courtRepository.findByCourtCode(COURT_CODE)).thenReturn(Optional.of(courtEntity));
            var caseToUpdate = EntityHelper.aCourtCaseEntity(CRN, CASE_NO);
            var existingCase = EntityHelper.aCourtCaseEntity(null, CASE_NO);

            when(groupedOffenderMatchRepository.findByCourtCodeAndCaseNo(COURT_CODE, CASE_NO)).thenReturn(buildOffenderMatches());
            when(courtCaseRepository.findFirstByCaseNoOrderByCreatedDesc(COURT_CODE, CASE_NO)).thenReturn(Optional.of(existingCase));
            when(courtCaseRepository.save(existingCase)).thenReturn(existingCase);

            service.createCase(COURT_CODE, CASE_NO, caseToUpdate).block();

            verify(groupedOffenderMatchRepository).save(matchesCaptor.capture());

            var matches = matchesCaptor.getValue();

            var confirmedMatch = matches.getOffenderMatches().get(0);
            verify(telemetryService).trackMatchEvent(TelemetryEventType.MATCH_CONFIRMED, confirmedMatch, caseToUpdate);

            var rejectedMatch1 = matches.getOffenderMatches().get(1);
            verify(telemetryService).trackMatchEvent(TelemetryEventType.MATCH_REJECTED, rejectedMatch1, caseToUpdate);
        }

        @Test
        public void givenMatchesDontExistForCase_whenCrnUpdated_thenDontThrowException() {
            when(courtRepository.findByCourtCode(COURT_CODE)).thenReturn(Optional.of(courtEntity));
            var caseToUpdate = EntityHelper.aCourtCaseEntity(CRN, CASE_NO);

            when(courtCaseRepository.findFirstByCaseNoOrderByCreatedDesc(COURT_CODE, CASE_NO)).thenReturn(Optional.of(caseToUpdate));
            when(courtCaseRepository.save(caseToUpdate)).thenReturn(caseToUpdate);

            service.createCase(COURT_CODE, CASE_NO, caseToUpdate).block();
        }

        @Test
        public void givenMatchesAreNullForCase_whenCrnUpdated_thenDontThrowException() {
            when(courtRepository.findByCourtCode(COURT_CODE)).thenReturn(Optional.of(courtEntity));
            when(groupedOffenderMatchRepository.findByCourtCodeAndCaseNo(COURT_CODE, CASE_NO)).thenReturn(Optional.empty());
            var caseToUpdate = EntityHelper.aCourtCaseEntity(CRN, CASE_NO);
            when(courtCaseRepository.findFirstByCaseNoOrderByCreatedDesc(COURT_CODE, CASE_NO)).thenReturn(Optional.of(caseToUpdate));
            when(courtCaseRepository.save(caseToUpdate)).thenReturn(caseToUpdate);

            service.createCase(COURT_CODE, CASE_NO, caseToUpdate).block();
        }

        @NonNull
        public Optional<GroupedOffenderMatchesEntity> buildOffenderMatches() {
            return Optional.ofNullable(GroupedOffenderMatchesEntity.builder()
                .courtCode(COURT_CODE)
                .caseNo(CASE_NO)
                .offenderMatches(Arrays.asList(OffenderMatchEntity.builder()
                        .crn(CRN)
                        .confirmed(false)
                        .rejected(false)
                        .build(),
                    OffenderMatchEntity.builder()
                        .crn("Rejected CRN 1")
                        .confirmed(false)
                        .rejected(false)
                        .build()))
                .build());
        }
    }

    @ExtendWith(MockitoExtension.class)
    @Nested
    @DisplayName("Tests for updateStatus")
    class PostUpdateStatusTest {

        @BeforeEach
        void setup() {
            service = new ImmutableCourtCaseService(courtRepository, courtCaseRepository, telemetryService, groupedOffenderMatchRepository, false);
        }

        @Test
        public void givenNoExistingCase_whenCreateOrUpdateCaseCalledWithCrn_thenLogCreatedAndLinkedEvent() {

            var now = LocalDateTime.now();
            var courtCaseToIgnore = EntityHelper.aCourtCaseEntity(CRN, "1235", now.plusDays(1), "Current");
            var courtCaseToUpdate = EntityHelper.aCourtCaseEntity(CRN, "1236", now.plusDays(1), "Previously known");

            when(courtCaseRepository.findOtherCurrentCasesByCrn(CRN, CASE_NO)).thenReturn(List.of(courtCaseToIgnore, courtCaseToUpdate));

            service.updateOtherProbationStatusForCrn(CRN, "Current", CASE_NO);

            // The case to be saved will be same as the updated with case no 1236 but with Current as the
            var expectedCourtCaseToSave = EntityHelper.aCourtCaseEntity(CRN, "1236", now.plusDays(1), "Current");
            verify(courtCaseRepository).saveAll(List.of(expectedCourtCaseToSave));
        }
    }

}
