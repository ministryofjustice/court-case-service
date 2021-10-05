package uk.gov.justice.probation.courtcaseservice.controller;

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
import java.util.UUID;
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
import uk.gov.justice.probation.courtcaseservice.controller.model.ExtendedCourtCaseRequest;
import uk.gov.justice.probation.courtcaseservice.jpa.entity.CourtCaseEntity;
import uk.gov.justice.probation.courtcaseservice.jpa.entity.CourtSession;
import uk.gov.justice.probation.courtcaseservice.jpa.entity.EntityHelper;
import uk.gov.justice.probation.courtcaseservice.jpa.entity.NamePropertiesEntity;
import uk.gov.justice.probation.courtcaseservice.service.CourtCaseService;
import uk.gov.justice.probation.courtcaseservice.service.OffenderMatchService;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoMoreInteractions;
import static org.mockito.Mockito.when;
import static uk.gov.justice.probation.courtcaseservice.jpa.entity.EntityHelper.CASE_ID;
import static uk.gov.justice.probation.courtcaseservice.jpa.entity.EntityHelper.CRN;
import static uk.gov.justice.probation.courtcaseservice.jpa.entity.EntityHelper.DEFENDANT_ID;
import static uk.gov.justice.probation.courtcaseservice.jpa.entity.EntityHelper.aCourtCaseEntity;
import static uk.gov.justice.probation.courtcaseservice.jpa.entity.EntityHelper.aDefendantEntity;
import static uk.gov.justice.probation.courtcaseservice.jpa.entity.EntityHelper.aHearingEntity;
import static uk.gov.justice.probation.courtcaseservice.jpa.entity.SourceType.COMMON_PLATFORM;

@ExtendWith(MockitoExtension.class)
class CourtCaseControllerTest {

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
        .caseId(CASE_ID)
        .sourceType(COMMON_PLATFORM)
        .sessionStartTime(now)
        .build();

    private CourtSession session;

    @BeforeEach
    void beforeEach() {
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("a");
        session = formatter.format(now).equalsIgnoreCase("am") ? CourtSession.MORNING : CourtSession.AFTERNOON;
    }

    @Test
    void getCourtCase_shouldReturnCourtCaseResponse() {
        when(courtCaseService.getCaseByCaseNumber(COURT_CODE, CASE_NO)).thenReturn(courtCaseEntity);
        when(offenderMatchService.getMatchCount(COURT_CODE, CASE_NO)).thenReturn(Optional.of(3));
        var courtCase = courtCaseController.getCourtCase(COURT_CODE, CASE_NO);
        assertThat(courtCase.getCourtCode()).isEqualTo(COURT_CODE);
        assertThat(courtCase.getCaseNo()).isNull();
        assertThat(courtCase.getSource()).isEqualTo("COMMON_PLATFORM");
        assertThat(courtCase.getSessionStartTime()).isNotNull();
        assertThat(courtCase.getSession()).isSameAs(session);
        assertThat(courtCase.getNumberOfPossibleMatches()).isEqualTo(3);

        verify(courtCaseService).getCaseByCaseNumber(COURT_CODE, CASE_NO);
        verify(offenderMatchService).getMatchCount(COURT_CODE, CASE_NO);
        verifyNoMoreInteractions(courtCaseService, offenderMatchService);
    }

    @Test
    void getCourtCaseByCaseIdAndDefendantId_shouldReturnCourtCaseResponseNoCaseNo() {

        when(offenderMatchService.getMatchCountByCaseIdAndDefendant(CASE_ID, DEFENDANT_ID)).thenReturn(Optional.of(2));
        when(courtCaseService.getCaseByCaseIdAndDefendantId(CASE_ID, DEFENDANT_ID)).thenReturn(courtCaseEntity);

        var courtCase = courtCaseController.getCourtCaseByCaseIdAndDefendantId(CASE_ID, DEFENDANT_ID);
        assertThat(courtCase.getCourtCode()).isEqualTo(COURT_CODE);
        assertThat(courtCase.getCaseNo()).isNull();
        assertThat(courtCase.getSessionStartTime()).isNotNull();
        assertThat(courtCase.getNumberOfPossibleMatches()).isEqualTo(2);
        assertThat(courtCase.getSession()).isSameAs(session);

        verify(courtCaseService).getCaseByCaseIdAndDefendantId(CASE_ID, DEFENDANT_ID);
        verify(offenderMatchService).getMatchCountByCaseIdAndDefendant(CASE_ID, DEFENDANT_ID);
        verifyNoMoreInteractions(courtCaseService, offenderMatchService);
    }

