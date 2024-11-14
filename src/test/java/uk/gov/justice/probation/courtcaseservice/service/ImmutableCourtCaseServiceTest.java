package uk.gov.justice.probation.courtcaseservice.service;

import kotlin.Pair;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.ArgumentMatcher;
import org.mockito.Captor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;
import org.springframework.lang.NonNull;
import uk.gov.justice.probation.courtcaseservice.controller.exceptions.ConflictingInputException;
import uk.gov.justice.probation.courtcaseservice.controller.model.HearingPrepStatus;
import uk.gov.justice.probation.courtcaseservice.controller.model.HearingSearchRequest;
import uk.gov.justice.probation.courtcaseservice.jpa.DTOHelper;
import uk.gov.justice.probation.courtcaseservice.jpa.dto.HearingDefendantDTO;
import uk.gov.justice.probation.courtcaseservice.jpa.entity.CourtCaseEntity;
import uk.gov.justice.probation.courtcaseservice.jpa.entity.CourtEntity;
import uk.gov.justice.probation.courtcaseservice.jpa.entity.DefendantEntity;
import uk.gov.justice.probation.courtcaseservice.jpa.entity.EntityHelper;
import uk.gov.justice.probation.courtcaseservice.jpa.entity.GroupedOffenderMatchesEntity;
import uk.gov.justice.probation.courtcaseservice.jpa.entity.HearingDayEntity;
import uk.gov.justice.probation.courtcaseservice.jpa.entity.HearingDefendantEntity;
import uk.gov.justice.probation.courtcaseservice.jpa.entity.HearingEntity;
import uk.gov.justice.probation.courtcaseservice.jpa.entity.HearingEventType;
import uk.gov.justice.probation.courtcaseservice.jpa.entity.NamePropertiesEntity;
import uk.gov.justice.probation.courtcaseservice.jpa.entity.OffenderEntity;
import uk.gov.justice.probation.courtcaseservice.jpa.entity.OffenderMatchEntity;
import uk.gov.justice.probation.courtcaseservice.jpa.entity.OffenderProbationStatus;
import uk.gov.justice.probation.courtcaseservice.jpa.repository.CourtCaseRepository;
import uk.gov.justice.probation.courtcaseservice.jpa.repository.CourtRepository;
import uk.gov.justice.probation.courtcaseservice.jpa.repository.GroupedOffenderMatchRepository;
import uk.gov.justice.probation.courtcaseservice.jpa.repository.HearingRepository;
import uk.gov.justice.probation.courtcaseservice.jpa.repository.HearingRepositoryFacade;
import uk.gov.justice.probation.courtcaseservice.jpa.repository.PagedCaseListRepositoryCustom;
import uk.gov.justice.probation.courtcaseservice.service.exceptions.EntityNotFoundException;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.ThrowableAssert.catchThrowable;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.lenient;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoMoreInteractions;
import static org.mockito.Mockito.when;
import static uk.gov.justice.probation.courtcaseservice.jpa.DTOHelper.DEFENDANT_ID_2;
import static uk.gov.justice.probation.courtcaseservice.jpa.entity.EntityHelper.CASE_ID;
import static uk.gov.justice.probation.courtcaseservice.jpa.entity.EntityHelper.COURT_CODE;
import static uk.gov.justice.probation.courtcaseservice.jpa.entity.EntityHelper.CRN;
import static uk.gov.justice.probation.courtcaseservice.jpa.entity.EntityHelper.HEARING_ID;
import static uk.gov.justice.probation.courtcaseservice.jpa.entity.EntityHelper.LIST_NO;
import static uk.gov.justice.probation.courtcaseservice.jpa.entity.EntityHelper.PROBATION_STATUS;

@ExtendWith(MockitoExtension.class)
class ImmutableCourtCaseServiceTest {

    private static final String CASE_NO = "1600028912";
    private static final LocalDateTime CREATED_BEFORE = LocalDateTime.of(2020, 11, 9, 12, 50);

    @Mock
    private CourtRepository courtRepository;
    @Mock
    private HearingRepositoryFacade hearingRepositoryFacade;
    @Mock
    private CourtEntity courtEntity;
    @Mock
    private GroupedOffenderMatchRepository groupedOffenderMatchRepository;
    @Mock
    private TelemetryService telemetryService;

    private ImmutableCourtCaseService service;
    @Mock
    private DomainEventService domainEventService;
    @Mock
    private CourtCaseRepository courtCaseRepository;
    @Mock
    private ShortTermCustodyPredictorService shortTermCustodyPredictorService;
    @Mock
    private HearingRepository hearingRepository;
    @Mock
    private PagedCaseListRepositoryCustom pagedCaseListRepositoryCustom;

    @ExtendWith(MockitoExtension.class)
    @Nested
    @DisplayName("Tests for createCase by case ID and defendant ID")
    class CreateUpdateByCaseAndDefendantIdTest {

        private HearingEntity incomingHearing;
        private HearingDefendantEntity defendant;
        private OffenderEntity offender;

        @BeforeEach
        void setup() {
            service = new ImmutableCourtCaseService(courtRepository, hearingRepositoryFacade, telemetryService, groupedOffenderMatchRepository,
                    domainEventService, courtCaseRepository, shortTermCustodyPredictorService, hearingRepository,
                    pagedCaseListRepositoryCustom);
            lenient().when(courtRepository.findByCourtCode(COURT_CODE)).thenReturn(Optional.of(courtEntity));
            incomingHearing = EntityHelper.aHearingEntity(CRN, CASE_NO);
            offender = OffenderEntity.builder().crn("X99999").probationStatus(OffenderProbationStatus.of(PROBATION_STATUS)).build();
            defendant = buildHearingDefendant(offender);
        }

        private HearingDefendantEntity buildHearingDefendant(OffenderEntity offender) {
            return HearingDefendantEntity.builder()
                    .defendantId(EntityHelper.DEFENDANT_ID)
                    .defendant(DefendantEntity.builder()
                            .defendantId(EntityHelper.DEFENDANT_ID)
                            .offender(offender)
                            .personId("1")
                            .build())
                    .build();
        }
    }

