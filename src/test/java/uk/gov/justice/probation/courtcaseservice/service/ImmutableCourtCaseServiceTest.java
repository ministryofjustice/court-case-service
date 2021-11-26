package uk.gov.justice.probation.courtcaseservice.service;

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
import uk.gov.justice.probation.courtcaseservice.jpa.entity.HearingEntity;
import uk.gov.justice.probation.courtcaseservice.jpa.entity.OffenderEntity;
import uk.gov.justice.probation.courtcaseservice.jpa.entity.OffenderMatchEntity;
import uk.gov.justice.probation.courtcaseservice.jpa.repository.CourtCaseRepository;
import uk.gov.justice.probation.courtcaseservice.jpa.repository.CourtRepository;
import uk.gov.justice.probation.courtcaseservice.jpa.repository.GroupedOffenderMatchRepository;
import uk.gov.justice.probation.courtcaseservice.jpa.repository.OffenderRepository;
import uk.gov.justice.probation.courtcaseservice.service.exceptions.EntityNotFoundException;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.ThrowableAssert.catchThrowable;
import static org.mockito.ArgumentMatchers.argThat;
import static org.mockito.Mockito.eq;
import static org.mockito.Mockito.lenient;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoMoreInteractions;
import static org.mockito.Mockito.when;
import static uk.gov.justice.probation.courtcaseservice.jpa.entity.EntityHelper.CASE_ID;
import static uk.gov.justice.probation.courtcaseservice.jpa.entity.EntityHelper.COURT_CODE;
import static uk.gov.justice.probation.courtcaseservice.jpa.entity.EntityHelper.CRN;
import static uk.gov.justice.probation.courtcaseservice.jpa.entity.EntityHelper.DEFENDANT_ID;
import static uk.gov.justice.probation.courtcaseservice.jpa.entity.EntityHelper.PROBATION_STATUS;
import static uk.gov.justice.probation.courtcaseservice.jpa.entity.EntityHelper.aDefendantEntity;
import static uk.gov.justice.probation.courtcaseservice.jpa.entity.EntityHelper.aHearingEntity;

@ExtendWith(MockitoExtension.class)
class ImmutableCourtCaseServiceTest {

    private static final String CASE_NO = "1600028912";
    private static final LocalDateTime CREATED_BEFORE = LocalDateTime.of(2020, 11, 9, 12, 50);

    @Mock
    private OffenderRepository offenderRepository;
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
    @DisplayName("Tests for createCase by case ID and defendant ID")
    class CreateUpdateByCaseAndDefendantIdTest {

        private CourtCaseEntity incomingCourtCase;
        private DefendantEntity defendant;
        private OffenderEntity offender;

        @BeforeEach
        void setup() {
            service = new ImmutableCourtCaseService(courtRepository, courtCaseRepository, telemetryService, groupedOffenderMatchRepository, offenderRepository);
            lenient().when(courtRepository.findByCourtCode(COURT_CODE)).thenReturn(Optional.of(courtEntity));
            incomingCourtCase = EntityHelper.aCourtCaseEntity(CRN, CASE_NO);
            offender = OffenderEntity.builder().crn("X99999").probationStatus(ProbationStatus.of(PROBATION_STATUS)).build();
            defendant = DefendantEntity.builder().defendantId(DEFENDANT_ID).offender(offender).build();
        }

        @Test
        void givenUnknownCourtCode_whenCreateOrUpdateCase_thenThrowException() {
            when(courtRepository.findByCourtCode("XXX")).thenThrow(new EntityNotFoundException("not found court"));
            incomingCourtCase = CourtCaseEntity.builder()
                    .caseId(CASE_ID)
                    .hearings(Collections.singletonList(aHearingEntity().withCourtCode("XXX")))
                    .build();

            Assertions.assertThrows(EntityNotFoundException.class, () -> {
                service.createUpdateCaseForSingleDefendantId(CASE_ID, DEFENDANT_ID, incomingCourtCase).block();
            });
            verify(courtRepository).findByCourtCode("XXX");
            verifyNoMoreInteractions(courtRepository, courtCaseRepository, telemetryService, offenderRepository);
        }