    @Test
    void updateCaseByCourtAndCaseNo_shouldReturnCourtCaseResponse() {
        when(courtCaseUpdate.asEntity()).thenReturn(courtCaseEntity);
        when(courtCaseService.createCase(COURT_CODE, CASE_NO, courtCaseEntity)).thenReturn(Mono.just(courtCaseEntity));
        when(offenderMatchService.getMatchCount(COURT_CODE, CASE_NO)).thenReturn(Optional.empty());
        var courtCase = courtCaseController.updateCourtCaseNo(COURT_CODE, CASE_NO, courtCaseUpdate). block();

        assertCourtCase(courtCase, null, 0);
        verify(courtCaseService).createCase(COURT_CODE, CASE_NO, courtCaseEntity);
        verify(offenderMatchService).getMatchCount(COURT_CODE, CASE_NO);
        verifyNoMoreInteractions(courtCaseService, offenderMatchService);
    }

    @Test
    void getCaseList_shouldReturnCourtCaseResponse() {

        var lastModified = Optional.of(LocalDateTime.of(LocalDate.of(2015, Month.OCTOBER, 21), LocalTime.of(7, 28)));
        when(courtCaseService.filterCasesLastModified(COURT_CODE, DATE)).thenReturn(lastModified);

        when(courtCaseService.filterCases(COURT_CODE, DATE, CREATED_AFTER, CREATED_BEFORE))
            .thenReturn(Collections.singletonList(courtCaseEntity.withDefendants(List.of(aDefendantEntity()))));
        var responseEntity = courtCaseController.getCaseList(COURT_CODE, DATE, CREATED_AFTER, CREATED_BEFORE, webRequest);

        assertThat(responseEntity.getBody().getCases()).hasSize(1);
        assertCourtCase(responseEntity.getBody().getCases().get(0), null, 0);
        assertThat(responseEntity.getHeaders().getFirst(HttpHeaders.LAST_MODIFIED)).isEqualTo("Wed, 21 Oct 2015 07:28:00 GMT");
    }

    @Test
    void givenSingleCaseWithMultipleDefendants_whenGetCaseList_shouldReturnMultipleCourtCaseResponse() {

        var defendantEntity1 = EntityHelper.aDefendantEntity();
        var defendantEntity2 = EntityHelper.aDefendantEntity(NamePropertiesEntity.builder().title("HRH").forename1("Catherine").forename2("The").surname("GREAT").build());
        var courtCaseEntity = CourtCaseEntity.builder()
            .caseNo(CASE_NO)
            .courtCode(COURT_CODE)
            .sourceType(COMMON_PLATFORM)
            .sessionStartTime(now)
            .defendants(List.of(defendantEntity1, defendantEntity2))
            .build();

        var lastModified = Optional.of(LocalDateTime.of(LocalDate.of(2015, Month.OCTOBER, 21), LocalTime.of(7, 28)));
        when(courtCaseService.filterCasesLastModified(COURT_CODE, DATE)).thenReturn(lastModified);
        when(courtCaseService.filterCases(COURT_CODE, DATE, CREATED_AFTER, CREATED_BEFORE)).thenReturn(Collections.singletonList(courtCaseEntity));

        var responseEntity = courtCaseController.getCaseList(COURT_CODE, DATE, CREATED_AFTER, CREATED_BEFORE, webRequest);

        assertThat(responseEntity.getBody().getCases()).hasSize(2);
        // Top level fields for both are the same
        assertCourtCase(responseEntity.getBody().getCases().get(0), null, 0);
        assertCourtCase(responseEntity.getBody().getCases().get(1), null, 0);
        assertThat(responseEntity.getBody().getCases().get(0).getCrn()).isEqualTo(CRN);
        assertThat(responseEntity.getBody().getCases().get(0).getDefendantName()).isEqualTo("Mr Gordon BENNETT");
        assertThat(responseEntity.getBody().getCases().get(1).getDefendantName()).isEqualTo("HRH Catherine The GREAT");
        assertThat(responseEntity.getHeaders().getFirst(HttpHeaders.LAST_MODIFIED)).isEqualTo("Wed, 21 Oct 2015 07:28:00 GMT");
    }