    @ExtendWith(MockitoExtension.class)
    @Nested
    @DisplayName("Tests for createCase by case ID with ExtendedCourtCaseRequest")
    @Disabled("ImmutableCourtCaseService.createHearing is deprecated")
    class CreateUpdateByCaseIdTest {

        private HearingEntity hearing;

        @BeforeEach
        void setup() {
            service = new ImmutableCourtCaseService(courtRepository, hearingRepositoryFacade, telemetryService, groupedOffenderMatchRepository,
                    domainEventService, courtCaseRepository, shortTermCustodyPredictorService,hearingRepository,
                    pagedCaseListRepositoryCustom);
            lenient().when(courtRepository.findByCourtCode(COURT_CODE)).thenReturn(Optional.of(courtEntity));
            hearing = EntityHelper.aHearingEntity(CRN, CASE_NO);
        }

        @Test
        void givenNoExistingCase_whenCreateOrUpdateCaseCalledWithLinkedDefendant_thenLogCreatedAndLinkedEvent() {
            when(hearingRepositoryFacade.findFirstByHearingId(HEARING_ID)).thenReturn(Optional.empty());
            when(hearingRepositoryFacade.save(hearing)).thenReturn(hearing);

            var savedCourtCase = service.createHearing(CASE_ID, hearing).block();

            verify(telemetryService).trackCourtCaseEvent(TelemetryEventType.COURT_CASE_CREATED, hearing);
            verify(telemetryService).trackCourtCaseDefendantEvent(TelemetryEventType.DEFENDANT_LINKED, hearing.getHearingDefendants().get(0), hearing.getCaseId());
            verify(hearingRepositoryFacade).save(hearing);
            assertThat(savedCourtCase).isNotNull();
            assertThat(savedCourtCase.getHearingId()).isEqualTo(HEARING_ID);
            verifyNoMoreInteractions(hearingRepositoryFacade, telemetryService);
        }

        @Test
        void givenNoExistingCase_whenCreateCaseCalledWithoutCrn_thenLogOnlyCreatedEvent() {

            hearing = EntityHelper.aHearingEntity(null, CASE_NO);
            when(hearingRepositoryFacade.findFirstByHearingId(HEARING_ID)).thenReturn(Optional.empty());
            when(hearingRepositoryFacade.save(hearing)).thenReturn(hearing);

            var savedCourtCase = service.createHearing(CASE_ID, hearing).block();

            verify(telemetryService).trackCourtCaseEvent(TelemetryEventType.COURT_CASE_CREATED, hearing);
            verify(hearingRepositoryFacade).save(hearing);
            verifyNoMoreInteractions(hearingRepositoryFacade, telemetryService);
            assertThat(savedCourtCase).isNotNull();
        }

        @Test
        void givenExistingCase_whenCreateOrUpdateCaseCalled_thenLogUpdatedEvent() {
            when(hearingRepositoryFacade.findFirstByHearingId(HEARING_ID)).thenReturn(Optional.of(hearing));
            when(hearingRepositoryFacade.save(hearing)).thenReturn(hearing);

            var savedCourtCase = service.createHearing(CASE_ID, hearing).block();

            verify(telemetryService).trackCourtCaseEvent(TelemetryEventType.COURT_CASE_UPDATED, hearing);
            verify(hearingRepositoryFacade).save(hearing);
            assertThat(savedCourtCase).isNotNull();
            verifyNoMoreInteractions(telemetryService, hearingRepositoryFacade);
        }

        @Test
        void givenExistingCaseWithNullCrn_whenCreateOrUpdateCaseCalledWithCrn_thenLogLinkedEvent() {
            var existingCase = EntityHelper.aHearingEntity(null, CASE_NO);
            when(hearingRepositoryFacade.findFirstByHearingId(HEARING_ID)).thenReturn(Optional.of(existingCase));
            when(hearingRepositoryFacade.save(existingCase)).thenReturn(existingCase);

            var savedCourtCase = service.createHearing(CASE_ID, EntityHelper.aHearingEntity(CRN, CASE_NO)).block();

            verify(telemetryService).trackCourtCaseEvent(TelemetryEventType.COURT_CASE_UPDATED, existingCase);
            verify(telemetryService).trackCourtCaseDefendantEvent(TelemetryEventType.DEFENDANT_LINKED, hearing.getHearingDefendants().get(0), CASE_ID);
            verify(hearingRepositoryFacade).save(existingCase);
            assertThat(savedCourtCase).isNotNull();
            verifyNoMoreInteractions(telemetryService, hearingRepositoryFacade);
        }

        @Test
        void givenExistingCaseWithCrn_whenCreateOrUpdateCaseCalledWithNullCrn_thenLogUnLinkedEvent() {
            when(hearingRepositoryFacade.findFirstByHearingId(HEARING_ID)).thenReturn(Optional.of(hearing));
            when(hearingRepositoryFacade.save(hearing)).thenReturn(hearing);

            var updatedCourtCase = EntityHelper.aHearingEntity(null, CASE_NO);

            var savedCourtCase = service.createHearing(CASE_ID, updatedCourtCase).block();

            verify(telemetryService).trackCourtCaseEvent(TelemetryEventType.COURT_CASE_UPDATED, updatedCourtCase);
            verify(telemetryService).trackCourtCaseDefendantEvent(TelemetryEventType.DEFENDANT_UNLINKED, hearing.getHearingDefendants().get(0), CASE_ID);
            verify(hearingRepositoryFacade).save(updatedCourtCase);
            assertThat(savedCourtCase).isNotNull();
            verifyNoMoreInteractions(telemetryService, hearingRepositoryFacade);
        }