        @Test
        void givenCaseIdMismatch_whenCreateOrUpdateCase_thenThrowException() {
            incomingCourtCase = EntityHelper.aCourtCaseEntityWithCrn(CRN);
            Assertions.assertThrows(ConflictingInputException.class, () -> {
                service.createUpdateCaseForSingleDefendantId("OTHER-CASE-ID", DEFENDANT_ID, incomingCourtCase).block();
            });
            verify(courtRepository).findByCourtCode(COURT_CODE);
            verifyNoMoreInteractions(courtRepository, courtCaseRepository, telemetryService, offenderRepository);
        }

        @Test
        void givenDefendantIdMismatch_whenCreateOrUpdateCase_thenThrowException() {
            Assertions.assertThrows(ConflictingInputException.class, () -> {
                service.createUpdateCaseForSingleDefendantId(CASE_ID, "OTHER-DEFENDANT-ID", incomingCourtCase).block();
            });
            verify(courtRepository).findByCourtCode(COURT_CODE);
            verifyNoMoreInteractions(courtRepository, courtCaseRepository, telemetryService, offenderRepository);
        }

        @Test
        void givenSingleNewLinkedCase_whenCreateOrUpdateCaseCalledWithCrn_thenLogCreatedEventAndSave() {
            when(courtCaseRepository.findByCaseIdAndDefendantId(CASE_ID, DEFENDANT_ID)).thenReturn(Optional.empty());
            when(courtCaseRepository.save(incomingCourtCase)).thenReturn(incomingCourtCase);
            final var existingOffender = OffenderEntity.builder().crn(CRN).id(199L).probationStatus(ProbationStatus.CURRENT).build();
            when(offenderRepository.findByCrn(CRN)).thenReturn(Optional.of(existingOffender));

            var savedCourtCase = service.createUpdateCaseForSingleDefendantId(CASE_ID, DEFENDANT_ID, incomingCourtCase).block();

            verify(telemetryService).trackCourtCaseEvent(TelemetryEventType.COURT_CASE_CREATED, incomingCourtCase);
            verify(telemetryService).trackCourtCaseDefendantEvent(TelemetryEventType.DEFENDANT_LINKED, incomingCourtCase.getDefendants().get(0), CASE_ID);
            verify(courtCaseRepository).findByCaseIdAndDefendantId(CASE_ID, DEFENDANT_ID);
            verify(courtCaseRepository).save(incomingCourtCase);
            verify(offenderRepository).findByCrn(CRN);
            verify(offenderRepository).save(existingOffender);
            assertThat(savedCourtCase).isSameAs(incomingCourtCase);

            verifyNoMoreInteractions(courtCaseRepository, telemetryService, groupedOffenderMatchRepository, offenderRepository);
        }

        @Test
        void givenSingleNewUnlinkedCase_whenCreateOrUpdateCaseCalledWithCrn_thenLogCreatedEventAndSave() {
            incomingCourtCase = EntityHelper.aCourtCaseEntityWithCrn(null);
            when(courtCaseRepository.findByCaseIdAndDefendantId(CASE_ID, DEFENDANT_ID)).thenReturn(Optional.empty());
            when(courtCaseRepository.save(incomingCourtCase)).thenReturn(incomingCourtCase);

            var savedCourtCase = service.createUpdateCaseForSingleDefendantId(CASE_ID, DEFENDANT_ID, incomingCourtCase).block();

            verify(telemetryService).trackCourtCaseEvent(TelemetryEventType.COURT_CASE_CREATED, incomingCourtCase);
            verify(courtCaseRepository).findByCaseIdAndDefendantId(CASE_ID, DEFENDANT_ID);
            verify(courtCaseRepository).save(incomingCourtCase);
            assertThat(savedCourtCase).isSameAs(incomingCourtCase);

            verifyNoMoreInteractions(courtCaseRepository, telemetryService, groupedOffenderMatchRepository, offenderRepository);
        }

