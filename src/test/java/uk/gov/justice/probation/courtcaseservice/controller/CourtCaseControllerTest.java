package uk.gov.justice.probation.courtcaseservice.controller;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import reactor.core.publisher.Mono;
import uk.gov.justice.probation.courtcaseservice.controller.model.CaseListResponse;
import uk.gov.justice.probation.courtcaseservice.controller.model.CourtCaseRequest;
import uk.gov.justice.probation.courtcaseservice.controller.model.CourtCaseResponse;
import uk.gov.justice.probation.courtcaseservice.jpa.entity.CourtCaseEntity;
import uk.gov.justice.probation.courtcaseservice.jpa.entity.CourtSession;
import uk.gov.justice.probation.courtcaseservice.service.CourtCaseService;
import uk.gov.justice.probation.courtcaseservice.service.OffenderMatchService;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.time.format.DateTimeFormatter;
import java.time.temporal.ChronoUnit;
import java.util.Collections;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
public class CourtCaseControllerTest {

    private static final String COURT_CODE = "COURT_CODE";
    private static final String CASE_NO = "CASE_NO";
    private static final LocalDate DATE = LocalDate.of(2020, 2, 24);
    private static final LocalDateTime CREATED_AFTER = LocalDateTime.of(2020, 2, 23, 0, 0);
    private static final LocalDateTime CREATED_BEFORE = LocalDateTime.of(2020, 3, 23, 0, 0);
    private final LocalDateTime now = LocalDateTime.now();
    @Mock
    private CourtCaseService courtCaseService;
    @Mock
    private CourtCaseRequest courtCaseUpdate;
    @Mock
    private OffenderMatchService offenderMatchService;
    @InjectMocks
    private CourtCaseController courtCaseController;
    private final CourtCaseEntity courtCaseEntity = CourtCaseEntity.builder()
        .caseNo(CASE_NO)
        .courtCode(COURT_CODE)
        .sessionStartTime(now)
        .build();

    private CourtSession session;

    @BeforeEach
    public void beforeEach() {
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("a");
        session = formatter.format(now).equalsIgnoreCase("am") ? CourtSession.MORNING : CourtSession.AFTERNOON;
    }

    @Test
    public void getCourtCase_shouldReturnCourtCaseResponse() {
        when(courtCaseService.getCaseByCaseNumber(COURT_CODE, CASE_NO)).thenReturn(courtCaseEntity);
        CourtCaseResponse courtCase = courtCaseController.getCourtCase(COURT_CODE, CASE_NO);
        assertThat(courtCase.getCourtCode()).isEqualTo(COURT_CODE);
        assertThat(courtCase.getCaseNo()).isEqualTo(CASE_NO);
        assertThat(courtCase.getSessionStartTime()).isNotNull();
        assertThat(courtCase.getSession()).isSameAs(session);
    }

    @Test
    public void updateCaseByCourtAndCaseNo_shouldReturnCourtCaseResponse() {
        when(courtCaseUpdate.asEntity()).thenReturn(courtCaseEntity);
        when(courtCaseService.createCase(COURT_CODE, CASE_NO, courtCaseEntity)).thenReturn(Mono.just(courtCaseEntity));
        CourtCaseResponse courtCase = courtCaseController.updateCourtCaseNo(COURT_CODE, CASE_NO, courtCaseUpdate).block();

        assertThat(courtCase.getCourtCode()).isEqualTo(COURT_CODE);
        assertThat(courtCase.getCaseNo()).isEqualTo(CASE_NO);
        assertThat(courtCase.getSessionStartTime()).isNotNull();
        assertThat(courtCase.getSession()).isSameAs(session);
    }

    @Test
    public void getCaseList_shouldReturnCourtCaseResponse() {

        when(courtCaseService.filterCasesByCourtAndDate(COURT_CODE, DATE, CREATED_AFTER, CREATED_BEFORE)).thenReturn(Collections.singletonList(courtCaseEntity));
        CaseListResponse caseList = courtCaseController.getCaseList(COURT_CODE, DATE, CREATED_AFTER, CREATED_BEFORE);

        assertThat(caseList.getCases()).hasSize(1);
        assertThat(caseList.getCases().get(0).getCourtCode()).isEqualTo(COURT_CODE);
        assertThat(caseList.getCases().get(0).getCaseNo()).isEqualTo(CASE_NO);
        assertThat(caseList.getCases().get(0).getSessionStartTime()).isNotNull();
        assertThat(caseList.getCases().get(0).getSession()).isSameAs(session);
    }