        @Test
        void givenNewCaseWithTwoDefendants_whenCreateCase_thenLogCreatedAndOneDefendantLinkedEvent() {
            var linkedDefendant = EntityHelper.aHearingDefendantEntity("abc", CRN);
            var unlinkedDefendant = EntityHelper.aHearingDefendantEntity("def", null);
            var newCourtCase = hearing.withHearingDefendants(List.of(linkedDefendant, unlinkedDefendant));
            when(hearingRepositoryFacade.findFirstByHearingId(HEARING_ID)).thenReturn(Optional.empty());
            when(hearingRepositoryFacade.save(newCourtCase)).thenReturn(newCourtCase);

            var savedCourtCase = service.createHearing(CASE_ID, newCourtCase).block();

            verify(telemetryService).trackCourtCaseEvent(TelemetryEventType.COURT_CASE_CREATED, newCourtCase);
            verify(telemetryService).trackCourtCaseDefendantEvent(TelemetryEventType.DEFENDANT_LINKED, linkedDefendant, CASE_ID);
            verify(hearingRepositoryFacade).save(newCourtCase);
            assertThat(savedCourtCase).isNotNull();
            verifyNoMoreInteractions(telemetryService, hearingRepositoryFacade);
        }

        @Test
        void givenUnknownCourtCode_whenCreateOrUpdateCase_thenSaveTheUnknownCourt() {
            hearing = HearingEntity.builder()
                    .hearingId(HEARING_ID)
                    .hearingDays(List.of(
                            HearingDayEntity.builder()
                                    .courtCode(COURT_CODE)
                                    .build(),
                            HearingDayEntity.builder()
                                    .courtCode("XXX")
                                    .build()

                    ))
                    .hearingDefendants(Collections.emptyList())
                    .courtCase(CourtCaseEntity.builder()
                            .caseId(CASE_ID)
                            .build())
                    .build();

            when(hearingRepositoryFacade.findFirstByHearingId(HEARING_ID)).thenReturn(Optional.empty());
            when(hearingRepositoryFacade.save(hearing)).thenReturn(hearing);
            when(courtRepository.findByCourtCode(COURT_CODE)).thenReturn(Optional.of(courtEntity));
            when(courtRepository.findByCourtCode("XXX")).thenReturn(Optional.empty());

            service.createHearing(CASE_ID, hearing).block();

            verify(courtRepository).findByCourtCode("XXX");
            verify(telemetryService).trackCourtCaseEvent(TelemetryEventType.COURT_CASE_CREATED, hearing);
            verify(hearingRepositoryFacade).save(hearing);
            verify(courtRepository).save(any(CourtEntity.class));
            verifyNoMoreInteractions(hearingRepositoryFacade, telemetryService, courtRepository);
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
            final var exception = Assertions.assertThrows(ConflictingInputException.class, () -> service.createHearing(CASE_ID, hearing).block());
            verify(courtRepository).findByCourtCode(COURT_CODE);
            verifyNoMoreInteractions(courtRepository, hearingRepositoryFacade, telemetryService);
            assertThat(exception.getMessage()).isEqualTo(String.format("Case Id %s does not match with value from body %s",
                    CASE_ID, "xcx"));
        }
    }

    @ExtendWith(MockitoExtension.class)
    @Nested
    @DisplayName("Tests for createCase by hearing ID with ExtendedHearingRequestResponse")
    class CreateUpdateByHearingIdTest {

        private HearingEntity hearing;

        @BeforeEach
        void setup() {
            service = new ImmutableCourtCaseService(courtRepository, hearingRepositoryFacade, telemetryService, groupedOffenderMatchRepository,
                    domainEventService, courtCaseRepository, shortTermCustodyPredictorService, hearingRepository,
                    pagedCaseListRepositoryCustom);
            lenient().when(courtRepository.findByCourtCode(COURT_CODE)).thenReturn(Optional.of(courtEntity));
            hearing = EntityHelper.aHearingEntity(CRN, CASE_NO);
        }

        @Test
        void givenNoExistingCase_whenCreateOrUpdateCaseCalledWithLinkedDefendant_thenLogCreatedAndLinkedEvent() {
            when(hearingRepositoryFacade.findFirstByHearingIdAndCourtCaseId(HEARING_ID, CASE_ID)).thenReturn(Optional.empty());
            when(hearingRepositoryFacade.save(hearing)).thenReturn(hearing);

            var savedCourtCase = service.createOrUpdateHearingByHearingId(HEARING_ID, hearing).block();

            verify(telemetryService).trackCourtCaseEvent(TelemetryEventType.COURT_CASE_CREATED, hearing);
            verify(telemetryService).trackCourtCaseDefendantEvent(TelemetryEventType.DEFENDANT_LINKED, hearing.getHearingDefendants().get(0), hearing.getCaseId());
            verify(hearingRepositoryFacade).save(hearing);
            assertThat(savedCourtCase).isNotNull();
            assertThat(savedCourtCase.getHearingId()).isEqualTo(HEARING_ID);
            verifyNoMoreInteractions(hearingRepositoryFacade, telemetryService);
        }

        @Test
        void givenNoExistingCase_whenCreateCaseCalledWithoutCrn_thenLogOnlyCreatedEvent() {

            hearing = EntityHelper.aHearingEntity(null, CASE_NO);
            when(hearingRepositoryFacade.findFirstByHearingIdAndCourtCaseId(HEARING_ID, CASE_ID)).thenReturn(Optional.empty());
            when(hearingRepositoryFacade.save(hearing)).thenReturn(hearing);

            var savedCourtCase = service.createOrUpdateHearingByHearingId(HEARING_ID, hearing).block();

            verify(telemetryService).trackCourtCaseEvent(TelemetryEventType.COURT_CASE_CREATED, hearing);
            verify(hearingRepositoryFacade).save(hearing);
            verifyNoMoreInteractions(hearingRepositoryFacade, telemetryService);
            assertThat(savedCourtCase).isNotNull();
        }