        @Test
        void givenSingleExistingCaseLinkedCase_whenCreateOrUpdateCaseCalledWithoutCrnToUnlink_thenLogUpdatedEventAndSave() {
            var defendant = DefendantEntity.builder().defendantId(DEFENDANT_ID).build();
            var updatedCase = EntityHelper.aCourtCaseEntity(CASE_ID).withDefendants(List.of(defendant));
            var existingCase = EntityHelper.aCourtCaseEntityWithCrn(CRN);
            when(courtCaseRepository.findByCaseIdAndDefendantId(CASE_ID, DEFENDANT_ID)).thenReturn(Optional.of(existingCase));
            when(courtCaseRepository.save(updatedCase)).thenReturn(updatedCase);

            var savedCourtCase = service.createUpdateCaseForSingleDefendantId(CASE_ID, DEFENDANT_ID, updatedCase).block();

            verify(telemetryService).trackCourtCaseEvent(TelemetryEventType.COURT_CASE_UPDATED, updatedCase);
            verify(telemetryService).trackCourtCaseDefendantEvent(TelemetryEventType.DEFENDANT_UNLINKED, existingCase.getDefendants().get(0), CASE_ID);
            verify(courtCaseRepository).findByCaseIdAndDefendantId(CASE_ID, DEFENDANT_ID);
            verify(courtCaseRepository).save(updatedCase);
            assertThat(savedCourtCase).isSameAs(updatedCase);
            verifyNoMoreInteractions(courtCaseRepository, telemetryService, offenderRepository);
        }

        @Test
        void givenSingleExistingUnlinkedCase_whenCreateOrUpdateCaseCalledWithCrn_thenLogUpdatedEventAndSave() {
            var unlinkedDefendant = defendant.withOffender(null);
            var updatedCase = EntityHelper.aCourtCaseEntity(CASE_ID).withDefendants(List.of(defendant));
            var existingCase = EntityHelper.aCourtCaseEntity(CASE_ID).withDefendants(List.of(unlinkedDefendant));
            var offender = defendant.getOffender();
            when(courtCaseRepository.findByCaseIdAndDefendantId(CASE_ID, DEFENDANT_ID)).thenReturn(Optional.of(existingCase));
            when(courtCaseRepository.save(updatedCase)).thenReturn(updatedCase);
            when(offenderRepository.findByCrn("X99999")).thenReturn(Optional.empty());

            var savedCourtCase = service.createUpdateCaseForSingleDefendantId(CASE_ID, DEFENDANT_ID, updatedCase).block();

            verify(telemetryService).trackCourtCaseEvent(TelemetryEventType.COURT_CASE_UPDATED, updatedCase);
            verify(telemetryService).trackCourtCaseDefendantEvent(TelemetryEventType.DEFENDANT_LINKED, defendant, CASE_ID);
            verify(courtCaseRepository).findByCaseIdAndDefendantId(CASE_ID, DEFENDANT_ID);
            verify(courtCaseRepository).save(updatedCase);
            verify(offenderRepository).findByCrn("X99999");
            verify(offenderRepository).save(offender);
            assertThat(savedCourtCase).isSameAs(updatedCase);
            verifyNoMoreInteractions(courtCaseRepository, telemetryService, offenderRepository);
        }

        @Test
        void givenExistingCaseWithMultipleDefendants_whenCreateOrUpdateCaseCalledWithCrn_thenLogCreatedAndLinkedEvent() {
            var otherExistingDefendant = DefendantEntity.builder().defendantId("DEF_ID_2").build().withOffender(offender);
            var updatedCase = EntityHelper.aCourtCaseEntity(CASE_ID).withDefendants(List.of(defendant));
            var existingCase = EntityHelper.aCourtCaseEntity(CASE_ID).withDefendants(List.of(defendant, otherExistingDefendant));
            when(offenderRepository.findByCrn("X99999")).thenReturn(Optional.empty());
            when(courtCaseRepository.findByCaseIdAndDefendantId(CASE_ID, DEFENDANT_ID)).thenReturn(Optional.of(existingCase));
            var expectedSave = new CourtCaseEntityMatcher(CASE_ID, List.of(DEFENDANT_ID, "DEF_ID_2"));
            when(courtCaseRepository.save(argThat(expectedSave))).thenReturn(updatedCase);

            var savedCourtCase = service.createUpdateCaseForSingleDefendantId(CASE_ID, DEFENDANT_ID, updatedCase).block();

            verify(telemetryService).trackCourtCaseEvent(eq(TelemetryEventType.COURT_CASE_UPDATED), eq(updatedCase));
            verify(courtCaseRepository).findByCaseIdAndDefendantId(CASE_ID, DEFENDANT_ID);
            verify(courtCaseRepository).save(argThat(expectedSave));
            verify(offenderRepository).findByCrn("X99999");
            verify(offenderRepository).save(offender);
            assertThat(savedCourtCase).isNotNull();
            verifyNoMoreInteractions(courtCaseRepository, telemetryService, offenderRepository);
        }