    @Test
    public void getCaseList_sorted() {
        CourtCaseController controller = new CourtCaseController(courtCaseService, offenderMatchService);

        LocalDateTime mornSession = LocalDateTime.of(DATE, LocalTime.of(9, 30));
        LocalDateTime aftSession = LocalDateTime.of(DATE, LocalTime.of(14, 0));
        CourtCaseEntity entity1 = CourtCaseEntity.builder().courtRoom("1").sessionStartTime(mornSession).defendantName("Mr Nicholas CAGE").build();
        CourtCaseEntity entity2 = CourtCaseEntity.builder().courtRoom("1").sessionStartTime(mornSession).defendantName("Mr Christopher PLUMMER").build();
        CourtCaseEntity entity3 = CourtCaseEntity.builder().courtRoom("1").sessionStartTime(aftSession).defendantName("Mr Darren ARONOFSKY").build();
        CourtCaseEntity entity4 = CourtCaseEntity.builder().courtRoom("3").sessionStartTime(mornSession).defendantName("Mrs Minnie DRIVER").build();
        CourtCaseEntity entity5 = CourtCaseEntity.builder().courtRoom("3").sessionStartTime(aftSession).defendantName("Mrs Juliette BINOCHE").build();

        // Add in reverse order
        final LocalDateTime createdAfter = LocalDateTime.now().minus(1, ChronoUnit.DAYS);
        when(courtCaseService.filterCasesByCourtAndDate(COURT_CODE, DATE, createdAfter, CREATED_BEFORE)).thenReturn(List.of(entity5, entity4, entity3, entity2, entity1));
        CaseListResponse caseList = controller.getCaseList(COURT_CODE, DATE, createdAfter, CREATED_BEFORE);

        List<CourtCaseResponse> cases = caseList.getCases();
        assertThat(cases).hasSize(5);

        assertPosition(0, cases, "1", "Mr Nicholas CAGE", mornSession);
        assertPosition(1, cases, "1", "Mr Christopher PLUMMER", mornSession);
        assertPosition(2, cases, "1", "Mr Darren ARONOFSKY", aftSession);
        assertPosition(3, cases, "3", "Mrs Minnie DRIVER", mornSession);
        assertPosition(4, cases, "3", "Mrs Juliette BINOCHE", aftSession);
    }

    @Test
    public void whenCreatedAfterIsNull_thenDefaultToUnixEpoch() {
        CourtCaseController controller = new CourtCaseController(courtCaseService, offenderMatchService);
        final LocalDateTime createdAfter = LocalDateTime.of(LocalDate.EPOCH, LocalTime.MIDNIGHT);
        controller.getCaseList(COURT_CODE, DATE, null, CREATED_BEFORE);

        verify(courtCaseService).filterCasesByCourtAndDate(COURT_CODE, DATE, createdAfter, CREATED_BEFORE);
    }

    @Test
    public void whenCreatedBeforeIsNull_thenDefaultToMaxDate() {
        CourtCaseController controller = new CourtCaseController(courtCaseService, offenderMatchService);
        final LocalDateTime createdBefore = LocalDateTime.of(294276, 12, 31, 23, 59);
        controller.getCaseList(COURT_CODE, DATE, CREATED_AFTER, null);

        verify(courtCaseService).filterCasesByCourtAndDate(COURT_CODE, DATE, CREATED_AFTER, createdBefore);
    }

    private void assertPosition(int position, List<CourtCaseResponse> cases, String courtRoom, String defendantName, LocalDateTime sessionTime) {
        assertThat(cases.get(position).getCourtRoom()).isEqualTo(courtRoom);
        assertThat(cases.get(position).getDefendantName()).isEqualTo(defendantName);
        assertThat(cases.get(position).getSessionStartTime()).isEqualTo(sessionTime);
    }
}