        @Test
        void givenExistingCase_whenCreateOrUpdateCaseCalled_thenLogUpdatedEvent() {
            when(hearingRepositoryFacade.findFirstByHearingIdAndCourtCaseId(HEARING_ID, CASE_ID)).thenReturn(Optional.of(hearing));
            when(hearingRepositoryFacade.save(hearing)).thenReturn(hearing);

            var savedCourtCase = service.createOrUpdateHearingByHearingId(HEARING_ID, hearing).block();

            verify(telemetryService).trackCourtCaseEvent(TelemetryEventType.COURT_CASE_UPDATED, hearing);
            verify(hearingRepositoryFacade).save(hearing);
            assertThat(savedCourtCase).isNotNull();
            verifyNoMoreInteractions(telemetryService, hearingRepositoryFacade);
        }

        @Test
        void givenExistingCaseWithNullCrn_whenCreateOrUpdateCaseCalledWithCrn_thenLogLinkedEvent() {
            var existingCase = EntityHelper.aHearingEntity(null, CASE_NO);
            when(hearingRepositoryFacade.findFirstByHearingIdAndCourtCaseId(HEARING_ID, CASE_ID)).thenReturn(Optional.of(existingCase));
            when(hearingRepositoryFacade.save(existingCase)).thenReturn(existingCase);

            var savedCourtCase = service.createOrUpdateHearingByHearingId(HEARING_ID, EntityHelper.aHearingEntity(CRN, CASE_NO)).block();

            verify(telemetryService).trackCourtCaseEvent(TelemetryEventType.COURT_CASE_UPDATED, existingCase);
            verify(telemetryService).trackCourtCaseDefendantEvent(TelemetryEventType.DEFENDANT_LINKED, hearing.getHearingDefendants().get(0), CASE_ID);
            verify(hearingRepositoryFacade).save(existingCase);
            assertThat(savedCourtCase).isNotNull();
            verifyNoMoreInteractions(telemetryService, hearingRepositoryFacade);
        }

        @Test
        void givenExistingCaseWithCrn_whenCreateOrUpdateCaseCalledWithNullCrn_thenLogUnLinkedEvent() {
            when(hearingRepositoryFacade.findFirstByHearingIdAndCourtCaseId(HEARING_ID, CASE_ID)).thenReturn(Optional.of(hearing));
            when(hearingRepositoryFacade.save(hearing)).thenReturn(hearing);

            var updatedCourtCase = EntityHelper.aHearingEntity(null, CASE_NO);

            var savedCourtCase = service.createOrUpdateHearingByHearingId(HEARING_ID, updatedCourtCase).block();

            verify(telemetryService).trackCourtCaseEvent(TelemetryEventType.COURT_CASE_UPDATED, updatedCourtCase);
            verify(telemetryService).trackCourtCaseDefendantEvent(TelemetryEventType.DEFENDANT_UNLINKED, hearing.getHearingDefendants().get(0), CASE_ID);
            verify(hearingRepositoryFacade).save(updatedCourtCase);
            assertThat(savedCourtCase).isNotNull();
            verifyNoMoreInteractions(telemetryService, hearingRepositoryFacade);
        }

        @Test
        void givenNewCaseWithTwoDefendants_whenCreateCase_thenLogCreatedAndOneDefendantLinkedEvent() {
            var linkedDefendant = EntityHelper.aHearingDefendantEntity("abc", CRN);
            var unlinkedDefendant = EntityHelper.aHearingDefendantEntity("def", null);
            var newCourtCase = hearing.withHearingDefendants(List.of(linkedDefendant, unlinkedDefendant));
            when(hearingRepositoryFacade.findFirstByHearingIdAndCourtCaseId(HEARING_ID, CASE_ID)).thenReturn(Optional.empty());
            when(hearingRepositoryFacade.save(newCourtCase)).thenReturn(newCourtCase);

            var savedCourtCase = service.createOrUpdateHearingByHearingId(HEARING_ID, newCourtCase).block();

            verify(telemetryService).trackCourtCaseEvent(TelemetryEventType.COURT_CASE_CREATED, newCourtCase);
            verify(telemetryService).trackCourtCaseDefendantEvent(TelemetryEventType.DEFENDANT_LINKED, linkedDefendant, CASE_ID);
            verify(hearingRepositoryFacade).save(newCourtCase);
            assertThat(savedCourtCase).isNotNull();
            verifyNoMoreInteractions(telemetryService, hearingRepositoryFacade);
        }

        @Test
        void givenUnknownCourtCode_whenCreateOrUpdateCase_thenSaveTheUnknownCourt() {
            hearing = HearingEntity.builder()
                    .hearingId(HEARING_ID)
                    .hearingDays(List.of(
                            HearingDayEntity.builder()
                                    .courtCode(COURT_CODE)
                                    .build(),
                            HearingDayEntity.builder()
                                    .courtCode("XXX")
                                    .build()

                    ))
                    .hearingDefendants(Collections.emptyList())
                    .courtCase(CourtCaseEntity.builder()
                            .caseId(CASE_ID)
                            .build())
                    .build();

            when(hearingRepositoryFacade.findFirstByHearingIdAndCourtCaseId(HEARING_ID, CASE_ID)).thenReturn(Optional.empty());
            when(hearingRepositoryFacade.save(hearing)).thenReturn(hearing);
            when(courtRepository.findByCourtCode(COURT_CODE)).thenReturn(Optional.of(courtEntity));
            when(courtRepository.findByCourtCode("XXX")).thenReturn(Optional.empty());

            service.createOrUpdateHearingByHearingId(HEARING_ID, hearing).block();

            verify(courtRepository).findByCourtCode("XXX");
            verify(telemetryService).trackCourtCaseEvent(TelemetryEventType.COURT_CASE_CREATED, hearing);
            verify(hearingRepositoryFacade).save(hearing);
            verify(courtRepository).save(any(CourtEntity.class));
            verifyNoMoreInteractions(hearingRepositoryFacade, telemetryService, courtRepository);
        }