        @Test
        void whenAddDefendants_thenReturn() {
            var otherExistingDefendant = DefendantEntity.builder().defendantId("DEF_ID_2").build().withOffender(offender);
            var updatedCase = EntityHelper.aCourtCaseEntity(CASE_ID).withDefendants(List.of(defendant));
            var existingCase = EntityHelper.aCourtCaseEntity(CASE_ID).withDefendants(List.of(defendant, otherExistingDefendant));
            when(offenderRepository.findByCrn("X99999")).thenReturn(Optional.empty());
            when(courtCaseRepository.findByCaseIdAndDefendantId(CASE_ID, DEFENDANT_ID)).thenReturn(Optional.of(existingCase));
            var expectedSave = new CourtCaseEntityMatcher(CASE_ID, List.of(DEFENDANT_ID, "DEF_ID_2"));
            when(courtCaseRepository.save(argThat(expectedSave))).thenReturn(updatedCase);

            var savedCourtCase = service.createUpdateCaseForSingleDefendantId(CASE_ID, DEFENDANT_ID, updatedCase).block();

            verify(telemetryService).trackCourtCaseEvent(eq(TelemetryEventType.COURT_CASE_UPDATED), eq(updatedCase));
            verify(courtCaseRepository).findByCaseIdAndDefendantId(CASE_ID, DEFENDANT_ID);
            verify(courtCaseRepository).save(argThat(expectedSave));
            verify(offenderRepository).findByCrn("X99999");
            verify(offenderRepository).save(offender);
            assertThat(savedCourtCase).isNotNull();
            verifyNoMoreInteractions(courtCaseRepository, telemetryService, offenderRepository);
        }

    }

    @ExtendWith(MockitoExtension.class)
    @Nested
    @DisplayName("Tests for createCase by case ID with ExtendedCourtCaseRequest")
    class CreateUpdateByCaseIdTest {

        private CourtCaseEntity courtCase;

        @BeforeEach
        void setup() {
            service = new ImmutableCourtCaseService(courtRepository, courtCaseRepository, telemetryService, groupedOffenderMatchRepository, offenderRepository);
            lenient().when(courtRepository.findByCourtCode(COURT_CODE)).thenReturn(Optional.of(courtEntity));
            courtCase = EntityHelper.aCourtCaseEntity(CRN, CASE_NO);
        }

        @Test
        void givenNoExistingCase_whenCreateOrUpdateCaseCalledWithLinkedDefendant_thenLogCreatedAndLinkedEvent() {
            when(courtCaseRepository.findFirstByCaseIdOrderByIdDesc(CASE_ID)).thenReturn(Optional.empty());
            when(courtCaseRepository.save(courtCase)).thenReturn(courtCase);
            when(offenderRepository.findByCrn(CRN)).thenReturn(Optional.empty());

            var savedCourtCase = service.createCase(CASE_ID, courtCase).block();

            verify(telemetryService).trackCourtCaseEvent(TelemetryEventType.COURT_CASE_CREATED, courtCase);
            verify(telemetryService).trackCourtCaseDefendantEvent(TelemetryEventType.DEFENDANT_LINKED, courtCase.getDefendants().get(0), courtCase.getCaseId());
            verify(courtCaseRepository).save(courtCase);
            assertThat(savedCourtCase).isNotNull();
            verify(offenderRepository).findByCrn(CRN);
            verify(offenderRepository).save(courtCase.getDefendants().get(0).getOffender());
            verifyNoMoreInteractions(courtCaseRepository, telemetryService, offenderRepository);
        }

