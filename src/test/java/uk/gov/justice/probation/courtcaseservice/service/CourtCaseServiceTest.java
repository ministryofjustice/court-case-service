package uk.gov.justice.probation.courtcaseservice.service;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.time.Month;
import java.util.Arrays;
import java.util.InputMismatchException;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.platform.commons.util.StringUtils;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import uk.gov.justice.probation.courtcaseservice.jpa.entity.AddressPropertiesEntity;
import uk.gov.justice.probation.courtcaseservice.jpa.entity.CourtCaseEntity;
import uk.gov.justice.probation.courtcaseservice.jpa.entity.CourtEntity;
import uk.gov.justice.probation.courtcaseservice.jpa.entity.OffenceEntity;
import uk.gov.justice.probation.courtcaseservice.jpa.repository.CourtCaseRepository;
import uk.gov.justice.probation.courtcaseservice.jpa.repository.CourtRepository;
import uk.gov.justice.probation.courtcaseservice.service.exceptions.EntityNotFoundException;

import static java.util.Collections.singletonList;
import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatExceptionOfType;
import static org.assertj.core.api.ThrowableAssert.catchThrowable;
import static org.mockito.Mockito.eq;
import static org.mockito.Mockito.mock;
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
    private static final LocalDateTime LAST_UPDATED = LocalDateTime.of(2020, 2, 25, 9, 0);
    private static final boolean SUSPENDED_SENTENCE = true;
    private static final boolean BREACH = true;
    private static final String DEFENDANT_NAME = "JTEST";
    private static final AddressPropertiesEntity DEFENDANT_ADDRESS = new AddressPropertiesEntity("27", "Elm Place", "AB21 3ES", "Bangor", null, null);
    private static final String CRN = "CRN";
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
    private CourtEntity courtEntity;

    @Mock
    private List<CourtCaseEntity> caseList;

    private CourtCaseEntity courtCase;

    @InjectMocks
    private CourtCaseService service;

    @BeforeEach
    void setup() {
        courtCase = buildCourtCase();
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
        when(courtCaseRepository.findByCourtCodeAndCaseNo(COURT_CODE, CASE_NO)).thenReturn(Optional.of(courtCase));

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
    void givenMismatchInputCourtCode_whenUpdateByCourtAndCase_ThenThrowInputMismatch() {
        when(courtRepository.findByCourtCode(COURT_CODE)).thenReturn(Optional.of(courtEntity));
        String misMatchCourt = "NWS";
        assertThatExceptionOfType(InputMismatchException.class).isThrownBy( () ->
            service.createOrUpdateCase(misMatchCourt, CASE_NO, courtCase)
        ).withMessage("Case No " + CASE_NO + " and Court Code " + misMatchCourt + " do not match with values from body " + CASE_NO + " and " + COURT_CODE);
    }

    @Test
    void givenMismatchInputCaseNo_whenUpdateByCourtAndCase_ThenThrowInputMismatch() {
        when(courtRepository.findByCourtCode(COURT_CODE)).thenReturn(Optional.of(courtEntity));
        String misMatchCaseNo = "999";
        assertThatExceptionOfType(InputMismatchException.class).isThrownBy( () ->
            service.createOrUpdateCase(COURT_CODE, misMatchCaseNo, courtCase)
        ).withMessage("Case No " + misMatchCaseNo + " and Court Code " + COURT_CODE + " do not match with values from body " + CASE_NO + " and " + COURT_CODE);
    }

    @Test
    void whenDeleteMissingCases_ThenCallRepo() {

        LocalDateTime start = LocalDateTime.of(2020, Month.JANUARY, 1, 0, 0);
        LocalDateTime end = LocalDateTime.of(2020, Month.JANUARY, 2, 0, 0);
        List<String> caseNos = Arrays.asList("100", "101");
        final Map<LocalDate, List<String>> existingCases = Map.of(start.toLocalDate(), Arrays.asList("100", "101"));
        when(courtRepository.findByCourtCode(COURT_CODE)).thenReturn(Optional.of(courtEntity));
        CourtCaseEntity caseToDelete = mock(CourtCaseEntity.class);
        when(courtCaseRepository.findCourtCasesNotIn(COURT_CODE, start, end, caseNos)).thenReturn(singletonList(caseToDelete));

        service.deleteAbsentCases(COURT_CODE, existingCases);

        verify(courtCaseRepository).deleteAll(Set.of(caseToDelete));
    }

    @Test
    void givenUnknownCourt_whenDeleteMissingCases_ThenThrow() {

        when(courtRepository.findByCourtCode(COURT_CODE)).thenReturn(Optional.empty());
        LocalDateTime start = LocalDateTime.of(2020, Month.JANUARY, 1, 0, 0);
        final Map<LocalDate, List<String>> existingCases = Map.of(start.toLocalDate(), Arrays.asList("100", "101"));

        var exception = catchThrowable(() ->
            service.deleteAbsentCases(COURT_CODE, existingCases));
        assertThat(exception).isInstanceOf(EntityNotFoundException.class)
            .hasMessageContaining("Court " + COURT_CODE + " not found");
    }

    static CourtCaseEntity buildCourtCase() {
        CourtCaseEntity courtCaseEntity = CourtCaseEntity.builder().caseId(CASE_ID)
            .lastUpdated(LAST_UPDATED)
            .breach(BREACH)
            .caseNo(CASE_NO)
            .courtCode(COURT_CODE)
            .courtRoom(COURT_ROOM)
            .defendantAddress(DEFENDANT_ADDRESS)
            .defendantName(DEFENDANT_NAME)
            .defendantDob(DEFENDANT_DOB)
            .defendantSex(DEFENDANT_SEX)
            .crn(CRN)
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

    static OffenceEntity buildOffenceEntity(String sequenceNumber, CourtCaseEntity courtCaseEntity) {
        if (StringUtils.isBlank(sequenceNumber)) {
            return OffenceEntity.builder().act("ACT-NULL").build();
        }

        return OffenceEntity.builder()
            .sequenceNumber(Integer.valueOf(sequenceNumber))
            .offenceTitle(OFFENCE_TITLE)
            .offenceSummary(OFFENCE_SUMMARY)
            .act("ACT" + sequenceNumber)
            .courtCase(courtCaseEntity)
            .build();
    }

}