        @Test
        void givenCaseIdMismatch_whenCreateOrUpdateCase_thenThrowException() {
            String invalidHearingId = "non-matching-hearing-id";
            hearing = HearingEntity.builder()
                    .hearingId(invalidHearingId)
                    .hearingDays(List.of(
                            HearingDayEntity.builder()
                                    .courtCode(COURT_CODE)
                                    .build()
                    ))
                    .build();
            final var exception = Assertions.assertThrows(ConflictingInputException.class, () -> service.createOrUpdateHearingByHearingId(HEARING_ID, hearing).block());
            verify(courtRepository).findByCourtCode(COURT_CODE);
            verifyNoMoreInteractions(courtRepository, hearingRepositoryFacade, telemetryService);
            assertThat(exception.getMessage()).isEqualTo(String.format("Hearing Id %s does not match with value from body %s",
                    HEARING_ID, invalidHearingId));
        }

        @Test
        void givenExistingCase_whenCreateOrUpdateCaseCalled_WithResultedHearingEventType_thenEmitSentencedEvent() {
            HearingEntity resultedHearingEntity = EntityHelper.aHearingEntity(CRN, CASE_NO)
                    .withHearingEventType(HearingEventType.RESULTED);
            when(hearingRepositoryFacade.findFirstByHearingIdAndCourtCaseId(HEARING_ID, CASE_ID)).thenReturn(Optional.of(resultedHearingEntity));
            when(hearingRepositoryFacade.save(resultedHearingEntity)).thenReturn(resultedHearingEntity);

            var savedCourtCase = service.createOrUpdateHearingByHearingId(HEARING_ID, resultedHearingEntity).block();

            verify(telemetryService).trackCourtCaseEvent(TelemetryEventType.COURT_CASE_UPDATED, resultedHearingEntity);
            verify(domainEventService).emitSentencedEvent(resultedHearingEntity);
            verify(hearingRepositoryFacade).save(resultedHearingEntity);
            assertThat(savedCourtCase).isNotNull();
            verifyNoMoreInteractions(telemetryService, domainEventService, hearingRepositoryFacade);
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
            service = new ImmutableCourtCaseService(courtRepository, hearingRepositoryFacade, telemetryService, groupedOffenderMatchRepository,
                    domainEventService, courtCaseRepository, shortTermCustodyPredictorService, hearingRepository,
                    pagedCaseListRepositoryCustom);
        }

        @Test
        void givenCreatedBeforeIsNull_filterByDateShouldRetrieveCourtCasesFromRepository() {
            when(courtRepository.findByCourtCode(COURT_CODE)).thenReturn(Optional.of(courtEntity));
            when(courtEntity.getCourtCode()).thenReturn(COURT_CODE);
            when(hearingRepositoryFacade.findByCourtCodeAndHearingDay(COURT_CODE, SEARCH_DATE, CREATED_AFTER, CREATED_BEFORE))
                    .thenReturn(caseList);

            var courtCaseEntities = service.filterHearings(COURT_CODE, SEARCH_DATE, CREATED_AFTER, CREATED_BEFORE);

            assertThat(courtCaseEntities).isEqualTo(caseList);
            verify(hearingRepositoryFacade).findByCourtCodeAndHearingDay(COURT_CODE, SEARCH_DATE, CREATED_AFTER, CREATED_BEFORE);
            verifyNoMoreInteractions(hearingRepositoryFacade);
        }

        @Test
        void givenUseExtendedCases_filterByHearingDayShouldRetrieveCourtCasesFromRepository() {
            service = new ImmutableCourtCaseService(courtRepository, hearingRepositoryFacade, telemetryService, groupedOffenderMatchRepository,
                    domainEventService, courtCaseRepository, shortTermCustodyPredictorService,
                    hearingRepository, pagedCaseListRepositoryCustom);
            when(courtRepository.findByCourtCode(COURT_CODE)).thenReturn(Optional.of(courtEntity));
            when(courtEntity.getCourtCode()).thenReturn(COURT_CODE);
            when(hearingRepositoryFacade.findByCourtCodeAndHearingDay(COURT_CODE, SEARCH_DATE, CREATED_AFTER, CREATED_BEFORE))
                    .thenReturn(caseList);

            var courtCaseEntities = service.filterHearings(COURT_CODE, SEARCH_DATE, CREATED_AFTER, CREATED_BEFORE);

            assertThat(courtCaseEntities).isEqualTo(caseList);
            verify(hearingRepositoryFacade).findByCourtCodeAndHearingDay(COURT_CODE, SEARCH_DATE, CREATED_AFTER, CREATED_BEFORE);
            verifyNoMoreInteractions(hearingRepositoryFacade);
        }

        @Test
        void givenCreatedBeforeIsNotNull_filterByDateShouldRetrieveCourtCasesFromRepository() {
            when(courtRepository.findByCourtCode(COURT_CODE)).thenReturn(Optional.of(courtEntity));
            when(courtEntity.getCourtCode()).thenReturn(COURT_CODE);
            when(hearingRepositoryFacade.findByCourtCodeAndHearingDay(COURT_CODE, SEARCH_DATE, CREATED_AFTER, CREATED_BEFORE))
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
            when(hearingRepositoryFacade.findLastModifiedByHearingDay(COURT_CODE, SEARCH_DATE))
                    .thenReturn(Optional.of(expectedLastModified));

            var lastModified = service.filterHearingsLastModified(COURT_CODE, SEARCH_DATE);

            assertThat(lastModified).isPresent();
            assertThat(lastModified.get()).isEqualTo(expectedLastModified);
        }

        @Test
        void whenFilterByCourtAndDateForLastModified_andNoneFound_thenReturnEmpty() {
            when(hearingRepositoryFacade.findLastModifiedByHearingDay(COURT_CODE, SEARCH_DATE))
                    .thenReturn(Optional.empty());

            var lastModified = service.filterHearingsLastModified(COURT_CODE, SEARCH_DATE);

            assertThat(lastModified).isEmpty();
        }
    }