        @Test
        void givenNoExistingCase_whenCreateCaseCalledWithoutCrn_thenLogOnlyCreatedEvent() {

            courtCase = EntityHelper.aCourtCaseEntity(null, CASE_NO);
            when(courtCaseRepository.findFirstByCaseIdOrderByIdDesc(CASE_ID)).thenReturn(Optional.empty());
            when(courtCaseRepository.save(courtCase)).thenReturn(courtCase);

            var savedCourtCase = service.createCase(CASE_ID, courtCase).block();

            verify(telemetryService).trackCourtCaseEvent(TelemetryEventType.COURT_CASE_CREATED, courtCase);
            verify(courtCaseRepository).save(courtCase);
            verifyNoMoreInteractions(courtCaseRepository, telemetryService, offenderRepository);
            assertThat(savedCourtCase).isNotNull();
        }

        @Test
        void givenExistingCase_whenCreateOrUpdateCaseCalled_thenLogUpdatedEvent() {
            when(courtCaseRepository.findFirstByCaseIdOrderByIdDesc(CASE_ID)).thenReturn(Optional.of(courtCase));
            when(courtCaseRepository.save(courtCase)).thenReturn(courtCase);
            var existingOffender = OffenderEntity.builder().crn(CRN).probationStatus(ProbationStatus.CURRENT).id(201L).build();
            when(offenderRepository.findByCrn(CRN)).thenReturn(Optional.of(existingOffender));
            var updateOffender = courtCase.getDefendants().get(0).getOffender();

            var savedCourtCase = service.createCase(CASE_ID, courtCase).block();

            verify(telemetryService).trackCourtCaseEvent(TelemetryEventType.COURT_CASE_UPDATED, courtCase);
            verify(courtCaseRepository).save(courtCase);
            verify(offenderRepository).findByCrn(CRN);
            verify(offenderRepository).save(existingOffender);
            assertThat(savedCourtCase).isNotNull();
            // The existing one has been updated based on values from the one passed in.
            assertThat(existingOffender.getProbationStatus()).isSameAs(ProbationStatus.PREVIOUSLY_KNOWN);
            assertThat(updateOffender.getId()).isEqualTo(201);
            verifyNoMoreInteractions(telemetryService, courtCaseRepository, offenderRepository);
        }

        @Test
        void givenExistingCaseWithNullCrn_whenCreateOrUpdateCaseCalledWithCrn_thenLogLinkedEvent() {
            var existingCase = EntityHelper.aCourtCaseEntity(null, CASE_NO);
            when(courtCaseRepository.findFirstByCaseIdOrderByIdDesc(CASE_ID)).thenReturn(Optional.of(existingCase));
            when(courtCaseRepository.save(existingCase)).thenReturn(existingCase);
            var existingOffender = OffenderEntity.builder().crn(CRN).id(201L).build();
            when(offenderRepository.findByCrn(CRN)).thenReturn(Optional.of(existingOffender));

            var savedCourtCase = service.createCase(CASE_ID, EntityHelper.aCourtCaseEntity(CRN, CASE_NO)).block();

            verify(telemetryService).trackCourtCaseEvent(TelemetryEventType.COURT_CASE_UPDATED, existingCase);
            verify(telemetryService).trackCourtCaseDefendantEvent(TelemetryEventType.DEFENDANT_LINKED, courtCase.getDefendants().get(0), CASE_ID);
            verify(courtCaseRepository).save(existingCase);
            verify(offenderRepository).findByCrn(CRN);
            verify(offenderRepository).save(existingOffender);
            assertThat(savedCourtCase).isNotNull();
            verifyNoMoreInteractions(telemetryService, courtCaseRepository, offenderRepository);
        }

