package uk.gov.justice.probation.courtcaseservice.service;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.platform.commons.util.StringUtils;
import org.mockito.ArgumentCaptor;
import org.mockito.Captor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.lang.NonNull;
import uk.gov.justice.probation.courtcaseservice.controller.exceptions.ConflictingInputException;
import uk.gov.justice.probation.courtcaseservice.jpa.entity.AddressPropertiesEntity;
import uk.gov.justice.probation.courtcaseservice.jpa.entity.CourtCaseEntity;
import uk.gov.justice.probation.courtcaseservice.jpa.entity.CourtEntity;
import uk.gov.justice.probation.courtcaseservice.jpa.entity.GroupedOffenderMatchesEntity;
import uk.gov.justice.probation.courtcaseservice.jpa.entity.ImmutableOffenceEntity;
import uk.gov.justice.probation.courtcaseservice.jpa.entity.OffenderMatchEntity;
import uk.gov.justice.probation.courtcaseservice.jpa.repository.CourtCaseRepository;
import uk.gov.justice.probation.courtcaseservice.jpa.repository.CourtRepository;
import uk.gov.justice.probation.courtcaseservice.jpa.repository.GroupedOffenderMatchRepository;
import uk.gov.justice.probation.courtcaseservice.service.exceptions.EntityNotFoundException;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatExceptionOfType;
import static org.assertj.core.api.ThrowableAssert.catchThrowable;
import static org.mockito.Mockito.eq;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class CourtCaseServiceTest {

    private static final String CASE_ID = "CASE_ID";
    private static final String CASE_NO = "1600028912";
    private static final String COURT_CODE = "SHF";
    private static final String COURT_ROOM = "COURT_ROOM";
    private static final LocalDateTime SESSION_START_TIME = LocalDateTime.of(2020, 2, 26, 9, 0);
    private static final String PROBATION_STATUS = "PROBATION_STATUS";
    private static final LocalDate TERMINATION_DATE = LocalDate.of(2020, 2, 27);
    private static final LocalDate SEARCH_DATE = LocalDate.of(2020, 1, 16);
    private static final boolean SUSPENDED_SENTENCE = true;
    private static final boolean BREACH = true;
    private static final String DEFENDANT_NAME = "JTEST";
    private static final AddressPropertiesEntity DEFENDANT_ADDRESS = new AddressPropertiesEntity("27", "Elm Place", "AB21 3ES", "Bangor", null, null);
    static final String CRN = "CRN";
    private static final String PNC = "PNC";
    private static final String LIST_NO = "LIST_NO";
    private static final LocalDate DEFENDANT_DOB = LocalDate.of(1958, 12, 14);
    private static final String DEFENDANT_SEX = "M";
    private static final String NATIONALITY_1 = "British";
    private static final String NATIONALITY_2 = "Polish";
    private static final String OFFENCE_TITLE = "OFFENCE TITLE";
    private static final String OFFENCE_SUMMARY = "OFFENCE SUMMARY";

    @Mock
    private CourtRepository courtRepository;
    @Mock
    private CourtCaseRepository courtCaseRepository;
    @Mock
    private GroupedOffenderMatchRepository groupedOffenderMatchRepository;
    @Mock
    private CourtEntity courtEntity;
    @Mock
    private List<CourtCaseEntity> caseList;
    @Mock
    private TelemetryService telemetryService;
    @Captor
    ArgumentCaptor<GroupedOffenderMatchesEntity> matchesCaptor;

    private CourtCaseEntity courtCase;

    @InjectMocks
    private ImmutableCourtCaseService service;

    @BeforeEach
    void setup() {
        courtCase = buildCourtCase(CRN);
    }

    @Test
    public void givenNoExistingCase_whenCreateOrUpdateCaseCalled_thenLogCreatedEvent() {
        when(courtRepository.findByCourtCode(COURT_CODE)).thenReturn(Optional.of(courtEntity));
        when(courtCaseRepository.findTopByCourtCodeAndCaseNoOrderByCreatedDesc(COURT_CODE,CASE_NO)).thenReturn(Optional.empty());

        service.createCase(COURT_CODE, CASE_NO, courtCase);

        verify(telemetryService).trackCourtCaseEvent(TelemetryEventType.COURT_CASE_CREATED, courtCase);
    }

    @Test
    public void givenNoExistingCase_whenCreateOrUpdateCaseCalledWithCrn_thenLogLinkedEvent() {
        when(courtRepository.findByCourtCode(COURT_CODE)).thenReturn(Optional.of(courtEntity));
        when(courtCaseRepository.findTopByCourtCodeAndCaseNoOrderByCreatedDesc(COURT_CODE,CASE_NO)).thenReturn(Optional.empty());

        service.createCase(COURT_CODE, CASE_NO, courtCase);

        verify(telemetryService).trackCourtCaseEvent(TelemetryEventType.DEFENDANT_LINKED, courtCase);
    }

    @Test
    public void givenNoExistingCase_whenCreateOrUpdateCaseCalledWithNullCrn_thenDontLogLinkedEvent() {
        when(courtRepository.findByCourtCode(COURT_CODE)).thenReturn(Optional.of(courtEntity));
        when(courtCaseRepository.findTopByCourtCodeAndCaseNoOrderByCreatedDesc(COURT_CODE,CASE_NO)).thenReturn(Optional.empty());

        service.createCase(COURT_CODE, CASE_NO, buildCourtCase(null));

        verify(telemetryService, never()).trackCourtCaseEvent(TelemetryEventType.DEFENDANT_LINKED, courtCase);
    }

    @Test
    public void givenExistingCase_whenCreateOrUpdateCaseCalled_thenLogUpdatedEvent() {
        when(courtRepository.findByCourtCode(COURT_CODE)).thenReturn(Optional.of(courtEntity));
        when(courtCaseRepository.findTopByCourtCodeAndCaseNoOrderByCreatedDesc(COURT_CODE,CASE_NO)).thenReturn(Optional.of(courtCase));

        service.createCase(COURT_CODE, CASE_NO, courtCase);

        verify(telemetryService).trackCourtCaseEvent(TelemetryEventType.COURT_CASE_UPDATED, courtCase);
    }

    @Test
    public void givenExistingCaseWithNullCrn_whenCreateOrUpdateCaseCalledWithCrn_thenLogLinkedEvent() {
        when(courtRepository.findByCourtCode(COURT_CODE)).thenReturn(Optional.of(courtEntity));
        CourtCaseEntity existingCase = buildCourtCase(null);
        when(courtCaseRepository.findTopByCourtCodeAndCaseNoOrderByCreatedDesc(COURT_CODE,CASE_NO)).thenReturn(Optional.of(existingCase));
        when(courtCaseRepository.save(existingCase)).thenReturn(existingCase);

        service.createCase(COURT_CODE, CASE_NO, buildCourtCase(CRN));

        verify(telemetryService).trackCourtCaseEvent(TelemetryEventType.DEFENDANT_LINKED, this.courtCase);
    }

    @Test
    public void givenExistingCaseWithCrn_whenCreateOrUpdateCaseCalledWithNullCrn_thenLogUnLinkedEvent() {
        when(courtRepository.findByCourtCode(COURT_CODE)).thenReturn(Optional.of(courtEntity));
        when(courtCaseRepository.findTopByCourtCodeAndCaseNoOrderByCreatedDesc(COURT_CODE,CASE_NO)).thenReturn(Optional.of(courtCase));
        when(courtCaseRepository.save(courtCase)).thenReturn(courtCase);

        service.createCase(COURT_CODE, CASE_NO, buildCourtCase(null));

        verify(telemetryService).trackCourtCaseEvent(TelemetryEventType.DEFENDANT_UNLINKED, courtCase);
    }

    @Test
    public void whenUpdateMatches_thenLogRejectedAndConfirmedEvents() {
        when(courtRepository.findByCourtCode(COURT_CODE)).thenReturn(Optional.of(courtEntity));
        var caseToUpdate = buildCourtCase(CRN);
        var existingCase = buildCourtCase(null);

        when(groupedOffenderMatchRepository.findByCourtCodeAndCaseNo(COURT_CODE, CASE_NO)).thenReturn(buildOffenderMatches());
        when(courtCaseRepository.findTopByCourtCodeAndCaseNoOrderByCreatedDesc(COURT_CODE, CASE_NO)).thenReturn(Optional.of(existingCase));
        when(courtCaseRepository.save(existingCase)).thenReturn(existingCase);

        service.createCase(COURT_CODE, CASE_NO, caseToUpdate);

        verify(groupedOffenderMatchRepository).save(matchesCaptor.capture());

        var matches = matchesCaptor.getValue();

        var confirmedMatch = matches.getOffenderMatches().get(0);
        verify(telemetryService).trackMatchEvent(TelemetryEventType.MATCH_CONFIRMED, confirmedMatch, caseToUpdate);

        var rejectedMatch1 = matches.getOffenderMatches().get(1);
        verify(telemetryService).trackMatchEvent(TelemetryEventType.MATCH_REJECTED, rejectedMatch1, caseToUpdate);
    }

    @Test
    void filterByDateShouldRetrieveCourtCasesFromRepository() {
        when(courtRepository.findByCourtCode(COURT_CODE)).thenReturn(Optional.of(courtEntity));
        when(courtEntity.getCourtCode()).thenReturn(COURT_CODE);
        LocalDateTime startTime = LocalDateTime.of(SEARCH_DATE, LocalTime.MIDNIGHT);
        LocalDateTime endTime = startTime.plusDays(1);
        when(courtCaseRepository.findByCourtCodeAndSessionStartTimeBetween(eq(COURT_CODE), eq(startTime), eq(endTime))).thenReturn(caseList);

        List<CourtCaseEntity> courtCaseEntities = service.filterCasesByCourtAndDate(COURT_CODE, SEARCH_DATE);

        assertThat(courtCaseEntities).isEqualTo(caseList);
    }

    @Test
    void filterByDateShouldThrowNotFoundExceptionIfCourtCodeNotFound() {
        when(courtRepository.findByCourtCode(COURT_CODE)).thenReturn(Optional.empty());

        var exception = catchThrowable(() ->
                service.filterCasesByCourtAndDate(COURT_CODE, SEARCH_DATE));
        assertThat(exception).isInstanceOf(EntityNotFoundException.class)
                .hasMessageContaining("Court " + COURT_CODE + " not found");

    }

    @Test
    void getCourtCaseShouldRetrieveCaseFromRepository() {
        when(courtRepository.findByCourtCode(COURT_CODE)).thenReturn(Optional.of(courtEntity));
        when(courtCaseRepository.findTopByCourtCodeAndCaseNoOrderByCreatedDesc(COURT_CODE, CASE_NO)).thenReturn(Optional.of(courtCase));

        service.getCaseByCaseNumber(COURT_CODE, CASE_NO);
        verify(courtCaseRepository).findTopByCourtCodeAndCaseNoOrderByCreatedDesc(COURT_CODE, CASE_NO);
    }

    @Test
    void getCourtCaseShouldThrowNotFoundException() {
        when(courtRepository.findByCourtCode(COURT_CODE)).thenReturn(Optional.of(courtEntity));
        when(courtCaseRepository.findTopByCourtCodeAndCaseNoOrderByCreatedDesc(COURT_CODE, CASE_NO)).thenReturn(Optional.empty());

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
    void givenMismatchInputCourtCode_whenUpdateByCourtAndCase_ThenThrowInputMismatch() {
        when(courtRepository.findByCourtCode(COURT_CODE)).thenReturn(Optional.of(courtEntity));
        String misMatchCourt = "NWS";
        assertThatExceptionOfType(ConflictingInputException.class).isThrownBy( () ->
            service.createCase(misMatchCourt, CASE_NO, courtCase)
        ).withMessage("Case No " + CASE_NO + " and Court Code " + misMatchCourt + " do not match with values from body " + CASE_NO + " and " + COURT_CODE);
    }

    @Test
    void givenMismatchInputCaseNo_whenUpdateByCourtAndCase_ThenThrowInputMismatch() {
        when(courtRepository.findByCourtCode(COURT_CODE)).thenReturn(Optional.of(courtEntity));
        String misMatchCaseNo = "999";
        assertThatExceptionOfType(ConflictingInputException.class).isThrownBy( () ->
            service.createCase(COURT_CODE, misMatchCaseNo, courtCase)
        ).withMessage("Case No " + misMatchCaseNo + " and Court Code " + COURT_CODE + " do not match with values from body " + CASE_NO + " and " + COURT_CODE);
    }

    @Test
    public void givenOffenderMatchesExistForCase_whenCrnUpdated_thenUpdateMatches() {
        when(courtRepository.findByCourtCode(COURT_CODE)).thenReturn(Optional.of(courtEntity));
        CourtCaseEntity existingCase = buildCourtCase(CRN);
        when(groupedOffenderMatchRepository.findByCourtCodeAndCaseNo(COURT_CODE, CASE_NO)).thenReturn(buildOffenderMatches());
        CourtCaseEntity caseToUpdate = buildCourtCase(CRN);

        when(courtCaseRepository.findTopByCourtCodeAndCaseNoOrderByCreatedDesc(COURT_CODE, CASE_NO)).thenReturn(Optional.of(existingCase));
        when(courtCaseRepository.save(existingCase)).thenReturn(existingCase);

        service.createCase(COURT_CODE, CASE_NO, caseToUpdate);

        verify(groupedOffenderMatchRepository).save(matchesCaptor.capture());

        final var groupedOffenderMatches = matchesCaptor.getValue();

        OffenderMatchEntity correctMatch = groupedOffenderMatches.getOffenderMatches().get(0);
        assertThat(correctMatch.getCrn()).isEqualTo(CRN);
        assertThat(correctMatch.getConfirmed()).isEqualTo(true);
        assertThat(correctMatch.getRejected()).isEqualTo(false);

        OffenderMatchEntity rejectedMatch1 = groupedOffenderMatches.getOffenderMatches().get(1);
        assertThat(rejectedMatch1.getCrn()).isEqualTo("Rejected CRN 1");
        assertThat(rejectedMatch1.getConfirmed()).isEqualTo(false);
        assertThat(rejectedMatch1.getRejected()).isEqualTo(true);

    }

    @Test
    public void givenOffenderMatchesExistForCase_whenCrnRemoved_thenRejectAllMatches() {
        when(courtRepository.findByCourtCode(COURT_CODE)).thenReturn(Optional.of(courtEntity));
        when(groupedOffenderMatchRepository.findByCourtCodeAndCaseNo(COURT_CODE, CASE_NO)).thenReturn(buildOffenderMatches());
        CourtCaseEntity caseToUpdate = buildCourtCase(null);
        CourtCaseEntity existingCase = buildCourtCase(CRN);

        when(courtCaseRepository.findTopByCourtCodeAndCaseNoOrderByCreatedDesc(COURT_CODE, CASE_NO)).thenReturn(Optional.of(existingCase));
        when(courtCaseRepository.save(existingCase)).thenReturn(existingCase);

        service.createCase(COURT_CODE, CASE_NO, caseToUpdate);

        verify(groupedOffenderMatchRepository).save(matchesCaptor.capture());

        final var groupedOffenderMatches = matchesCaptor.getValue();

        OffenderMatchEntity rejectedMatch1 = groupedOffenderMatches.getOffenderMatches().get(0);
        assertThat(rejectedMatch1.getCrn()).isEqualTo(CRN);
        assertThat(rejectedMatch1.getConfirmed()).isEqualTo(false);
        assertThat(rejectedMatch1.getRejected()).isEqualTo(true);

        OffenderMatchEntity rejectedMatch2 = groupedOffenderMatches.getOffenderMatches().get(1);
        assertThat(rejectedMatch2.getCrn()).isEqualTo("Rejected CRN 1");
        assertThat(rejectedMatch2.getConfirmed()).isEqualTo(false);
        assertThat(rejectedMatch2.getRejected()).isEqualTo(true);

    }

    @Test
    public void givenMatchesDontExistForCase_whenCrnUpdated_thenDontThrowException() {
        when(courtRepository.findByCourtCode(COURT_CODE)).thenReturn(Optional.of(courtEntity));
        CourtCaseEntity caseToUpdate = buildCourtCase(CRN);
        when(courtCaseRepository.findTopByCourtCodeAndCaseNoOrderByCreatedDesc(COURT_CODE, CASE_NO)).thenReturn(Optional.of(caseToUpdate));
        when(courtCaseRepository.save(caseToUpdate)).thenReturn(caseToUpdate);

        service.createCase(COURT_CODE, CASE_NO, caseToUpdate);
    }

    @Test
    public void givenMatchesAreNullForCase_whenCrnUpdated_thenDontThrowException() {
        when(courtRepository.findByCourtCode(COURT_CODE)).thenReturn(Optional.of(courtEntity));
        when(groupedOffenderMatchRepository.findByCourtCodeAndCaseNo(COURT_CODE, CASE_NO)).thenReturn(Optional.empty());
        CourtCaseEntity caseToUpdate = buildCourtCase(CRN);
        when(courtCaseRepository.findTopByCourtCodeAndCaseNoOrderByCreatedDesc(COURT_CODE, CASE_NO)).thenReturn(Optional.of(caseToUpdate));
        when(courtCaseRepository.save(caseToUpdate)).thenReturn(caseToUpdate);

        service.createCase(COURT_CODE, CASE_NO, caseToUpdate);
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

    static CourtCaseEntity buildCourtCase(String crn) {
        CourtCaseEntity courtCaseEntity = CourtCaseEntity.builder().caseId(CASE_ID)
            .breach(BREACH)
            .caseNo(CASE_NO)
            .courtCode(COURT_CODE)
            .courtRoom(COURT_ROOM)
            .defendantAddress(DEFENDANT_ADDRESS)
            .defendantName(DEFENDANT_NAME)
            .defendantDob(DEFENDANT_DOB)
            .defendantSex(DEFENDANT_SEX)
            .crn(crn)
            .listNo(LIST_NO)
            .nationality1(NATIONALITY_1)
            .nationality2(NATIONALITY_2)
            .probationStatus(PROBATION_STATUS)
            .sessionStartTime(SESSION_START_TIME)
            .suspendedSentenceOrder(SUSPENDED_SENTENCE)
            .previouslyKnownTerminationDate(TERMINATION_DATE)
            .pnc(PNC)
            .build();
        courtCaseEntity.setOffences(List.of(buildOffenceEntity("1", courtCaseEntity)));
        return courtCaseEntity;
    }

    static ImmutableOffenceEntity buildOffenceEntity(String sequenceNumber, CourtCaseEntity courtCaseEntity) {
        if (StringUtils.isBlank(sequenceNumber)) {
            return ImmutableOffenceEntity.builder().act("ACT-NULL").build();
        }

        return ImmutableOffenceEntity.builder()
            .sequenceNumber(Integer.valueOf(sequenceNumber))
            .offenceTitle(OFFENCE_TITLE)
            .offenceSummary(OFFENCE_SUMMARY)
            .act("ACT" + sequenceNumber)
            .courtCase(courtCaseEntity)
            .build();
    }

}