    @ExtendWith(MockitoExtension.class)
    @Nested
    @DisplayName("Tests for paged and filtered case-list endpoint")
    class PagedHearingFilterTest {
        private final LocalDateTime CREATED_AFTER = LocalDateTime.of(2020, 10, 9, 12, 50);
        private final LocalDate SEARCH_DATE = LocalDate.of(2020, 1, 16);

        @Mock
        private List<HearingEntity> caseList;

        @BeforeEach
        void setup() {
            service = new ImmutableCourtCaseService(courtRepository, hearingRepositoryFacade, telemetryService, groupedOffenderMatchRepository,
                    domainEventService, courtCaseRepository, shortTermCustodyPredictorService,
                    hearingRepository, pagedCaseListRepositoryCustom);
        }

        @Test
        void shouldInvokePagedCaseListRepositoryAndTransformResult() {

            LocalDate hearingDay = DTOHelper.SESSION_START_TIME.toLocalDate();

            var req = new HearingSearchRequest(
                    List.of(),
                    List.of(),
                    List.of(),
                    List.of(),
                    false,
                    false,
                    null,
                    hearingDay,
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

            when(groupedOffenderMatchRepository.getPossibleMatchesCountByDate(COURT_CODE, hearingDay)).thenReturn(Optional.of(2));
            when(hearingRepository.getRecentlyAddedCasesCount(COURT_CODE, hearingDay)).thenReturn(Optional.of(2));
            when(hearingRepository.getCourtroomsForCourtAndHearingDay(COURT_CODE, hearingDay)).thenReturn(List.of("01", "03", "04", "05", "1", "Crown Court 5-1"));

            HearingDefendantDTO hearingDefendant = DTOHelper.aHearingDefendantDTO(DTOHelper.DEFENDANT_NAME);
            hearingDefendant.setPrepStatus(String.valueOf(HearingPrepStatus.IN_PROGRESS));
            hearingDefendant.setHearing(DTOHelper.aHearingDTO(CASE_ID));

            Pair pair = new Pair(hearingDefendant, 5);
            when(pagedCaseListRepositoryCustom.filterHearings(COURT_CODE, req))
                    .thenReturn(new PageImpl<>(List.of(pair, pair, pair, pair, pair), Pageable.ofSize(5).withPage(1), 11));

            var result = service.filterHearings(COURT_CODE, req);

            assertThat(result.getCases().size()).isEqualTo(5);
            assertThat(result.getTotalElements()).isEqualTo(11);
            assertThat(result.getTotalPages()).isEqualTo(3);
            assertThat(result.getPossibleMatchesCount()).isEqualTo(2);
            assertThat(result.getRecentlyAddedCount()).isEqualTo(2);
            assertThat(result.getCourtRoomFilters()).containsAll(List.of("01", "03", "04", "05", "1", "Crown Court 5-1"));

            verify(pagedCaseListRepositoryCustom).filterHearings(COURT_CODE, req);
            verify(hearingRepository).getCourtroomsForCourtAndHearingDay(COURT_CODE, hearingDay);
            verify(hearingRepository).getRecentlyAddedCasesCount(COURT_CODE, hearingDay);
            verify(groupedOffenderMatchRepository).getPossibleMatchesCountByDate(COURT_CODE, hearingDay);
            verifyNoMoreInteractions(hearingRepository);
        }

        @Test
        void filterCourtCaseResponsesWithSampleCriteriaForMatcher() {
            HearingDefendantDTO hearingDefendant = DTOHelper.aNoRecordHearingDefendantDTO(5990, DTOHelper.DEFENDANT_ID);
            hearingDefendant.setPrepStatus(String.valueOf(HearingPrepStatus.IN_PROGRESS));
            hearingDefendant.setHearing(DTOHelper.aHearingDTO(CASE_ID));
            HearingDefendantDTO hearingDefendant2 = DTOHelper.aNoRecordHearingDefendantDTO(5991, DEFENDANT_ID_2,
                    NamePropertiesEntity.builder().forename1("Joe").surname("BLOGGS").title("Mr").build());
            hearingDefendant2.setPrepStatus(String.valueOf(HearingPrepStatus.IN_PROGRESS));
            hearingDefendant2.setHearing(DTOHelper.aHearingDTO("ac24a1be-939b-49a4-a524-21a3d228f8b"));
            Pair pair = new Pair(hearingDefendant, 2);
            Pair pair2 = new Pair(hearingDefendant2, 2);
            PageImpl pageImpl = new PageImpl<>(List.of(pair, pair2), Pageable.ofSize(5).withPage(1), 1);

            var forename = new HearingSearchRequest(List.of(), List.of(), List.of(), List.of(),false,false,
                    null, DTOHelper.SESSION_START_TIME.toLocalDate(),1,5,null,
                    "Gordon",null,null,null,null,null);
            when(pagedCaseListRepositoryCustom.filterHearings(COURT_CODE, forename))
                    .thenReturn(pageImpl);
            var resultForename = service.filterHearingsForMatcher(COURT_CODE,
                    forename);
            assertThat(resultForename.getCases().size()).isEqualTo(1);
            assertThat(resultForename.getCases().getFirst().getDefendantForename()).isEqualTo("Gordon");

            var surname = new HearingSearchRequest(List.of(), List.of(), List.of(), List.of(),false,false,
                    null,DTOHelper.SESSION_START_TIME.toLocalDate(),1,5,null,
                    null,"bennett",null,null,null,null);
            when(pagedCaseListRepositoryCustom.filterHearings(COURT_CODE, surname)).thenReturn(pageImpl);
            var resultSurname = service.filterHearingsForMatcher(COURT_CODE, surname);
            assertThat(resultSurname.getCases().size()).isEqualTo(1);
            assertThat(resultSurname.getCases().getFirst().getDefendantSurname()).isEqualTo("BENNETT");

            var numberOfMatches = new HearingSearchRequest(List.of(), List.of(), List.of(), List.of(),false,false,
                    null,DTOHelper.SESSION_START_TIME.toLocalDate(),1,5,2L,
                    null,null,null,null,null,null);
            when(pagedCaseListRepositoryCustom.filterHearings(COURT_CODE, numberOfMatches)).thenReturn(pageImpl);
            var resultNumberOfMatches = service.filterHearingsForMatcher(COURT_CODE, numberOfMatches);
            assertThat(resultNumberOfMatches.getCases().size()).isEqualTo(2);
            assertThat(resultNumberOfMatches.getCases().getFirst().getNumberOfPossibleMatches()).isEqualTo(2);

            var defendantName = new HearingSearchRequest(List.of(), List.of(), List.of(), List.of(),false,false,
                    null,DTOHelper.SESSION_START_TIME.toLocalDate(),1,5,null,
                    null,null,"Mr Joe Bloggs",null,null,null);
            when(pagedCaseListRepositoryCustom.filterHearings(COURT_CODE, defendantName)).thenReturn(pageImpl);
            var resultDefendantName = service.filterHearingsForMatcher(COURT_CODE, defendantName);
            assertThat(resultDefendantName.getCases().size()).isEqualTo(1);
            assertThat(resultDefendantName.getCases().getFirst().getDefendantName()).isEqualTo("Mr Joe BLOGGS");

            var caseId = new HearingSearchRequest(List.of(), List.of(), List.of(), List.of(),false,false,
                    null,DTOHelper.SESSION_START_TIME.toLocalDate(),1,5,null,
                    null,null,null,CASE_ID,null,null);
            when(pagedCaseListRepositoryCustom.filterHearings(COURT_CODE, caseId)).thenReturn(pageImpl);
            var resultCaseId = service.filterHearingsForMatcher(COURT_CODE, caseId);
            assertThat(resultCaseId.getCases().size()).isEqualTo(1);
            assertThat(resultCaseId.getCases().getFirst().getCaseId()).isEqualTo(CASE_ID);
            assertThat(resultCaseId.getCases().getFirst().getDefendantName()).isEqualTo("Mr Gordon BENNETT");

            var hearingId = new HearingSearchRequest(List.of(), List.of(), List.of(), List.of(),false,false,
                    null,DTOHelper.SESSION_START_TIME.toLocalDate(),1,5,null,
                    null,null,null, null, HEARING_ID,null);
            when(pagedCaseListRepositoryCustom.filterHearings(COURT_CODE, hearingId)).thenReturn(pageImpl);
            var resultHearingId = service.filterHearingsForMatcher(COURT_CODE, hearingId);
            assertThat(resultHearingId.getCases().size()).isEqualTo(2);
            assertThat(resultHearingId.getCases().getFirst().getHearingId()).isEqualTo(HEARING_ID);

            var defendantId = new HearingSearchRequest(List.of(), List.of(), List.of(), List.of(),false,false,
                    null,DTOHelper.SESSION_START_TIME.toLocalDate(),1,5,null,
                    null,null,null, null, null, DEFENDANT_ID_2);
            when(pagedCaseListRepositoryCustom.filterHearings(COURT_CODE, defendantId)).thenReturn(pageImpl);
            var resultDefendantId = service.filterHearingsForMatcher(COURT_CODE, defendantId);
            assertThat(resultDefendantId.getCases().size()).isEqualTo(1);
            assertThat(resultDefendantId.getCases().getFirst().getDefendantId()).isEqualTo(DEFENDANT_ID_2);
        }
    }