        @Test
        void givenExistingCaseWithCrn_whenCreateOrUpdateCaseCalledWithNullCrn_thenLogUnLinkedEvent() {
            when(courtCaseRepository.findFirstByCaseIdOrderByIdDesc(CASE_ID)).thenReturn(Optional.of(courtCase));
            when(courtCaseRepository.save(courtCase)).thenReturn(courtCase);

            var updatedCourtCase = EntityHelper.aCourtCaseEntity(null, CASE_NO);

            var savedCourtCase = service.createCase(CASE_ID, updatedCourtCase).block();

            verify(telemetryService).trackCourtCaseEvent(TelemetryEventType.COURT_CASE_UPDATED, updatedCourtCase);
            verify(telemetryService).trackCourtCaseDefendantEvent(TelemetryEventType.DEFENDANT_UNLINKED, courtCase.getDefendants().get(0), CASE_ID);
            verify(courtCaseRepository).save(updatedCourtCase);
            assertThat(savedCourtCase).isNotNull();
            verifyNoMoreInteractions(telemetryService, courtCaseRepository, offenderRepository);
        }

        @Test
        void givenNewCaseWithTwoDefendants_whenCreateCase_thenLogCreatedAndOneDefendantLinkedEvent() {
            var linkedDefendant = aDefendantEntity("abc", CRN);
            var unlinkedDefendant = aDefendantEntity("def", null);
            var newCourtCase = courtCase.withDefendants(List.of(linkedDefendant, unlinkedDefendant));
            when(courtCaseRepository.findFirstByCaseIdOrderByIdDesc(CASE_ID)).thenReturn(Optional.empty());
            when(courtCaseRepository.save(newCourtCase)).thenReturn(newCourtCase);
            when(offenderRepository.findByCrn(CRN)).thenReturn(Optional.empty());

            var savedCourtCase = service.createCase(CASE_ID, newCourtCase).block();

            verify(telemetryService).trackCourtCaseEvent(TelemetryEventType.COURT_CASE_CREATED, newCourtCase);
            verify(telemetryService).trackCourtCaseDefendantEvent(TelemetryEventType.DEFENDANT_LINKED, linkedDefendant, CASE_ID);
            verify(courtCaseRepository).save(newCourtCase);
            verify(offenderRepository).findByCrn(CRN);
            verify(offenderRepository).save(linkedDefendant.getOffender());
            assertThat(savedCourtCase).isNotNull();
            verifyNoMoreInteractions(telemetryService, courtCaseRepository, offenderRepository);
        }

        @Test
        void givenUnknownCourtCode_whenCreateOrUpdateCase_thenThrowException() {
            when(courtRepository.findByCourtCode(COURT_CODE)).thenReturn(Optional.of(courtEntity));
            when(courtRepository.findByCourtCode("XXX")).thenReturn(Optional.empty());
            courtCase = CourtCaseEntity.builder()
                    .hearings(List.of(
                            HearingEntity.builder()
                                    .courtCode(COURT_CODE)
                                    .build(),
                            HearingEntity.builder()
                                    .courtCode("XXX")
                                    .build()

                    ))
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
                    .hearings(List.of(
                            HearingEntity.builder()
                                    .courtCode(COURT_CODE)
                                    .build()
                    ))
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
            service = new ImmutableCourtCaseService(courtRepository, courtCaseRepository, telemetryService, groupedOffenderMatchRepository, offenderRepository);
        }