    @Test
    void getCaseList_sorted() {
        final var lastModified = Optional.of(LocalDateTime.of(LocalDate.of(2015, Month.OCTOBER, 21), LocalTime.of(7, 28)));
        when(courtCaseService.filterCasesLastModified(COURT_CODE, DATE)).thenReturn(lastModified);

        final var controller = new CourtCaseController(courtCaseService, offenderMatchService);

        final var mornSession = LocalDateTime.of(DATE, LocalTime.of(9, 30));
        final var aftSession = LocalDateTime.of(DATE, LocalTime.of(14, 0));

        final var entity1 = buildCourtCaseEntity("Mr Nicholas CAGE", mornSession, "1");
        final var entity2 = buildCourtCaseEntity("Mr Christopher PLUMMER", mornSession, "1");
        final var entity3 = buildCourtCaseEntity("Mr Darren ARONOFSKY", aftSession, "1");
        final var entity4 = buildCourtCaseEntity("Mrs Minnie DRIVER", mornSession, "3");
        final var entity5 = buildCourtCaseEntity("Mrs Juliette BINOCHE", aftSession, "3");

        // Add in reverse order
        final var createdAfter = LocalDateTime.now().minus(1, ChronoUnit.DAYS);
        when(courtCaseService.filterCases(COURT_CODE, DATE, createdAfter, CREATED_BEFORE)).thenReturn(List.of(entity5, entity4, entity3, entity2, entity1));
        var responseEntity = controller.getCaseList(COURT_CODE, DATE, createdAfter, CREATED_BEFORE, webRequest);

        final var cases = responseEntity.getBody().getCases();
        assertThat(cases).hasSize(5);

        assertPosition(0, cases, "1", "Mr Nicholas CAGE", mornSession);
        assertPosition(1, cases, "1", "Mr Christopher PLUMMER", mornSession);
        assertPosition(2, cases, "1", "Mr Darren ARONOFSKY", aftSession);
        assertPosition(3, cases, "3", "Mrs Minnie DRIVER", mornSession);
        assertPosition(4, cases, "3", "Mrs Juliette BINOCHE", aftSession);
        assertThat(responseEntity.getHeaders().getFirst(HttpHeaders.LAST_MODIFIED)).isEqualTo("Wed, 21 Oct 2015 07:28:00 GMT");
    }

    @Test
    void whenCreatedAfterIsNull_thenDefaultToTodayMinus8Days() {
        final var lastModified = Optional.of(LocalDateTime.of(LocalDate.of(2015, Month.OCTOBER, 21), LocalTime.of(7, 28)));
        when(courtCaseService.filterCasesLastModified(COURT_CODE, DATE)).thenReturn(lastModified);
        final var controller = new CourtCaseController(courtCaseService, offenderMatchService);
        final LocalDateTime createdAfter = LocalDateTime.of(DATE, LocalTime.MIDNIGHT).minusDays(8);
        controller.getCaseList(COURT_CODE, DATE, null, CREATED_BEFORE, webRequest);

        verify(courtCaseService).filterCases(COURT_CODE, DATE, createdAfter, CREATED_BEFORE);
    }

    @Test
    void whenCreatedBeforeIsNull_thenDefaultToMaxDate() {
        final var lastModified = Optional.of(LocalDateTime.of(LocalDate.of(2015, Month.OCTOBER, 21), LocalTime.of(7, 28)));
        when(courtCaseService.filterCasesLastModified(COURT_CODE, DATE)).thenReturn(lastModified);
        final var controller = new CourtCaseController(courtCaseService, offenderMatchService);
        final LocalDateTime createdBefore = LocalDateTime.of(294276, 12, 31, 23, 59);
        controller.getCaseList(COURT_CODE, DATE, CREATED_AFTER, null, webRequest);

        verify(courtCaseService).filterCases(COURT_CODE, DATE, CREATED_AFTER, createdBefore);
    }

    @Test
    void whenListIsNotModified_thenReturn() {
        final var lastModified = Optional.of(LocalDateTime.of(LocalDate.of(2015, Month.OCTOBER, 21), LocalTime.of(7, 28)));
        when(courtCaseService.filterCasesLastModified(COURT_CODE, DATE)).thenReturn(lastModified);
        final var controller = new CourtCaseController(courtCaseService, offenderMatchService);
        when(webRequest.checkNotModified(lastModified.get().toInstant(ZoneOffset.UTC).toEpochMilli())).thenReturn(true);

        var responseEntity = controller.getCaseList(COURT_CODE, DATE, CREATED_AFTER, null, webRequest);

        assertThat(responseEntity.getStatusCode().value()).isEqualTo(304);
        assertThat(responseEntity.getHeaders().get("Cache-Control").get(0)).isEqualTo("max-age=1");
    }