    @ExtendWith(MockitoExtension.class)
    @Nested
    @DisplayName("Tests for getting case by case no or case id")
    class GetCaseByCaseNoOrCaseIdTest {

        @BeforeEach
        void setup() {
            service = new ImmutableCourtCaseService(courtRepository, hearingRepositoryFacade, telemetryService, groupedOffenderMatchRepository,
                    domainEventService, courtCaseRepository, shortTermCustodyPredictorService,
                    hearingRepository, pagedCaseListRepositoryCustom);
        }

        @Test
        void getCourtCaseShouldRetrieveCaseFromRepository() {
            when(courtRepository.findByCourtCode(COURT_CODE)).thenReturn(Optional.of(courtEntity));
            when(hearingRepositoryFacade.findByCourtCodeAndCaseNo(COURT_CODE, CASE_NO, LIST_NO)).thenReturn(Optional.of(EntityHelper.aHearingEntity(CRN, CASE_NO)));

            service.getHearingByCaseNumber(COURT_CODE, CASE_NO, LIST_NO);
            verify(hearingRepositoryFacade).findByCourtCodeAndCaseNo(COURT_CODE, CASE_NO, LIST_NO);
        }

        @Test
        void getCourtCaseShouldThrowNotFoundException() {
            when(courtRepository.findByCourtCode(COURT_CODE)).thenReturn(Optional.of(courtEntity));
            when(hearingRepositoryFacade.findByCourtCodeAndCaseNo(COURT_CODE, CASE_NO, LIST_NO)).thenReturn(Optional.empty());

            var exception = catchThrowable(() ->
                    service.getHearingByCaseNumber(COURT_CODE, CASE_NO, LIST_NO)
            );
            assertThat(exception).isInstanceOf(EntityNotFoundException.class)
                    .hasMessageContaining("Case " + CASE_NO + " not found for court " + COURT_CODE);
        }

        @Test
        void getCourtCaseShouldThrowIncorrectCourtException() {
            when(courtRepository.findByCourtCode(COURT_CODE)).thenReturn(Optional.empty());

            var exception = catchThrowable(() ->
                    service.getHearingByCaseNumber(COURT_CODE, CASE_NO, LIST_NO)
            );
            assertThat(exception).isInstanceOf(EntityNotFoundException.class)
                    .hasMessageContaining("Court " + COURT_CODE + " not found");
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
            service = new ImmutableCourtCaseService(courtRepository, hearingRepositoryFacade, telemetryService, groupedOffenderMatchRepository,
                    domainEventService, courtCaseRepository, shortTermCustodyPredictorService,
                    hearingRepository, pagedCaseListRepositoryCustom);
        }