        @Test
        void givenCreatedBeforeIsNull_filterByDateShouldRetrieveCourtCasesFromRepository() {
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
        void givenUseExtendedCases_filterByHearingDayShouldRetrieveCourtCasesFromRepository() {
            service = new ImmutableCourtCaseService(courtRepository, courtCaseRepository, telemetryService, groupedOffenderMatchRepository, offenderRepository);
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
            when(courtCaseRepository.findByCourtCodeAndHearingDay(COURT_CODE, SEARCH_DATE, CREATED_AFTER, CREATED_BEFORE))
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
            when(courtCaseRepository.findLastModifiedByHearingDay(COURT_CODE, SEARCH_DATE))
                    .thenReturn(Optional.of(expectedLastModified));

            var lastModified = service.filterCasesLastModified(COURT_CODE, SEARCH_DATE);

            assertThat(lastModified).isPresent();
            assertThat(lastModified.get()).isEqualTo(expectedLastModified);
        }

        @Test
        void whenFilterByCourtAndDateForLastModified_andNoneFound_thenReturnEmpty() {
            when(courtCaseRepository.findLastModifiedByHearingDay(COURT_CODE, SEARCH_DATE))
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
            service = new ImmutableCourtCaseService(courtRepository, courtCaseRepository, telemetryService, groupedOffenderMatchRepository, offenderRepository);
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
            service = new ImmutableCourtCaseService(courtRepository, courtCaseRepository, telemetryService, groupedOffenderMatchRepository, offenderRepository);
        }

        @Test
        void givenOffenderMatchesExistForCaseWithMultipleDefendants_whenCrnUpdated_thenUpdateMatches() {
            final var matchCrn = "X11111";
            final var rejectedCrn = "X99999";
            when(courtRepository.findByCourtCode(COURT_CODE)).thenReturn(Optional.of(courtEntity));
            var existingCase = EntityHelper.aCourtCaseEntity(null, CASE_NO, List.of(
                    aDefendantEntity("defendant1", null)
            ));
            when(courtCaseRepository.findFirstByCaseIdOrderByIdDesc(CASE_ID)).thenReturn(Optional.of(existingCase));
            when(groupedOffenderMatchRepository.findByCaseIdAndDefendantId(CASE_ID, "defendant1"))
                .thenReturn(buildOffenderMatches(matchCrn, rejectedCrn));
            when(groupedOffenderMatchRepository.findByCaseIdAndDefendantId(CASE_ID, "defendant2")).thenReturn(Optional.empty());
            var caseToUpdate = EntityHelper.aCourtCaseEntity(null, CASE_NO, List.of(
                    aDefendantEntity("defendant1", matchCrn),
                    aDefendantEntity("defendant2", "X99999")
            ));

            when(courtCaseRepository.save(caseToUpdate)).thenReturn(caseToUpdate);

            service.createCase(CASE_ID, caseToUpdate).block();

            verify(courtCaseRepository).save(caseToUpdate);
            verify(groupedOffenderMatchRepository).findByCaseIdAndDefendantId(CASE_ID, "defendant1");
            verify(groupedOffenderMatchRepository).findByCaseIdAndDefendantId(CASE_ID, "defendant2");

            verify(groupedOffenderMatchRepository).save(matchesCaptor.capture());
            final var groupedOffenderMatches = matchesCaptor.getValue();
            var correctMatch = groupedOffenderMatches.getOffenderMatches().get(0);
            assertThat(correctMatch.getCrn()).isEqualTo(matchCrn);
            assertThat(correctMatch.getConfirmed()).isTrue();
            assertThat(correctMatch.getRejected()).isFalse();

            var rejectedMatch1 = groupedOffenderMatches.getOffenderMatches().get(1);
            assertThat(rejectedMatch1.getCrn()).isEqualTo(rejectedCrn);
            assertThat(rejectedMatch1.getConfirmed()).isFalse();
            assertThat(rejectedMatch1.getRejected()).isTrue();

            verifyNoMoreInteractions(groupedOffenderMatchRepository, courtCaseRepository);
        }

        @NonNull
        private Optional<GroupedOffenderMatchesEntity> buildOffenderMatches(String matchCrn, String rejectedCrn) {
            return Optional.ofNullable(GroupedOffenderMatchesEntity.builder()
                    .offenderMatches(Arrays.asList(OffenderMatchEntity.builder()
                                    .crn(matchCrn)
                                    .confirmed(false)
                                    .rejected(false)
                                    .build(),
                            OffenderMatchEntity.builder()
                                    .crn(rejectedCrn)
                                    .confirmed(false)
                                    .rejected(false)
                                    .build()))
                    .build());
        }
    }

    record CourtCaseEntityMatcher(String caseId, List<String> defendantIds) implements ArgumentMatcher<CourtCaseEntity> {

        @Override
        public boolean matches(CourtCaseEntity arg) {
            final var argDefendantIds = Optional.ofNullable(arg.getDefendants()).orElse(Collections.emptyList())
                .stream()
                .map(DefendantEntity::getDefendantId)
                .collect(Collectors.toList());
            return caseId.equals(arg.getCaseId()) && defendantIds.equals(argDefendantIds);
        }
    }

    record CourtCaseEntityListMatcher(String caseId, List<String> defendantIds) implements ArgumentMatcher<List<CourtCaseEntity>> {

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
