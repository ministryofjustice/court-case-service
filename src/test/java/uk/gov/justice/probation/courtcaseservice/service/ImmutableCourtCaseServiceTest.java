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
import uk.gov.justice.probation.courtcaseservice.jpa.entity.CourtCaseEntity;
import uk.gov.justice.probation.courtcaseservice.jpa.entity.CourtEntity;
import uk.gov.justice.probation.courtcaseservice.jpa.entity.DefendantEntity;
import uk.gov.justice.probation.courtcaseservice.jpa.entity.EntityHelper;
import uk.gov.justice.probation.courtcaseservice.jpa.entity.GroupedOffenderMatchesEntity;
import uk.gov.justice.probation.courtcaseservice.jpa.entity.HearingDayEntity;
import uk.gov.justice.probation.courtcaseservice.jpa.entity.HearingEntity;
import uk.gov.justice.probation.courtcaseservice.jpa.entity.OffenderEntity;
import uk.gov.justice.probation.courtcaseservice.jpa.entity.OffenderMatchEntity;
import uk.gov.justice.probation.courtcaseservice.jpa.entity.OffenderProbationStatus;
import uk.gov.justice.probation.courtcaseservice.jpa.repository.CourtCaseRepository;
import uk.gov.justice.probation.courtcaseservice.jpa.repository.CourtRepository;
import uk.gov.justice.probation.courtcaseservice.jpa.repository.GroupedOffenderMatchRepository;
import uk.gov.justice.probation.courtcaseservice.jpa.repository.HearingRepository;
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
import static org.mockito.ArgumentMatchers.any;
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
    private HearingRepository hearingRepository;
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

        private HearingEntity incomingHearing;
        private DefendantEntity defendant;
        private OffenderEntity offender;

        @BeforeEach
        void setup() {
            service = new ImmutableCourtCaseService(courtRepository, courtCaseRepository, hearingRepository, telemetryService, groupedOffenderMatchRepository, offenderRepository);
            lenient().when(courtRepository.findByCourtCode(COURT_CODE)).thenReturn(Optional.of(courtEntity));
            incomingHearing = EntityHelper.aHearingEntity(CRN, CASE_NO);
            offender = OffenderEntity.builder().crn("X99999").probationStatus(OffenderProbationStatus.of(PROBATION_STATUS)).build();
            defendant = DefendantEntity.builder().defendantId(DEFENDANT_ID).offender(offender).build();
        }

        @Test
        void givenUnknownCourtCode_whenCreateOrUpdateCase_thenCreateIt() {
            when(courtRepository.findByCourtCode("XXX")).thenReturn(Optional.empty());
            when(hearingRepository.findByCaseIdAndDefendantId(CASE_ID, DEFENDANT_ID)).thenReturn(Optional.empty());
            when(hearingRepository.save(incomingHearing)).thenReturn(incomingHearing);
            final var existingOffender = OffenderEntity.builder().crn(CRN).id(199L).probationStatus(OffenderProbationStatus.CURRENT).build();
            when(offenderRepository.findByCrn(any())).thenReturn(Optional.of(existingOffender));

            incomingHearing = HearingEntity.builder()
                    .courtCase(CourtCaseEntity.builder()
                        .caseId(CASE_ID)
                    .build())
                    .hearingDays(Collections.singletonList(aHearingEntity().withCourtCode("XXX")))
                    .defendants(Collections.singletonList(defendant))
                    .build();

            service.createUpdateHearingForSingleDefendantId(CASE_ID, DEFENDANT_ID, incomingHearing).block();
            verify(courtRepository).findByCourtCode("XXX");
            verify(courtRepository).save(CourtEntity.builder()
                    .courtCode("XXX")
                    .build());

            verify(telemetryService).trackCourtCaseEvent(TelemetryEventType.COURT_CASE_CREATED, incomingHearing);
            verify(telemetryService).trackCourtCaseDefendantEvent(TelemetryEventType.DEFENDANT_LINKED, incomingHearing.getDefendants().get(0), CASE_ID);
            verify(hearingRepository).findByCaseIdAndDefendantId(CASE_ID, DEFENDANT_ID);
            verify(hearingRepository).save(incomingHearing);
            verify(offenderRepository).findByCrn(any());
            verify(offenderRepository).save(existingOffender);
            verifyNoMoreInteractions(courtRepository, hearingRepository, telemetryService, offenderRepository);
        }

        @Test
        void givenCaseIdMismatch_whenCreateOrUpdateCase_thenThrowException() {
            incomingHearing = EntityHelper.aHearingEntityWithCrn(CRN);
            Assertions.assertThrows(ConflictingInputException.class, () -> {
                service.createUpdateHearingForSingleDefendantId("OTHER-CASE-ID", DEFENDANT_ID, incomingHearing).block();
            });
            verify(courtRepository).findByCourtCode(COURT_CODE);
            verifyNoMoreInteractions(courtRepository, hearingRepository, telemetryService, offenderRepository);
        }

        @Test
        void givenDefendantIdMismatch_whenCreateOrUpdateCase_thenThrowException() {
            Assertions.assertThrows(ConflictingInputException.class, () -> {
                service.createUpdateHearingForSingleDefendantId(CASE_ID, "OTHER-DEFENDANT-ID", incomingHearing).block();
            });
            verify(courtRepository).findByCourtCode(COURT_CODE);
            verifyNoMoreInteractions(courtRepository, hearingRepository, telemetryService, offenderRepository);
        }

        @Test
        void givenSingleNewLinkedCase_whenCreateOrUpdateCaseCalledWithCrn_thenLogCreatedEventAndSave() {
            when(hearingRepository.findByCaseIdAndDefendantId(CASE_ID, DEFENDANT_ID)).thenReturn(Optional.empty());
            when(hearingRepository.save(incomingHearing)).thenReturn(incomingHearing);
            final var existingOffender = OffenderEntity.builder().crn(CRN).id(199L).probationStatus(OffenderProbationStatus.CURRENT).build();
            when(offenderRepository.findByCrn(CRN)).thenReturn(Optional.of(existingOffender));

            var savedCourtCase = service.createUpdateHearingForSingleDefendantId(CASE_ID, DEFENDANT_ID, incomingHearing).block();

            verify(telemetryService).trackCourtCaseEvent(TelemetryEventType.COURT_CASE_CREATED, incomingHearing);
            verify(telemetryService).trackCourtCaseDefendantEvent(TelemetryEventType.DEFENDANT_LINKED, incomingHearing.getDefendants().get(0), CASE_ID);
            verify(hearingRepository).findByCaseIdAndDefendantId(CASE_ID, DEFENDANT_ID);
            verify(hearingRepository).save(incomingHearing);
            assertThat(incomingHearing.getHearingId()).isEqualTo(CASE_ID);
            verify(offenderRepository).findByCrn(CRN);
            verify(offenderRepository).save(existingOffender);
            assertThat(savedCourtCase).isSameAs(incomingHearing);

            verifyNoMoreInteractions(hearingRepository, telemetryService, groupedOffenderMatchRepository, offenderRepository);
        }

        @Test
        void givenSingleNewUnlinkedCase_whenCreateOrUpdateCaseCalledWithCrn_thenLogCreatedEventAndSave() {
            incomingHearing = EntityHelper.aHearingEntityWithCrn(null);
            when(hearingRepository.findByCaseIdAndDefendantId(CASE_ID, DEFENDANT_ID)).thenReturn(Optional.empty());
            when(hearingRepository.save(incomingHearing)).thenReturn(incomingHearing);

            var savedCourtCase = service.createUpdateHearingForSingleDefendantId(CASE_ID, DEFENDANT_ID, incomingHearing).block();

            verify(telemetryService).trackCourtCaseEvent(TelemetryEventType.COURT_CASE_CREATED, incomingHearing);
            verify(hearingRepository).findByCaseIdAndDefendantId(CASE_ID, DEFENDANT_ID);
            verify(hearingRepository).save(incomingHearing);
            assertThat(savedCourtCase).isSameAs(incomingHearing);

            verifyNoMoreInteractions(hearingRepository, telemetryService, groupedOffenderMatchRepository, offenderRepository);
        }

        @Test
        void givenSingleExistingCaseLinkedCase_whenCreateOrUpdateCaseCalledWithoutCrnToUnlink_thenLogUpdatedEventAndSave() {
            var defendant = DefendantEntity.builder().defendantId(DEFENDANT_ID).build();
            var updatedCase = EntityHelper.aHearingEntity(CASE_ID).withDefendants(List.of(defendant));
            var existingCase = EntityHelper.aHearingEntityWithCrn(CRN);
            when(hearingRepository.findByCaseIdAndDefendantId(CASE_ID, DEFENDANT_ID)).thenReturn(Optional.of(existingCase));
            when(hearingRepository.save(updatedCase)).thenReturn(updatedCase);

            var savedCourtCase = service.createUpdateHearingForSingleDefendantId(CASE_ID, DEFENDANT_ID, updatedCase).block();

            verify(telemetryService).trackCourtCaseEvent(TelemetryEventType.COURT_CASE_UPDATED, updatedCase);
            verify(telemetryService).trackCourtCaseDefendantEvent(TelemetryEventType.DEFENDANT_UNLINKED, existingCase.getDefendants().get(0), CASE_ID);
            verify(hearingRepository).findByCaseIdAndDefendantId(CASE_ID, DEFENDANT_ID);
            verify(hearingRepository).save(updatedCase);
            assertThat(savedCourtCase).isSameAs(updatedCase);
            verifyNoMoreInteractions(hearingRepository, telemetryService, offenderRepository);
        }

        @Test
        void givenSingleExistingUnlinkedCase_whenCreateOrUpdateCaseCalledWithCrn_thenLogUpdatedEventAndSave() {
            var unlinkedDefendant = defendant.withOffender(null);
            var updatedCase = EntityHelper.aHearingEntity(CASE_ID).withDefendants(List.of(defendant));
            var existingCase = EntityHelper.aHearingEntity(CASE_ID).withDefendants(List.of(unlinkedDefendant));
            var offender = defendant.getOffender();
            when(hearingRepository.findByCaseIdAndDefendantId(CASE_ID, DEFENDANT_ID)).thenReturn(Optional.of(existingCase));
            when(hearingRepository.save(updatedCase)).thenReturn(updatedCase);
            when(offenderRepository.findByCrn("X99999")).thenReturn(Optional.empty());

            var savedCourtCase = service.createUpdateHearingForSingleDefendantId(CASE_ID, DEFENDANT_ID, updatedCase).block();

            verify(telemetryService).trackCourtCaseEvent(TelemetryEventType.COURT_CASE_UPDATED, updatedCase);
            verify(telemetryService).trackCourtCaseDefendantEvent(TelemetryEventType.DEFENDANT_LINKED, defendant, CASE_ID);
            verify(hearingRepository).findByCaseIdAndDefendantId(CASE_ID, DEFENDANT_ID);
            verify(hearingRepository).save(updatedCase);
            verify(offenderRepository).findByCrn("X99999");
            verify(offenderRepository).save(offender);
            assertThat(savedCourtCase).isSameAs(updatedCase);
            verifyNoMoreInteractions(hearingRepository, telemetryService, offenderRepository);
        }

        @Test
        void givenExistingCaseWithMultipleDefendants_whenCreateOrUpdateCaseCalledWithCrn_thenLogCreatedAndLinkedEvent() {
            var otherExistingDefendant = DefendantEntity.builder().defendantId("DEF_ID_2").build().withOffender(offender);
            var updatedCase = EntityHelper.aHearingEntity(CASE_ID).withDefendants(List.of(defendant));
            var existingCase = EntityHelper.aHearingEntity(CASE_ID).withDefendants(List.of(defendant, otherExistingDefendant));
            when(offenderRepository.findByCrn("X99999")).thenReturn(Optional.empty());
            when(hearingRepository.findByCaseIdAndDefendantId(CASE_ID, DEFENDANT_ID)).thenReturn(Optional.of(existingCase));
            var expectedSave = new CourtCaseEntityMatcher(CASE_ID, List.of(DEFENDANT_ID, "DEF_ID_2"));
            when(hearingRepository.save(argThat(expectedSave))).thenReturn(updatedCase);

            var savedCourtCase = service.createUpdateHearingForSingleDefendantId(CASE_ID, DEFENDANT_ID, updatedCase).block();

            verify(telemetryService).trackCourtCaseEvent(eq(TelemetryEventType.COURT_CASE_UPDATED), eq(updatedCase));
            verify(hearingRepository).findByCaseIdAndDefendantId(CASE_ID, DEFENDANT_ID);
            verify(hearingRepository).save(argThat(expectedSave));
            verify(offenderRepository).findByCrn("X99999");
            verify(offenderRepository).save(offender);
            assertThat(savedCourtCase).isNotNull();
            verifyNoMoreInteractions(hearingRepository, telemetryService, offenderRepository);
        }

        @Test
        void whenAddDefendants_thenReturn() {
            var otherExistingDefendant = DefendantEntity.builder().defendantId("DEF_ID_2").build().withOffender(offender);
            var updatedCase = EntityHelper.aHearingEntity(CASE_ID).withDefendants(List.of(defendant));
            var existingCase = EntityHelper.aHearingEntity(CASE_ID).withDefendants(List.of(defendant, otherExistingDefendant));
            when(offenderRepository.findByCrn("X99999")).thenReturn(Optional.empty());
            when(hearingRepository.findByCaseIdAndDefendantId(CASE_ID, DEFENDANT_ID)).thenReturn(Optional.of(existingCase));
            var expectedSave = new CourtCaseEntityMatcher(CASE_ID, List.of(DEFENDANT_ID, "DEF_ID_2"));
            when(hearingRepository.save(argThat(expectedSave))).thenReturn(updatedCase);

            var savedCourtCase = service.createUpdateHearingForSingleDefendantId(CASE_ID, DEFENDANT_ID, updatedCase).block();

            verify(telemetryService).trackCourtCaseEvent(eq(TelemetryEventType.COURT_CASE_UPDATED), eq(updatedCase));
            verify(hearingRepository).findByCaseIdAndDefendantId(CASE_ID, DEFENDANT_ID);
            verify(hearingRepository).save(argThat(expectedSave));
            verify(offenderRepository).findByCrn("X99999");
            verify(offenderRepository).save(offender);
            assertThat(savedCourtCase).isNotNull();
            verifyNoMoreInteractions(hearingRepository, telemetryService, offenderRepository);
        }

    }

    @ExtendWith(MockitoExtension.class)
    @Nested
    @DisplayName("Tests for createCase by case ID with ExtendedCourtCaseRequest")
    class CreateUpdateByCaseIdTest {

        private HearingEntity hearing;

        @BeforeEach
        void setup() {
            service = new ImmutableCourtCaseService(courtRepository, courtCaseRepository, hearingRepository, telemetryService, groupedOffenderMatchRepository, offenderRepository);
            lenient().when(courtRepository.findByCourtCode(COURT_CODE)).thenReturn(Optional.of(courtEntity));
            hearing = EntityHelper.aHearingEntity(CRN, CASE_NO);
        }

        @Test
        void givenNoExistingCase_whenCreateOrUpdateCaseCalledWithLinkedDefendant_thenLogCreatedAndLinkedEvent() {
            when(hearingRepository.findFirstByCaseIdOrderByIdDesc(CASE_ID)).thenReturn(Optional.empty());
            when(hearingRepository.save(hearing)).thenReturn(hearing);
            when(offenderRepository.findByCrn(CRN)).thenReturn(Optional.empty());

            var savedCourtCase = service.createHearing(CASE_ID, hearing).block();

            verify(telemetryService).trackCourtCaseEvent(TelemetryEventType.COURT_CASE_CREATED, hearing);
            verify(telemetryService).trackCourtCaseDefendantEvent(TelemetryEventType.DEFENDANT_LINKED, hearing.getDefendants().get(0), hearing.getCaseId());
            verify(hearingRepository).save(hearing);
            assertThat(savedCourtCase).isNotNull();
            assertThat(savedCourtCase.getHearingId()).isEqualTo(CASE_ID);
            verify(offenderRepository).findByCrn(CRN);
            verify(offenderRepository).save(hearing.getDefendants().get(0).getOffender());
            verifyNoMoreInteractions(hearingRepository, telemetryService, offenderRepository);
        }

        @Test
        void givenNoExistingCase_whenCreateCaseCalledWithoutCrn_thenLogOnlyCreatedEvent() {

            hearing = EntityHelper.aHearingEntity(null, CASE_NO);
            when(hearingRepository.findFirstByCaseIdOrderByIdDesc(CASE_ID)).thenReturn(Optional.empty());
            when(hearingRepository.save(hearing)).thenReturn(hearing);

            var savedCourtCase = service.createHearing(CASE_ID, hearing).block();

            verify(telemetryService).trackCourtCaseEvent(TelemetryEventType.COURT_CASE_CREATED, hearing);
            verify(hearingRepository).save(hearing);
            verifyNoMoreInteractions(hearingRepository, telemetryService, offenderRepository);
            assertThat(savedCourtCase).isNotNull();
        }

        @Test
        void givenExistingCase_whenCreateOrUpdateCaseCalled_thenLogUpdatedEvent() {
            when(hearingRepository.findFirstByCaseIdOrderByIdDesc(CASE_ID)).thenReturn(Optional.of(hearing));
            when(hearingRepository.save(hearing)).thenReturn(hearing);
            var existingOffender = OffenderEntity.builder().crn(CRN).probationStatus(OffenderProbationStatus.CURRENT).id(201L).build();
            when(offenderRepository.findByCrn(CRN)).thenReturn(Optional.of(existingOffender));
            var updateOffender = hearing.getDefendants().get(0).getOffender();

            var savedCourtCase = service.createHearing(CASE_ID, hearing).block();

            verify(telemetryService).trackCourtCaseEvent(TelemetryEventType.COURT_CASE_UPDATED, hearing);
            verify(hearingRepository).save(hearing);
            verify(offenderRepository).findByCrn(CRN);
            verify(offenderRepository).save(existingOffender);
            assertThat(savedCourtCase).isNotNull();
            // The existing one has been updated based on values from the one passed in.
            assertThat(existingOffender.getProbationStatus()).isSameAs(OffenderProbationStatus.PREVIOUSLY_KNOWN);
            assertThat(updateOffender.getId()).isEqualTo(201);
            verifyNoMoreInteractions(telemetryService, hearingRepository, offenderRepository);
        }

        @Test
        void givenExistingCaseWithNullCrn_whenCreateOrUpdateCaseCalledWithCrn_thenLogLinkedEvent() {
            var existingCase = EntityHelper.aHearingEntity(null, CASE_NO);
            when(hearingRepository.findFirstByCaseIdOrderByIdDesc(CASE_ID)).thenReturn(Optional.of(existingCase));
            when(hearingRepository.save(existingCase)).thenReturn(existingCase);
            var existingOffender = OffenderEntity.builder().crn(CRN).id(201L).build();
            when(offenderRepository.findByCrn(CRN)).thenReturn(Optional.of(existingOffender));

            var savedCourtCase = service.createHearing(CASE_ID, EntityHelper.aHearingEntity(CRN, CASE_NO)).block();

            verify(telemetryService).trackCourtCaseEvent(TelemetryEventType.COURT_CASE_UPDATED, existingCase);
            verify(telemetryService).trackCourtCaseDefendantEvent(TelemetryEventType.DEFENDANT_LINKED, hearing.getDefendants().get(0), CASE_ID);
            verify(hearingRepository).save(existingCase);
            verify(offenderRepository).findByCrn(CRN);
            verify(offenderRepository).save(existingOffender);
            assertThat(savedCourtCase).isNotNull();
            verifyNoMoreInteractions(telemetryService, hearingRepository, offenderRepository);
        }

        @Test
        void givenExistingCaseWithCrn_whenCreateOrUpdateCaseCalledWithNullCrn_thenLogUnLinkedEvent() {
            when(hearingRepository.findFirstByCaseIdOrderByIdDesc(CASE_ID)).thenReturn(Optional.of(hearing));
            when(hearingRepository.save(hearing)).thenReturn(hearing);

            var updatedCourtCase = EntityHelper.aHearingEntity(null, CASE_NO);

            var savedCourtCase = service.createHearing(CASE_ID, updatedCourtCase).block();

            verify(telemetryService).trackCourtCaseEvent(TelemetryEventType.COURT_CASE_UPDATED, updatedCourtCase);
            verify(telemetryService).trackCourtCaseDefendantEvent(TelemetryEventType.DEFENDANT_UNLINKED, hearing.getDefendants().get(0), CASE_ID);
            verify(hearingRepository).save(updatedCourtCase);
            assertThat(savedCourtCase).isNotNull();
            verifyNoMoreInteractions(telemetryService, hearingRepository, offenderRepository);
        }

        @Test
        void givenNewCaseWithTwoDefendants_whenCreateCase_thenLogCreatedAndOneDefendantLinkedEvent() {
            var linkedDefendant = aDefendantEntity("abc", CRN);
            var unlinkedDefendant = aDefendantEntity("def", null);
            var newCourtCase = hearing.withDefendants(List.of(linkedDefendant, unlinkedDefendant));
            when(hearingRepository.findFirstByCaseIdOrderByIdDesc(CASE_ID)).thenReturn(Optional.empty());
            when(hearingRepository.save(newCourtCase)).thenReturn(newCourtCase);
            when(offenderRepository.findByCrn(CRN)).thenReturn(Optional.empty());

            var savedCourtCase = service.createHearing(CASE_ID, newCourtCase).block();

            verify(telemetryService).trackCourtCaseEvent(TelemetryEventType.COURT_CASE_CREATED, newCourtCase);
            verify(telemetryService).trackCourtCaseDefendantEvent(TelemetryEventType.DEFENDANT_LINKED, linkedDefendant, CASE_ID);
            verify(hearingRepository).save(newCourtCase);
            verify(offenderRepository).findByCrn(CRN);
            verify(offenderRepository).save(linkedDefendant.getOffender());
            assertThat(savedCourtCase).isNotNull();
            verifyNoMoreInteractions(telemetryService, hearingRepository, offenderRepository);
        }

        @Test
        void givenUnknownCourtCode_whenCreateOrUpdateCase_thenThrowException() {
            hearing = EntityHelper.aHearingEntity(null, CASE_NO);
            when(hearingRepository.findFirstByCaseIdOrderByIdDesc(CASE_ID)).thenReturn(Optional.empty());
            when(hearingRepository.save(hearing)).thenReturn(hearing);
            when(courtRepository.findByCourtCode(COURT_CODE)).thenReturn(Optional.of(courtEntity));
            when(courtRepository.findByCourtCode("XXX")).thenReturn(Optional.empty());


            hearing = HearingEntity.builder()
                    .hearingDays(List.of(
                            HearingDayEntity.builder()
                                    .courtCode(COURT_CODE)
                                    .build(),
                            HearingDayEntity.builder()
                                    .courtCode("XXX")
                                    .build()

                    ))
                    .courtCase(CourtCaseEntity.builder()
                        .caseId(CASE_ID)
                    .build())
                    .build();

            service.createHearing(CASE_ID, hearing).block();

            verify(courtRepository).findByCourtCode("XXX");
            verify(telemetryService).trackCourtCaseEvent(TelemetryEventType.COURT_CASE_CREATED, hearing);
            verify(hearingRepository).save(hearing);
            verifyNoMoreInteractions(hearingRepository, telemetryService, offenderRepository);
        }

        @Test
        void givenCaseIdMismatch_whenCreateOrUpdateCase_thenThrowException() {
            hearing = HearingEntity.builder()
                    .hearingDays(List.of(
                            HearingDayEntity.builder()
                                    .courtCode(COURT_CODE)
                                    .build()
                    ))
                    .courtCase(CourtCaseEntity.builder()
                        .caseId("xcx")
                    .build())
                    .build();
            Assertions.assertThrows(ConflictingInputException.class, () -> {
                service.createHearing(CASE_ID, hearing).block();
            });
            verify(courtRepository).findByCourtCode(COURT_CODE);
            verifyNoMoreInteractions(courtRepository, hearingRepository, telemetryService);
        }
    }

    @ExtendWith(MockitoExtension.class)
    @Nested
    @DisplayName("Tests for filterCasesByCourtAndDate")
    class FilterTest {
        private final LocalDateTime CREATED_AFTER = LocalDateTime.of(2020, 10, 9, 12, 50);
        private final LocalDate SEARCH_DATE = LocalDate.of(2020, 1, 16);

        @Mock
        private List<HearingEntity> caseList;

        @BeforeEach
        void setup() {
            service = new ImmutableCourtCaseService(courtRepository, courtCaseRepository, hearingRepository, telemetryService, groupedOffenderMatchRepository, offenderRepository);
        }

        @Test
        void givenCreatedBeforeIsNull_filterByDateShouldRetrieveCourtCasesFromRepository() {
            when(courtRepository.findByCourtCode(COURT_CODE)).thenReturn(Optional.of(courtEntity));
            when(courtEntity.getCourtCode()).thenReturn(COURT_CODE);
            when(hearingRepository.findByCourtCodeAndHearingDay(COURT_CODE, SEARCH_DATE, CREATED_AFTER, CREATED_BEFORE))
                    .thenReturn(caseList);

            var courtCaseEntities = service.filterHearings(COURT_CODE, SEARCH_DATE, CREATED_AFTER, CREATED_BEFORE);

            assertThat(courtCaseEntities).isEqualTo(caseList);
            verify(hearingRepository).findByCourtCodeAndHearingDay(COURT_CODE, SEARCH_DATE, CREATED_AFTER, CREATED_BEFORE);
            verifyNoMoreInteractions(hearingRepository);
        }

        @Test
        void givenUseExtendedCases_filterByHearingDayShouldRetrieveCourtCasesFromRepository() {
            service = new ImmutableCourtCaseService(courtRepository, courtCaseRepository, hearingRepository, telemetryService, groupedOffenderMatchRepository, offenderRepository);
            when(courtRepository.findByCourtCode(COURT_CODE)).thenReturn(Optional.of(courtEntity));
            when(courtEntity.getCourtCode()).thenReturn(COURT_CODE);
            when(hearingRepository.findByCourtCodeAndHearingDay(COURT_CODE, SEARCH_DATE, CREATED_AFTER, CREATED_BEFORE))
                    .thenReturn(caseList);

            var courtCaseEntities = service.filterHearings(COURT_CODE, SEARCH_DATE, CREATED_AFTER, CREATED_BEFORE);

            assertThat(courtCaseEntities).isEqualTo(caseList);
            verify(hearingRepository).findByCourtCodeAndHearingDay(COURT_CODE, SEARCH_DATE, CREATED_AFTER, CREATED_BEFORE);
            verifyNoMoreInteractions(hearingRepository);
        }

        @Test
        void givenCreatedBeforeIsNotNull_filterByDateShouldRetrieveCourtCasesFromRepository() {
            when(courtRepository.findByCourtCode(COURT_CODE)).thenReturn(Optional.of(courtEntity));
            when(courtEntity.getCourtCode()).thenReturn(COURT_CODE);
            when(hearingRepository.findByCourtCodeAndHearingDay(COURT_CODE, SEARCH_DATE, CREATED_AFTER, CREATED_BEFORE))
                    .thenReturn(caseList);

            var courtCaseEntities = service.filterHearings(COURT_CODE, SEARCH_DATE, CREATED_AFTER, CREATED_BEFORE);

            assertThat(courtCaseEntities).isEqualTo(caseList);
        }

        @Test
        void givenCreatedBeforeIsNull_filterByDateShouldThrowNotFoundExceptionIfCourtCodeNotFound() {
            when(courtRepository.findByCourtCode(COURT_CODE)).thenReturn(Optional.empty());

            var exception = catchThrowable(() ->
                    service.filterHearings(COURT_CODE, SEARCH_DATE, CREATED_AFTER, null));
            assertThat(exception).isInstanceOf(EntityNotFoundException.class)
                    .hasMessageContaining("Court " + COURT_CODE + " not found");

        }

        @Test
        void whenFilterByCourtAndDateForLastModified_thenReturn() {
            final var expectedLastModified = LocalDateTime.of(2021, 6, 1, 16, 59, 59);
            when(hearingRepository.findLastModifiedByHearingDay(COURT_CODE, SEARCH_DATE))
                    .thenReturn(Optional.of(expectedLastModified));

            var lastModified = service.filterHearingsLastModified(COURT_CODE, SEARCH_DATE);

            assertThat(lastModified).isPresent();
            assertThat(lastModified.get()).isEqualTo(expectedLastModified);
        }

        @Test
        void whenFilterByCourtAndDateForLastModified_andNoneFound_thenReturnEmpty() {
            when(hearingRepository.findLastModifiedByHearingDay(COURT_CODE, SEARCH_DATE))
                    .thenReturn(Optional.empty());

            var lastModified = service.filterHearingsLastModified(COURT_CODE, SEARCH_DATE);

            assertThat(lastModified).isEmpty();
        }
    }

    @ExtendWith(MockitoExtension.class)
    @Nested
    @DisplayName("Tests for getting case by case no or case id")
    class GetCaseByCaseNoOrCaseIdTest {

        @BeforeEach
        void setup() {
            service = new ImmutableCourtCaseService(courtRepository, courtCaseRepository, hearingRepository, telemetryService, groupedOffenderMatchRepository, offenderRepository);
        }

        @Test
        void getCourtCaseShouldRetrieveCaseFromRepository() {
            when(courtRepository.findByCourtCode(COURT_CODE)).thenReturn(Optional.of(courtEntity));
            when(hearingRepository.findByCourtCodeAndCaseNo(COURT_CODE, CASE_NO)).thenReturn(Optional.of(EntityHelper.aHearingEntity(CRN, CASE_NO)));

            service.getHearingByCaseNumber(COURT_CODE, CASE_NO);
            verify(hearingRepository).findByCourtCodeAndCaseNo(COURT_CODE, CASE_NO);
        }

        @Test
        void getCourtCaseShouldThrowNotFoundException() {
            when(courtRepository.findByCourtCode(COURT_CODE)).thenReturn(Optional.of(courtEntity));
            when(hearingRepository.findByCourtCodeAndCaseNo(COURT_CODE, CASE_NO)).thenReturn(Optional.empty());

            var exception = catchThrowable(() ->
                    service.getHearingByCaseNumber(COURT_CODE, CASE_NO)
            );
            assertThat(exception).isInstanceOf(EntityNotFoundException.class)
                    .hasMessageContaining("Case " + CASE_NO + " not found for court " + COURT_CODE);
        }

        @Test
        void getCourtCaseShouldThrowIncorrectCourtException() {
            when(courtRepository.findByCourtCode(COURT_CODE)).thenReturn(Optional.empty());

            var exception = catchThrowable(() ->
                    service.getHearingByCaseNumber(COURT_CODE, CASE_NO)
            );
            assertThat(exception).isInstanceOf(EntityNotFoundException.class)
                    .hasMessageContaining("Court " + COURT_CODE + " not found");
        }

        @Test
        void givenExistingCaseId_getCourtCaseByCaseId_thenRetrieve() {
            var caseEntityFromRepo = EntityHelper.aHearingEntityWithCrn(CRN);
            when(hearingRepository.findByCaseId(CASE_ID)).thenReturn(Optional.of(caseEntityFromRepo));

            var caseEntity = service.getHearingByCaseId(CASE_ID);
            assertThat(caseEntity).isSameAs(caseEntityFromRepo);
            verify(hearingRepository).findByCaseId(CASE_ID);
        }

        @Test
        void givenNonExistentCaseId_whenGetCourtCaseByCaseId_thenThrow() {
            when(hearingRepository.findByCaseId(CASE_ID)).thenReturn(Optional.empty());

            var exception = catchThrowable(() ->
                    service.getHearingByCaseId(CASE_ID)
            );
            assertThat(exception).isInstanceOf(EntityNotFoundException.class)
                    .hasMessageContaining("Case " + CASE_ID + " not found");
        }

        @Test
        void whenGetCourtCaseByIdAndDefendantId_shouldRetrieveCaseFromRepository() {
            final var courtCaseEntity = EntityHelper.aHearingEntityWithCrn(CRN);
            when(hearingRepository.findByCaseIdAndDefendantId(CASE_ID, DEFENDANT_ID)).thenReturn(Optional.of(courtCaseEntity));

            final var entity = service.getHearingByCaseIdAndDefendantId(CASE_ID, DEFENDANT_ID);

            assertThat(entity).isSameAs(courtCaseEntity);
            verify(hearingRepository).findByCaseIdAndDefendantId(CASE_ID, DEFENDANT_ID);
            verifyNoMoreInteractions(hearingRepository);
        }

        @Test
        void givenNoMatch_whenGetCourtCaseByIdAndDefendantId_shouldThrowException() {
            when(hearingRepository.findByCaseIdAndDefendantId(CASE_ID, DEFENDANT_ID)).thenReturn(Optional.empty());

            var exception = catchThrowable(() ->
                    service.getHearingByCaseIdAndDefendantId(CASE_ID, DEFENDANT_ID)
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

        @Captor
        private ArgumentCaptor<CourtCaseEntity> courtCaseCaptor;

        @BeforeEach
        void setup() {
            service = new ImmutableCourtCaseService(courtRepository, courtCaseRepository, hearingRepository, telemetryService, groupedOffenderMatchRepository, offenderRepository);
        }

        @Test
        void givenOffenderMatchesExistForCaseWithMultipleDefendants_whenCrnUpdated_thenUpdateMatches() {
            final var matchCrn = "X11111";
            final var rejectedCrn = "X99999";
            when(courtRepository.findByCourtCode(COURT_CODE)).thenReturn(Optional.of(courtEntity));
            var existingCase = EntityHelper.aHearingEntity(null, CASE_NO, List.of(
                    aDefendantEntity("defendant1", null)
            ));
            when(hearingRepository.findFirstByCaseIdOrderByIdDesc(CASE_ID)).thenReturn(Optional.of(existingCase));
            when(groupedOffenderMatchRepository.findByCaseIdAndDefendantId(CASE_ID, "defendant1"))
                    .thenReturn(buildOffenderMatches(matchCrn, rejectedCrn));
            when(groupedOffenderMatchRepository.findByCaseIdAndDefendantId(CASE_ID, "defendant2")).thenReturn(Optional.empty());
            var caseToUpdate = EntityHelper.aHearingEntity(null, CASE_NO, List.of(
                    aDefendantEntity("defendant1", matchCrn),
                    aDefendantEntity("defendant2", "X99999")
            ));

            when(hearingRepository.save(caseToUpdate)).thenReturn(caseToUpdate);

            service.createHearing(CASE_ID, caseToUpdate).block();

            verify(hearingRepository).save(caseToUpdate);
            assertThat(caseToUpdate.getHearingId()).isEqualTo(CASE_ID);
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

            verifyNoMoreInteractions(groupedOffenderMatchRepository, hearingRepository);
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

    record CourtCaseEntityMatcher(String caseId,
                                  List<String> defendantIds) implements ArgumentMatcher<HearingEntity> {

        @Override
        public boolean matches(HearingEntity arg) {
            final var argDefendantIds = Optional.ofNullable(arg.getDefendants()).orElse(Collections.emptyList())
                    .stream()
                    .map(DefendantEntity::getDefendantId)
                    .collect(Collectors.toList());
            return caseId.equals(arg.getCaseId()) && defendantIds.equals(argDefendantIds);
        }
    }

}
