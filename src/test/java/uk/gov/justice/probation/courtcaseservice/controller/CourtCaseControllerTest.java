package uk.gov.justice.probation.courtcaseservice.controller;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.HttpHeaders;
import org.springframework.web.context.request.WebRequest;
import reactor.core.publisher.Mono;
import uk.gov.justice.probation.courtcaseservice.controller.model.CourtCaseRequest;
import uk.gov.justice.probation.courtcaseservice.controller.model.CourtCaseResponse;
import uk.gov.justice.probation.courtcaseservice.jpa.entity.CourtCaseEntity;
import uk.gov.justice.probation.courtcaseservice.jpa.entity.CourtSession;
import uk.gov.justice.probation.courtcaseservice.service.CourtCaseService;
import uk.gov.justice.probation.courtcaseservice.service.OffenderMatchService;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.time.Month;
import java.time.ZoneOffset;
import java.time.format.DateTimeFormatter;
import java.time.temporal.ChronoUnit;
import java.util.Collections;
import java.util.List;
import java.util.Optional;

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
    private WebRequest webRequest;
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
        var courtCase = courtCaseController.getCourtCase(COURT_CODE, CASE_NO);
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

        Optional<LocalDateTime> lastModified = Optional.of(LocalDateTime.of(LocalDate.of(2015, Month.OCTOBER, 21), LocalTime.of(7, 28)));
        when(courtCaseService.filterCasesLastModified(COURT_CODE, DATE)).thenReturn(lastModified);
        when(courtCaseService.filterCases(COURT_CODE, DATE, CREATED_AFTER, CREATED_BEFORE)).thenReturn(Collections.singletonList(courtCaseEntity));
        var responseEntity = courtCaseController.getCaseList(COURT_CODE, DATE, CREATED_AFTER, CREATED_BEFORE, webRequest);

        assertThat(responseEntity.getBody().getCases()).hasSize(1);
        assertThat(responseEntity.getBody().getCases().get(0).getCourtCode()).isEqualTo(COURT_CODE);
        assertThat(responseEntity.getBody().getCases().get(0).getCaseNo()).isEqualTo(CASE_NO);
        assertThat(responseEntity.getBody().getCases().get(0).getSessionStartTime()).isNotNull();
        assertThat(responseEntity.getBody().getCases().get(0).getSession()).isSameAs(session);
        assertThat(responseEntity.getHeaders().getFirst(HttpHeaders.LAST_MODIFIED)).isEqualTo("Wed, 21 Oct 2015 07:28:00 GMT");
    }

    @Test
    public void getCaseList_sorted() {
        Optional<LocalDateTime> lastModified = Optional.of(LocalDateTime.of(LocalDate.of(2015, Month.OCTOBER, 21), LocalTime.of(7, 28)));
        when(courtCaseService.filterCasesLastModified(COURT_CODE, DATE)).thenReturn(lastModified);

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
        when(courtCaseService.filterCases(COURT_CODE, DATE, createdAfter, CREATED_BEFORE)).thenReturn(List.of(entity5, entity4, entity3, entity2, entity1));
        var responseEntity = controller.getCaseList(COURT_CODE, DATE, createdAfter, CREATED_BEFORE, webRequest);

        List<CourtCaseResponse> cases = responseEntity.getBody().getCases();
        assertThat(cases).hasSize(5);

        assertPosition(0, cases, "1", "Mr Nicholas CAGE", mornSession);
        assertPosition(1, cases, "1", "Mr Christopher PLUMMER", mornSession);
        assertPosition(2, cases, "1", "Mr Darren ARONOFSKY", aftSession);
        assertPosition(3, cases, "3", "Mrs Minnie DRIVER", mornSession);
        assertPosition(4, cases, "3", "Mrs Juliette BINOCHE", aftSession);
        assertThat(responseEntity.getHeaders().getFirst(HttpHeaders.LAST_MODIFIED)).isEqualTo("Wed, 21 Oct 2015 07:28:00 GMT");
    }

    @Test
    public void whenCreatedAfterIsNull_thenDefaultToTodayMinus8Days() {
        Optional<LocalDateTime> lastModified = Optional.of(LocalDateTime.of(LocalDate.of(2015, Month.OCTOBER, 21), LocalTime.of(7, 28)));
        when(courtCaseService.filterCasesLastModified(COURT_CODE, DATE)).thenReturn(lastModified);
        CourtCaseController controller = new CourtCaseController(courtCaseService, offenderMatchService);
        final LocalDateTime createdAfter = LocalDateTime.of(DATE, LocalTime.MIDNIGHT).minusDays(8);
        controller.getCaseList(COURT_CODE, DATE, null, CREATED_BEFORE, webRequest);

        verify(courtCaseService).filterCases(COURT_CODE, DATE, createdAfter, CREATED_BEFORE);
    }

    @Test
    public void whenCreatedBeforeIsNull_thenDefaultToMaxDate() {
        Optional<LocalDateTime> lastModified = Optional.of(LocalDateTime.of(LocalDate.of(2015, Month.OCTOBER, 21), LocalTime.of(7, 28)));
        when(courtCaseService.filterCasesLastModified(COURT_CODE, DATE)).thenReturn(lastModified);
        CourtCaseController controller = new CourtCaseController(courtCaseService, offenderMatchService);
        final LocalDateTime createdBefore = LocalDateTime.of(294276, 12, 31, 23, 59);
        controller.getCaseList(COURT_CODE, DATE, CREATED_AFTER, null, webRequest);

        verify(courtCaseService).filterCases(COURT_CODE, DATE, CREATED_AFTER, createdBefore);
    }

    @Test
    public void whenPageIsNotModified_thenReturn() {
        Optional<LocalDateTime> lastModified = Optional.of(LocalDateTime.of(LocalDate.of(2015, Month.OCTOBER, 21), LocalTime.of(7, 28)));
        when(courtCaseService.filterCasesLastModified(COURT_CODE, DATE)).thenReturn(lastModified);
        CourtCaseController controller = new CourtCaseController(courtCaseService, offenderMatchService);
        when(webRequest.checkNotModified(lastModified.get().toInstant(ZoneOffset.UTC).toEpochMilli())).thenReturn(true);

        var responseEntity = controller.getCaseList(COURT_CODE, DATE, CREATED_AFTER, null, webRequest);

        assertThat(responseEntity.getStatusCode().value()).isEqualTo(304);
    }

    private void assertPosition(int position, List<CourtCaseResponse> cases, String courtRoom, String defendantName, LocalDateTime sessionTime) {
        assertThat(cases.get(position).getCourtRoom()).isEqualTo(courtRoom);
        assertThat(cases.get(position).getDefendantName()).isEqualTo(defendantName);
        assertThat(cases.get(position).getSessionStartTime()).isEqualTo(sessionTime);
    }
}