        @Test
        @Disabled
        void givenOffenderMatchesExistForCaseWithMultipleDefendants_whenCrnUpdated_thenUpdateMatches() {
            final var matchCrn = "X11111";
            final var rejectedCrn = "X99999";
            when(courtRepository.findByCourtCode(COURT_CODE)).thenReturn(Optional.of(courtEntity));
            var existingCase = EntityHelper.aHearingEntity(null, CASE_NO, List.of(
                    EntityHelper.aHearingDefendantEntity("defendant1", null)
            ));
            when(hearingRepositoryFacade.findFirstByHearingId(HEARING_ID)).thenReturn(Optional.of(existingCase));
            when(groupedOffenderMatchRepository.findByCaseIdAndDefendantId(CASE_ID, "defendant1"))
                    .thenReturn(buildOffenderMatches());
            when(groupedOffenderMatchRepository.findByCaseIdAndDefendantId(CASE_ID, "defendant2")).thenReturn(Optional.empty());
            var caseToUpdate = EntityHelper.aHearingEntity(null, CASE_NO, List.of(
                    EntityHelper.aHearingDefendantEntity("defendant1", matchCrn),
                    EntityHelper.aHearingDefendantEntity("defendant2", "X99999")
            ));

            when(hearingRepositoryFacade.save(caseToUpdate)).thenReturn(caseToUpdate);

            service.createHearing(CASE_ID, caseToUpdate).block();

            verify(hearingRepositoryFacade).save(caseToUpdate);
            assertThat(caseToUpdate.getHearingId()).isEqualTo(HEARING_ID);
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

            verifyNoMoreInteractions(groupedOffenderMatchRepository, hearingRepositoryFacade);
        }

        @NonNull
        private Optional<GroupedOffenderMatchesEntity> buildOffenderMatches() {
            return Optional.ofNullable(GroupedOffenderMatchesEntity.builder()
                    .offenderMatches(Arrays.asList(OffenderMatchEntity.builder()
                                    .crn("X11111")
                                    .confirmed(false)
                                    .rejected(false)
                                    .build(),
                            OffenderMatchEntity.builder()
                                    .crn("X99999")
                                    .confirmed(false)
                                    .rejected(false)
                                    .build()))
                    .build());
        }
    }

    @ExtendWith(MockitoExtension.class)
    @Nested
    @DisplayName("Tests for get court case data by hearing id")
    class GetByHearingIdTest {

        @BeforeEach
        void setup() {
            service = new ImmutableCourtCaseService(courtRepository, hearingRepositoryFacade, telemetryService,
                    groupedOffenderMatchRepository, domainEventService, courtCaseRepository,
                    shortTermCustodyPredictorService, hearingRepository, pagedCaseListRepositoryCustom);
        }

        @Test
        void giveHearingExistWithHearingId_whenGetByHearingId_thenReturnHearingEntity() {
            when(hearingRepositoryFacade.findFirstByHearingId(HEARING_ID))
                    .thenReturn(Optional.of(HearingEntity.builder().build()));
            final var actual = service.getHearingByHearingId(HEARING_ID);
            verify(hearingRepositoryFacade).findFirstByHearingId(HEARING_ID);
            assertThat(actual).isEqualTo(HearingEntity.builder().build());
        }

        @Test
        void giveHearingDoesNoeExistWithHearingId_whenGetByHearingId_thenThrowEntityNotFoundException() {
            when(hearingRepositoryFacade.findFirstByHearingId(HEARING_ID))
                    .thenReturn(Optional.ofNullable(null));
            final var exception = assertThrows(EntityNotFoundException.class, () -> service.getHearingByHearingId(HEARING_ID));
            assertThat(exception.getMessage()).isEqualTo(String.format("Hearing %s not found", HEARING_ID));
        }

        @Test
        void givenHearingExistWithHearingId_whenGetByHearingIdAndCourtCaseId_thenReturnHearingEntity() {
            when(hearingRepositoryFacade.findFirstByHearingIdAndCourtCaseId(HEARING_ID, CASE_ID))
                    .thenReturn(Optional.of(HearingEntity.builder().build()));
            final var actual = service.getHearingByHearingIdAndCourtCaseId(HEARING_ID, CASE_ID);
            verify(hearingRepositoryFacade).findFirstByHearingIdAndCourtCaseId(HEARING_ID, CASE_ID);
            assertThat(actual).isEqualTo(HearingEntity.builder().build());
        }

        @Test
        void givenHearingDoesNoeExistWithHearingId_whenGetByHearingIdAndCourtCaseId_thenThrowEntityNotFoundException() {
            when(hearingRepositoryFacade.findFirstByHearingIdAndCourtCaseId(HEARING_ID, CASE_ID))
                    .thenReturn(Optional.empty());
            final var exception = assertThrows(EntityNotFoundException.class, () -> service.getHearingByHearingIdAndCourtCaseId(HEARING_ID, CASE_ID));
            assertThat(exception.getMessage()).isEqualTo(String.format("Hearing %s not found for court case %s", HEARING_ID, CASE_ID));
        }
    }

    record HearingEntityMatcher(String caseId,
                                List<String> defendantIds) implements ArgumentMatcher<HearingEntity> {

        @Override
        public boolean matches(HearingEntity arg) {
            final var argDefendantIds = Optional.ofNullable(arg.getHearingDefendants()).orElse(Collections.emptyList())
                    .stream()
                    .map(HearingDefendantEntity::getDefendantId)
                    .toList();
            return caseId.equals(arg.getCaseId()) && defendantIds.equals(argDefendantIds);
        }
    }

}