    @Test
    void whenListHasNeverBeenModified_thenReturnNeverModifiedDate() {
        when(courtCaseService.filterCasesLastModified(COURT_CODE, DATE)).thenReturn(Optional.empty());
        when(webRequest.checkNotModified(any(Long.class))).thenReturn(false);
        final var controller = new CourtCaseController(courtCaseService, offenderMatchService);

        var responseEntity = controller.getCaseList(COURT_CODE, DATE, CREATED_AFTER, null, webRequest);

        assertThat(responseEntity.getHeaders().get("Last-Modified").get(0)).isEqualTo("Wed, 01 Jan 2020 00:00:00 GMT");
        assertThat(responseEntity.getHeaders().get("Cache-Control").get(0)).isEqualTo("max-age=1");
    }

    @Test
    void whenUpdateCaseByCaseIdAndDefendantId_shouldReturnCourtCaseResponse() {
        when(courtCaseUpdate.asEntity()).thenReturn(courtCaseEntity);
        when(courtCaseService.createUpdateCaseForSingleDefendantId(CASE_ID, DEFENDANT_ID, courtCaseEntity)).thenReturn(Mono.just(courtCaseEntity));
        when(offenderMatchService.getMatchCountByCaseIdAndDefendant(CASE_ID, DEFENDANT_ID)).thenReturn(Optional.of(3));

        var courtCase = courtCaseController.updateCourtCaseByDefendantId(CASE_ID, DEFENDANT_ID, courtCaseUpdate). block();

        assertCourtCase(courtCase, null, 3);
        verify(courtCaseService).createUpdateCaseForSingleDefendantId(CASE_ID, DEFENDANT_ID, courtCaseEntity);
        verify(offenderMatchService).getMatchCountByCaseIdAndDefendant(CASE_ID, DEFENDANT_ID);
        verifyNoMoreInteractions(courtCaseService, offenderMatchService);
    }

    @Test
    void whenUpdateWholeCaseByCaseId_shouldReturnCourtCaseResponse() {
        var courtCaseRequest = mock(ExtendedCourtCaseRequest.class);
        when(courtCaseRequest.asCourtCaseEntity()).thenReturn(courtCaseEntity);
        when(courtCaseService.createCase(CASE_ID, courtCaseEntity)).thenReturn(Mono.just(courtCaseEntity));

        var courtCase = courtCaseController.updateCourtCaseId(CASE_ID, courtCaseRequest). block();

        assertThat(courtCase).isSameAs(courtCaseRequest);
        verify(courtCaseRequest).asCourtCaseEntity();
        verify(courtCaseService).createCase(CASE_ID, courtCaseEntity);
        verifyNoMoreInteractions(courtCaseService, offenderMatchService);
    }

    private void assertPosition(int position, List<CourtCaseResponse> cases, String courtRoom, String defendantName, LocalDateTime sessionTime) {
        assertThat(cases.get(position).getCourtRoom()).isEqualTo(courtRoom);
        assertThat(cases.get(position).getDefendantName()).isEqualTo(defendantName);
        assertThat(cases.get(position).getSessionStartTime()).isEqualTo(sessionTime);
    }

    private void assertCourtCase(CourtCaseResponse courtCase) {
        assertCourtCase(courtCase, CASE_NO, 0);
    }

    private void assertCourtCase(CourtCaseResponse courtCase, String caseNo, int possibleMatchCount) {
        assertThat(courtCase.getCourtCode()).isEqualTo(COURT_CODE);
        assertThat(courtCase.getCaseNo()).isEqualTo(caseNo);
        assertThat(courtCase.getSessionStartTime()).isNotNull();
        assertThat(courtCase.getSession()).isSameAs(session);
        assertThat(courtCase.getSource()).isEqualTo(COMMON_PLATFORM.name());
        assertThat(courtCase.getNumberOfPossibleMatches()).isEqualTo(possibleMatchCount);
    }

    private CourtCaseEntity buildCourtCaseEntity(String name, LocalDateTime sessionStartTime, String courtRoom) {

        // TODO - we have to build like this because the sorting relies on the fields in CourtCaseEntity
        return aCourtCaseEntity(UUID.randomUUID().toString())
            .withCourtRoom(courtRoom)
            .withDefendantName(name)
            .withSessionStartTime(sessionStartTime)
            .withDefendants(List.of(aDefendantEntity(UUID.randomUUID().toString()).withDefendantName(name)))
            .withHearings(List.of(aHearingEntity(sessionStartTime).withCourtRoom(courtRoom)));

    }
}
