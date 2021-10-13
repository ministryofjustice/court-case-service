package uk.gov.justice.probation.courtcaseservice.service;

import lombok.Data;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.ArgumentMatcher;
import org.mockito.Captor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.lang.NonNull;
import uk.gov.justice.probation.courtcaseservice.controller.exceptions.ConflictingInputException;
import uk.gov.justice.probation.courtcaseservice.controller.model.ProbationStatus;
import uk.gov.justice.probation.courtcaseservice.jpa.entity.CourtCaseEntity;
import uk.gov.justice.probation.courtcaseservice.jpa.entity.CourtEntity;
import uk.gov.justice.probation.courtcaseservice.jpa.entity.DefendantEntity;
import uk.gov.justice.probation.courtcaseservice.jpa.entity.EntityHelper;
import uk.gov.justice.probation.courtcaseservice.jpa.entity.GroupedOffenderMatchesEntity;
import uk.gov.justice.probation.courtcaseservice.jpa.entity.OffenderMatchEntity;
import uk.gov.justice.probation.courtcaseservice.jpa.repository.CourtCaseRepository;
import uk.gov.justice.probation.courtcaseservice.jpa.repository.CourtRepository;
import uk.gov.justice.probation.courtcaseservice.jpa.repository.GroupedOffenderMatchRepository;
import uk.gov.justice.probation.courtcaseservice.service.exceptions.EntityNotFoundException;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatExceptionOfType;
import static org.assertj.core.api.ThrowableAssert.catchThrowable;
import static org.mockito.ArgumentMatchers.argThat;
import static org.mockito.Mockito.eq;
import static org.mockito.Mockito.lenient;
import static org.mockito.Mockito.timeout;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoMoreInteractions;
import static org.mockito.Mockito.when;
import static uk.gov.justice.probation.courtcaseservice.jpa.entity.EntityHelper.CASE_ID;
import static uk.gov.justice.probation.courtcaseservice.jpa.entity.EntityHelper.COURT_CODE;
import static uk.gov.justice.probation.courtcaseservice.jpa.entity.EntityHelper.CRN;
import static uk.gov.justice.probation.courtcaseservice.jpa.entity.EntityHelper.DEFENDANT_ID;
import static uk.gov.justice.probation.courtcaseservice.jpa.entity.EntityHelper.PROBATION_STATUS;
import static uk.gov.justice.probation.courtcaseservice.jpa.entity.EntityHelper.aDefendantEntity;

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
        void givenNoExistingCase_whenCreateOrUpdateCaseCalledWithCrn_thenLogCreatedAndLinkedEvent() {
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
        void givenNoExistingCase_whenCreateOrUpdateCaseCalledWithoutCrn_thenLogOnlyCreatedEvent() {
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
        void givenExistingCase_whenCreateOrUpdateCaseCalled_thenLogUpdatedEvent() {
            when(courtRepository.findByCourtCode(COURT_CODE)).thenReturn(Optional.of(courtEntity));
            when(courtCaseRepository.findFirstByCaseNoOrderByCreatedDesc(COURT_CODE, CASE_NO)).thenReturn(Optional.of(courtCase));
            when(courtCaseRepository.save(courtCase)).thenReturn(courtCase);

            var savedCourtCase = service.createCase(COURT_CODE, CASE_NO, courtCase).block();

            verify(telemetryService).trackCourtCaseEvent(TelemetryEventType.COURT_CASE_UPDATED, courtCase);
            verify(courtCaseRepository).save(courtCase);
            assertThat(savedCourtCase).isNotNull();
        }

        @Test
        void givenExistingCaseWithNullCrn_whenCreateOrUpdateCaseCalledWithCrn_thenLogLinkedEvent() {
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
        void givenExistingCaseWithCrn_whenCreateOrUpdateCaseCalledWithNullCrn_thenLogUnLinkedEvent() {

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
    @DisplayName("Tests for createCase by case ID and defendant ID")
    class CreateUpdateByCaseAndDefendantIdTest {

        private CourtCaseEntity incomingCourtCase;
        private DefendantEntity defendant;

        @BeforeEach
        void setup() {
            service = new ImmutableCourtCaseService(courtRepository, courtCaseRepository, telemetryService, groupedOffenderMatchRepository, false);
            lenient().when(courtRepository.findByCourtCode(COURT_CODE)).thenReturn(Optional.of(courtEntity));
            incomingCourtCase = EntityHelper.aCourtCaseEntity(CRN, CASE_NO);
            defendant = DefendantEntity.builder().defendantId(DEFENDANT_ID).build();
        }

        @Test
        void givenUnknownCourtCode_whenCreateOrUpdateCase_thenThrowException() {
            when(courtRepository.findByCourtCode("XXX")).thenThrow(new EntityNotFoundException("not found court"));
            incomingCourtCase = CourtCaseEntity.builder()
                .courtCode("XXX")
                .caseId(CASE_ID)
                .build();

            Assertions.assertThrows(EntityNotFoundException.class, () -> {
                service.createUpdateCaseForSingleDefendantId(CASE_ID, DEFENDANT_ID, incomingCourtCase).block();
            });
            verify(courtRepository).findByCourtCode("XXX");
            verifyNoMoreInteractions(courtRepository, courtCaseRepository, telemetryService);
        }

        @Test
        void givenCaseIdMismatch_whenCreateOrUpdateCase_thenThrowException() {
            incomingCourtCase = EntityHelper.aCourtCaseEntityWithCrn(CRN);
            Assertions.assertThrows(ConflictingInputException.class, () -> {
                service.createUpdateCaseForSingleDefendantId("OTHER-CASE-ID", DEFENDANT_ID, incomingCourtCase).block();
            });
            verify(courtRepository).findByCourtCode(COURT_CODE);
            verifyNoMoreInteractions(courtRepository, courtCaseRepository, telemetryService);
        }

        @Test
        void givenDefendantIdMismatch_whenCreateOrUpdateCase_thenThrowException() {
            Assertions.assertThrows(ConflictingInputException.class, () -> {
                service.createUpdateCaseForSingleDefendantId(CASE_ID, "OTHER-DEFENDANT-ID", incomingCourtCase).block();
            });
            verify(courtRepository).findByCourtCode(COURT_CODE);
            verifyNoMoreInteractions(courtRepository, courtCaseRepository, telemetryService);
        }

        @Test
        void givenSingleNewLinkedCase_whenCreateOrUpdateCaseCalledWithCrn_thenLogUpdatedEventAndSave() {
            when(courtCaseRepository.findByCaseIdAndDefendantId(CASE_ID, DEFENDANT_ID)).thenReturn(Optional.empty());
            when(courtCaseRepository.save(incomingCourtCase)).thenReturn(incomingCourtCase);
            when(courtCaseRepository.findOtherCurrentCasesByCrnNotCaseId(CRN, CASE_ID)).thenReturn(Collections.emptyList());

            var savedCourtCase = service.createUpdateCaseForSingleDefendantId(CASE_ID, DEFENDANT_ID, incomingCourtCase).block();

            verify(telemetryService).trackCourtCaseEvent(TelemetryEventType.COURT_CASE_CREATED, incomingCourtCase);
            verify(telemetryService).trackCourtCaseEvent(TelemetryEventType.DEFENDANT_LINKED, incomingCourtCase);
            verify(courtCaseRepository).findByCaseIdAndDefendantId(CASE_ID, DEFENDANT_ID);
            verify(courtCaseRepository).save(incomingCourtCase);
            verify(courtCaseRepository).findOtherCurrentCasesByCrnNotCaseId(CRN, CASE_ID);
            assertThat(savedCourtCase).isSameAs(incomingCourtCase);

            verifyNoMoreInteractions(courtCaseRepository, telemetryService, groupedOffenderMatchRepository);
        }

        @Test
        void givenSingleNewUnlinkedCase_whenCreateOrUpdateCaseCalledWithCrn_thenLogUpdatedEventAndSave() {
            incomingCourtCase = EntityHelper.aCourtCaseEntity(CASE_ID);
            when(courtCaseRepository.findByCaseIdAndDefendantId(CASE_ID, DEFENDANT_ID)).thenReturn(Optional.empty());
            when(courtCaseRepository.save(incomingCourtCase)).thenReturn(incomingCourtCase);

            var savedCourtCase = service.createUpdateCaseForSingleDefendantId(CASE_ID, DEFENDANT_ID, incomingCourtCase).block();

            verify(telemetryService).trackCourtCaseEvent(TelemetryEventType.COURT_CASE_CREATED, incomingCourtCase);
            verify(courtCaseRepository).findByCaseIdAndDefendantId(CASE_ID, DEFENDANT_ID);
            verify(courtCaseRepository).save(incomingCourtCase);
            assertThat(savedCourtCase).isSameAs(incomingCourtCase);

            verifyNoMoreInteractions(courtCaseRepository, telemetryService, groupedOffenderMatchRepository);
        }

        @Test
        void givenSingleExistingCaseLinkedCase_whenCreateOrUpdateCaseCalledWithoutCrnToUnlink_thenLogUpdatedEventAndSave() {
            var updatedCase = EntityHelper.aCourtCaseEntity(CASE_ID).withCourtRoom("02").withDefendants(List.of(defendant));
            var existingCase = EntityHelper.aCourtCaseEntityWithCrn(CRN);
            when(courtCaseRepository.findByCaseIdAndDefendantId(CASE_ID, DEFENDANT_ID)).thenReturn(Optional.of(existingCase));
            when(courtCaseRepository.save(updatedCase)).thenReturn(updatedCase);

            var savedCourtCase = service.createUpdateCaseForSingleDefendantId(CASE_ID, DEFENDANT_ID, updatedCase).block();

            verify(telemetryService).trackCourtCaseEvent(TelemetryEventType.COURT_CASE_UPDATED, updatedCase);
            verify(telemetryService).trackCourtCaseEvent(TelemetryEventType.DEFENDANT_UNLINKED, existingCase);
            verify(courtCaseRepository).findByCaseIdAndDefendantId(CASE_ID, DEFENDANT_ID);
            verify(courtCaseRepository).save(updatedCase);
            assertThat(savedCourtCase).isSameAs(updatedCase);
            verifyNoMoreInteractions(courtCaseRepository, telemetryService);
        }

        @Test
        void givenSingleExistingLinkedCase_whenCreateOrUpdateCaseCalledWithoutCrn_thenLogUpdatedEventAndSave() {
            var updatedCase = EntityHelper.aCourtCaseEntity(CASE_ID).withCourtRoom("02").withDefendants(List.of(defendant)).withCrn(CRN);
            var existingCase = EntityHelper.aCourtCaseEntity(CASE_ID);
            when(courtCaseRepository.findByCaseIdAndDefendantId(CASE_ID, DEFENDANT_ID)).thenReturn(Optional.of(existingCase));
            when(courtCaseRepository.findOtherCurrentCasesByCrnNotCaseId(CRN, CASE_ID)).thenReturn(Collections.emptyList());
            when(courtCaseRepository.save(updatedCase)).thenReturn(updatedCase);

            var savedCourtCase = service.createUpdateCaseForSingleDefendantId(CASE_ID, DEFENDANT_ID, updatedCase).block();

            verify(telemetryService).trackCourtCaseEvent(TelemetryEventType.COURT_CASE_UPDATED, updatedCase);
            verify(telemetryService).trackCourtCaseEvent(TelemetryEventType.DEFENDANT_LINKED, updatedCase);
            verify(courtCaseRepository).findByCaseIdAndDefendantId(CASE_ID, DEFENDANT_ID);
            verify(courtCaseRepository).findOtherCurrentCasesByCrnNotCaseId(CRN, CASE_ID);
            verify(courtCaseRepository).save(updatedCase);
            assertThat(savedCourtCase).isSameAs(updatedCase);
            verifyNoMoreInteractions(courtCaseRepository, telemetryService);
        }

        @Test
        void givenExistingCaseWithMultipleDefendants_whenCreateOrUpdateCaseCalledWithCrn_thenLogCreatedAndLinkedEvent() {
            var otherExistingDefendant = DefendantEntity.builder().defendantId("DEF_ID_2").crn("X99999").build();
            var updatedCase = EntityHelper.aCourtCaseEntity(CASE_ID).withDefendants(List.of(defendant));
            var existingCase = EntityHelper.aCourtCaseEntity(CASE_ID).withDefendants(List.of(defendant, otherExistingDefendant));
            when(courtCaseRepository.findByCaseIdAndDefendantId(CASE_ID, DEFENDANT_ID)).thenReturn(Optional.of(existingCase));
            var expectedSave = new CourtCaseEntityMatcher(CASE_ID, List.of(DEFENDANT_ID, "DEF_ID_2"));
            when(courtCaseRepository.save(argThat(expectedSave))).thenReturn(updatedCase);

            var savedCourtCase = service.createUpdateCaseForSingleDefendantId(CASE_ID, DEFENDANT_ID, updatedCase).block();

            verify(telemetryService).trackCourtCaseEvent(eq(TelemetryEventType.COURT_CASE_UPDATED), eq(updatedCase));
            verify(courtCaseRepository).findByCaseIdAndDefendantId(CASE_ID, DEFENDANT_ID);
            verify(courtCaseRepository).save(argThat(expectedSave));
            assertThat(savedCourtCase).isNotNull();
            verifyNoMoreInteractions(courtCaseRepository, telemetryService);
        }

        @Test
        void whenAddDefendants_thenReturn() {
            var otherExistingDefendant = DefendantEntity.builder().defendantId("DEF_ID_2").crn("X99999").build();
            var updatedCase = EntityHelper.aCourtCaseEntity(CASE_ID).withDefendants(List.of(defendant));
            var existingCase = EntityHelper.aCourtCaseEntity(CASE_ID).withDefendants(List.of(defendant, otherExistingDefendant));

            when(courtCaseRepository.findByCaseIdAndDefendantId(CASE_ID, DEFENDANT_ID)).thenReturn(Optional.of(existingCase));
            var expectedSave = new CourtCaseEntityMatcher(CASE_ID, List.of(DEFENDANT_ID, "DEF_ID_2"));
            when(courtCaseRepository.save(argThat(expectedSave))).thenReturn(updatedCase);

            var savedCourtCase = service.createUpdateCaseForSingleDefendantId(CASE_ID, DEFENDANT_ID, updatedCase).block();

            verify(telemetryService).trackCourtCaseEvent(eq(TelemetryEventType.COURT_CASE_UPDATED), eq(updatedCase));
            verify(courtCaseRepository).findByCaseIdAndDefendantId(CASE_ID, DEFENDANT_ID);
            verify(courtCaseRepository).save(argThat(expectedSave));
            assertThat(savedCourtCase).isNotNull();
            verifyNoMoreInteractions(courtCaseRepository, telemetryService);
        }

    }

    @ExtendWith(MockitoExtension.class)
    @Nested
    @DisplayName("Tests for createCase by case ID with ExtendedCourtCaseRequest")
    class CreateUpdateByCaseIdTest {

        private CourtCaseEntity courtCase;

        @BeforeEach
        void setup() {
            service = new ImmutableCourtCaseService(courtRepository, courtCaseRepository, telemetryService, groupedOffenderMatchRepository, false);
            lenient().when(courtRepository.findByCourtCode(COURT_CODE)).thenReturn(Optional.of(courtEntity));
            courtCase = EntityHelper.aCourtCaseEntity(CRN, CASE_NO);
        }

        @Test
        void givenNoExistingCase_whenCreateOrUpdateCaseCalledWithCrn_thenLogCreatedAndLinkedEvent() {
            var defendantToUpdate = EntityHelper.aDefendantEntity(DEFENDANT_ID).withProbationStatus("CURRENT");
            var otherCourtCaseToUpdate = EntityHelper.aCourtCaseEntity(CASE_ID).withDefendants(List.of(defendantToUpdate));
            when(courtCaseRepository.findFirstByCaseIdOrderByIdDesc(CASE_ID)).thenReturn(Optional.empty());
            when(courtCaseRepository.findOtherCurrentCasesByCrnNotCaseId(CRN, CASE_ID)).thenReturn(List.of(otherCourtCaseToUpdate));
            when(courtCaseRepository.save(courtCase)).thenReturn(courtCase);

            var savedCourtCase = service.createCase(CASE_ID, courtCase).block();

            verify(telemetryService).trackCourtCaseEvent(TelemetryEventType.COURT_CASE_CREATED, courtCase);
            verify(telemetryService).trackCourtCaseEvent(TelemetryEventType.DEFENDANT_LINKED, courtCase);
            verify(courtCaseRepository).save(courtCase);
            assertThat(savedCourtCase).isNotNull();

            var expectedCourtCaseToSave = new CourtCaseEntityListMatcher(CASE_ID, List.of(DEFENDANT_ID));
            verify(courtCaseRepository, timeout(2000)).saveAll(argThat(expectedCourtCaseToSave));

            verifyNoMoreInteractions(courtCaseRepository, telemetryService);
        }

        @Test
        void givenNoExistingCase_whenCreateCaseCalledWithoutCrn_thenLogOnlyCreatedEvent() {

            courtCase = EntityHelper.aCourtCaseEntity(null, CASE_NO);
            when(courtCaseRepository.findFirstByCaseIdOrderByIdDesc(CASE_ID)).thenReturn(Optional.empty());
            when(courtCaseRepository.save(courtCase)).thenReturn(courtCase);

            var savedCourtCase = service.createCase(CASE_ID, courtCase).block();

            verify(telemetryService).trackCourtCaseEvent(TelemetryEventType.COURT_CASE_CREATED, courtCase);
            verify(courtCaseRepository).save(courtCase);
            assertThat(savedCourtCase).isNotNull();
        }

        @Test
        void givenExistingCase_whenCreateOrUpdateCaseCalled_thenLogUpdatedEvent() {
            when(courtCaseRepository.findFirstByCaseIdOrderByIdDesc(CASE_ID)).thenReturn(Optional.of(courtCase));
            when(courtCaseRepository.save(courtCase)).thenReturn(courtCase);

            var savedCourtCase = service.createCase(CASE_ID, courtCase).block();

            verify(telemetryService).trackCourtCaseEvent(TelemetryEventType.COURT_CASE_UPDATED, courtCase);
            verify(courtCaseRepository).save(courtCase);
            assertThat(savedCourtCase).isNotNull();
            verifyNoMoreInteractions(telemetryService);
        }

        @Test
        void givenExistingCaseWithNullCrn_whenCreateOrUpdateCaseCalledWithCrn_thenLogLinkedEvent() {
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
        void givenExistingCaseWithCrn_whenCreateOrUpdateCaseCalledWithNullCrn_thenLogUnLinkedEvent() {
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
        void givenUnknownCourtCode_whenCreateOrUpdateCase_thenThrowException() {
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
        void givenCaseIdMismatch_whenCreateOrUpdateCase_thenThrowException() {
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
            var caseEntityFromRepo = EntityHelper.aCourtCaseEntityWithCrn(CRN);
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
            final var courtCaseEntity = EntityHelper.aCourtCaseEntityWithCrn(CRN);
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
        void givenOffenderMatchesExistForCase_whenCrnUpdated_thenUpdateMatches() {
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
        void givenOffenderMatchesExistForCaseWithMultipleDefendants_whenCrnUpdated_thenUpdateMatches() {
            when(courtRepository.findByCourtCode(COURT_CODE)).thenReturn(Optional.of(courtEntity));
            var existingCase = EntityHelper.aCourtCaseEntity(CRN, CASE_NO, List.of(
                    aDefendantEntity("defendant1"),
                    aDefendantEntity("defendant2")
                    ));
            when(courtCaseRepository.findFirstByCaseIdOrderByIdDesc(CASE_ID)).thenReturn(Optional.of(existingCase));
            when(groupedOffenderMatchRepository.findByCaseIdAndDefendantId(CASE_ID, "defendant1")).thenReturn(buildOffenderMatches());
            when(groupedOffenderMatchRepository.findByCaseIdAndDefendantId(CASE_ID, "defendant2")).thenReturn(buildOffenderMatches());
            var caseToUpdate = EntityHelper.aCourtCaseEntity(CRN, CASE_NO, List.of(
                    aDefendantEntity("defendant1"),
                    aDefendantEntity("defendant2")
            ));

            when(courtCaseRepository.save(existingCase)).thenReturn(existingCase);

            service.createCase(CASE_ID, caseToUpdate).block();

            verify(groupedOffenderMatchRepository, times(2)).save(matchesCaptor.capture());

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
        void givenOffenderMatchesExistForCase_whenCrnRemoved_thenRejectAllMatches() {
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
        void whenUpdateMatches_thenLogRejectedAndConfirmedEvents() {
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
        void givenMatchesDontExistForCase_whenCrnUpdated_thenDontThrowException() {
            when(courtRepository.findByCourtCode(COURT_CODE)).thenReturn(Optional.of(courtEntity));
            var caseToUpdate = EntityHelper.aCourtCaseEntity(CRN, CASE_NO);

            when(courtCaseRepository.findFirstByCaseNoOrderByCreatedDesc(COURT_CODE, CASE_NO)).thenReturn(Optional.of(caseToUpdate));
            when(courtCaseRepository.save(caseToUpdate)).thenReturn(caseToUpdate);

            service.createCase(COURT_CODE, CASE_NO, caseToUpdate).block();
        }

        @Test
        void givenMatchesAreNullForCase_whenCrnUpdated_thenDontThrowException() {
            when(courtRepository.findByCourtCode(COURT_CODE)).thenReturn(Optional.of(courtEntity));
            when(groupedOffenderMatchRepository.findByCourtCodeAndCaseNo(COURT_CODE, CASE_NO)).thenReturn(Optional.empty());
            var caseToUpdate = EntityHelper.aCourtCaseEntity(CRN, CASE_NO);
            when(courtCaseRepository.findFirstByCaseNoOrderByCreatedDesc(COURT_CODE, CASE_NO)).thenReturn(Optional.of(caseToUpdate));
            when(courtCaseRepository.save(caseToUpdate)).thenReturn(caseToUpdate);

            service.createCase(COURT_CODE, CASE_NO, caseToUpdate).block();
        }

        @NonNull
        private Optional<GroupedOffenderMatchesEntity> buildOffenderMatches() {
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
    @DisplayName("Tests for updateOtherProbationStatusForCrn")
    class PostUpdateProbationStatusStatusTest {

        @BeforeEach
        void setup() {
            service = new ImmutableCourtCaseService(courtRepository, courtCaseRepository, telemetryService, groupedOffenderMatchRepository, false);
        }

        @Test
        void givenNullCrn_whenUpdateOtherProbationStatusForCrn_thenDoNothing() {

            service.updateOtherProbationStatusForCrn(null, "Current", CASE_NO);

            verifyNoMoreInteractions(courtCaseRepository);
        }

        @Test
        void givenNoExistingCase_whenUpdateOtherProbationStatusForCrnWithCrn_thenUpdateAndSaveAll() {

            var now = LocalDateTime.now();
            // One will be ignored because the status is the same
            var courtCaseToIgnore = EntityHelper.aCourtCaseEntity(CRN, "1235", now.plusDays(1), "Current");
            var courtCaseToUpdate = EntityHelper.aCourtCaseEntity(CRN, "1236", now.plusDays(1), "Previously known");
            when(courtCaseRepository.findOtherCurrentCasesByCrn(CRN, CASE_NO)).thenReturn(List.of(courtCaseToIgnore, courtCaseToUpdate));

            service.updateOtherProbationStatusForCrn(CRN, "Current", CASE_NO);

            // The case to be saved will be same as the updated with case no 1236 but with Current as the
            var expectedCourtCaseToSave = EntityHelper.aCourtCaseEntity(CRN, "1236", now.plusDays(1), "Current");
            verify(courtCaseRepository).saveAll(List.of(expectedCourtCaseToSave));
        }

        @Test
        void givenNullCrn_whenUpdateOtherProbationStatusForCaseId_thenDoNothing() {

            service.updateOtherProbationStatusForCrnByCaseId(null, "Current", CASE_NO);

            verifyNoMoreInteractions(courtCaseRepository);
        }

        @Test
        void givenNoExistingCase_whenUpdateOtherProbationStatusForCaseIdWithCrn_thenUpdateAndSaveAll() {

            final var newProbationStatus = ProbationStatus.CURRENT.getName();
            // One will be ignored because the status is the same, other ignored because different CRN
            final var defendant1 = EntityHelper.aDefendantEntity("7c011992-edc8-49ae-bb82-251d7da4a067")
                .withCrn("X456765");
            final var defendant2 = EntityHelper.aDefendantEntity("3bf36240-f629-44d4-b563-4a11042e4c5e")
                .withCrn(CRN).withProbationStatus(newProbationStatus);
            final var courtCaseToIgnore = EntityHelper.aCourtCaseEntity("733c63c8-5c0a-42bd-82cd-70c95d563a04")
                .withDefendants(List.of(defendant1, defendant2));

            // This has same CRN but different probation status so will be updated
            final var caseIdToUpdate = "0f04dd95-e257-4a96-9417-a3d6c73bb53c";
            final var courtCaseToUpdate = EntityHelper.aCourtCaseEntity(caseIdToUpdate);
            assertThat(courtCaseToUpdate.getProbationStatus()).isNotEqualTo(newProbationStatus);

            when(courtCaseRepository.findOtherCurrentCasesByCrnNotCaseId(CRN, CASE_ID)).thenReturn(List.of(courtCaseToIgnore, courtCaseToUpdate));

            service.updateOtherProbationStatusForCrnByCaseId(CRN, newProbationStatus, CASE_ID);

            // Only 1 case to save
            var expectedCourtCaseToSave = new CourtCaseEntityListMatcher(caseIdToUpdate, List.of(DEFENDANT_ID));
            verify(courtCaseRepository).saveAll(argThat(expectedCourtCaseToSave));
            verify(courtCaseRepository).findOtherCurrentCasesByCrnNotCaseId(CRN, CASE_ID);
            verifyNoMoreInteractions(courtCaseRepository);
        }

        @Test
        void givenNullOrEmptyDefendants_whenCheck_thenReturnFalse() {
            assertThat(service.hasAnyDefendantsSameCrnDifferentProbationStatus(null, CRN, ProbationStatus.CURRENT.getName())).isFalse();
            assertThat(service.hasAnyDefendantsSameCrnDifferentProbationStatus(Collections.emptyList(), CRN, ProbationStatus.CURRENT.getName())).isFalse();
        }

        @Test
        void givenDefendantsWithSameCrnAndStatus_whenCheck_thenReturnFalse() {
            var probationStatus = ProbationStatus.CURRENT.getName();
            var defendant = EntityHelper.aDefendantEntity().withCrn(CRN).withProbationStatus(probationStatus);

            assertThat(service.hasAnyDefendantsSameCrnDifferentProbationStatus(List.of(defendant), CRN, probationStatus)).isFalse();
        }

        @Test
        void givenDefendantsWithSameCrnAndDifferentStatus_whenCheck_thenReturnTrue() {
            var probationStatus = ProbationStatus.CURRENT.getName();
            var defendant = EntityHelper.aDefendantEntity().withCrn(CRN).withProbationStatus(null);

            assertThat(service.hasAnyDefendantsSameCrnDifferentProbationStatus(List.of(defendant), CRN, probationStatus)).isTrue();
        }

        @Test
        void givenDefendantsWithDifferentCrnAndSameStatus_whenCheck_thenReturnFalse() {
            var probationStatus = ProbationStatus.CURRENT.getName();
            var defendant = EntityHelper.aDefendantEntity().withCrn(null).withProbationStatus(probationStatus);

            assertThat(service.hasAnyDefendantsSameCrnDifferentProbationStatus(List.of(defendant), CRN, probationStatus)).isFalse();
        }

        @Data
        class EntityMatcher implements ArgumentMatcher<GroupedOffenderMatchesEntity> {

            private final String defendantId;
            private final String caseId;

            @Override
            public boolean matches(GroupedOffenderMatchesEntity argument) {
                return defendantId.equals(argument.getDefendantId()) && caseId.equals(argument.getCaseId());
            }
        }
    }

    @Data
    static class CourtCaseEntityMatcher implements ArgumentMatcher<CourtCaseEntity> {

        private final String caseId;
        private final List<String> defendantIds;

        @Override
        public boolean matches(CourtCaseEntity arg) {
            final var argDefendantIds = Optional.ofNullable(arg.getDefendants()).orElse(Collections.emptyList())
                .stream()
                .map(DefendantEntity::getDefendantId)
                .collect(Collectors.toList());
            return caseId.equals(arg.getCaseId()) && defendantIds.equals(argDefendantIds);
        }
    }

    @Data
    static class CourtCaseEntityListMatcher implements ArgumentMatcher<List<CourtCaseEntity>> {

        private final String caseId;
        private final List<String> defendantIds;

        @Override
        public boolean matches(List<CourtCaseEntity> arg) {
            if (arg.size() != 1) {
                return false;
            }
            final var argDefendantIds = Optional.ofNullable(arg.get(0).getDefendants()).orElse(Collections.emptyList())
                .stream()
                .map(DefendantEntity::getDefendantId)
                .collect(Collectors.toList());
            return caseId.equals(arg.get(0).getCaseId()) && defendantIds.equals(argDefendantIds);
        }
    }

}
