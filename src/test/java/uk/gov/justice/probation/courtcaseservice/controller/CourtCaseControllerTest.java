package uk.gov.justice.probation.courtcaseservice.controller;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.HttpHeaders;
import org.springframework.web.context.request.WebRequest;
import reactor.core.publisher.Mono;
import uk.gov.justice.probation.courtcaseservice.controller.exceptions.ConflictingInputException;
import uk.gov.justice.probation.courtcaseservice.controller.model.CaseCommentRequest;
import uk.gov.justice.probation.courtcaseservice.controller.model.CaseCommentResponse;
import uk.gov.justice.probation.courtcaseservice.controller.model.CourtCaseHistory;
import uk.gov.justice.probation.courtcaseservice.controller.model.CourtCaseRequest;
import uk.gov.justice.probation.courtcaseservice.controller.model.CourtCaseResponse;
import uk.gov.justice.probation.courtcaseservice.controller.model.DefendantOffender;
import uk.gov.justice.probation.courtcaseservice.jpa.entity.CaseCommentEntity;
import uk.gov.justice.probation.courtcaseservice.jpa.entity.CourtCaseEntity;
import uk.gov.justice.probation.courtcaseservice.jpa.entity.CourtSession;
import uk.gov.justice.probation.courtcaseservice.jpa.entity.DefendantEntity;
import uk.gov.justice.probation.courtcaseservice.jpa.entity.EntityHelper;
import uk.gov.justice.probation.courtcaseservice.jpa.entity.HearingDefendantEntity;
import uk.gov.justice.probation.courtcaseservice.jpa.entity.HearingEntity;
import uk.gov.justice.probation.courtcaseservice.jpa.entity.NamePropertiesEntity;
import uk.gov.justice.probation.courtcaseservice.jpa.entity.OffenderEntity;
import uk.gov.justice.probation.courtcaseservice.service.CaseCommentsService;
import uk.gov.justice.probation.courtcaseservice.service.CourtCaseHistoryService;
import uk.gov.justice.probation.courtcaseservice.service.CourtCaseService;
import uk.gov.justice.probation.courtcaseservice.service.OffenderMatchService;
import uk.gov.justice.probation.courtcaseservice.service.OffenderUpdateService;
import uk.gov.justice.probation.courtcaseservice.service.exceptions.EntityNotFoundException;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.time.Month;
import java.time.ZoneOffset;
import java.time.temporal.ChronoUnit;
import java.util.Collections;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatExceptionOfType;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoMoreInteractions;
import static org.mockito.Mockito.when;
import static uk.gov.justice.probation.courtcaseservice.jpa.entity.EntityHelper.CASE_ID;
import static uk.gov.justice.probation.courtcaseservice.jpa.entity.EntityHelper.CRN;
import static uk.gov.justice.probation.courtcaseservice.jpa.entity.EntityHelper.DEFENDANT_ID;
import static uk.gov.justice.probation.courtcaseservice.jpa.entity.SourceType.COMMON_PLATFORM;

@ExtendWith(MockitoExtension.class)
class CourtCaseControllerTest {

    private static final String COURT_CODE = "COURT_CODE";
    private static final String CASE_NO = "CASE_NO";
    private static final String HEARING_ID = "HEARING_ID";

    private static final LocalDate DATE = LocalDate.of(2020, 2, 24);
    private static final LocalDateTime CREATED_AFTER = LocalDateTime.of(2020, 2, 23, 0, 0);
    private static final LocalDateTime CREATED_BEFORE = LocalDateTime.of(2020, 3, 23, 0, 0);
    @Mock
    private WebRequest webRequest;
    @Mock
    private CourtCaseService courtCaseService;
    @Mock
    private CourtCaseRequest courtCaseUpdate;
    @Mock
    private OffenderMatchService offenderMatchService;
    @Mock
    private OffenderUpdateService offenderUpdateService;
    @Mock
    private CourtCaseHistoryService courtCaseHistoryService;
    @Mock
    private CaseCommentsService caseCommentsService;

    private CourtCaseController courtCaseController;
    private final HearingEntity hearingEntity = HearingEntity.builder()
            .hearingId(HEARING_ID)
            .courtCase(CourtCaseEntity.builder()
                    .caseNo(CASE_NO)
                    .caseId(CASE_ID)
                    .sourceType(COMMON_PLATFORM)
                    .build())
            .hearingDays(Collections.singletonList(EntityHelper.aHearingDayEntity()
                    .withCourtCode(COURT_CODE)))
            .hearingDefendants(Collections.singletonList(
                    HearingDefendantEntity.builder()
                            .defendant(DefendantEntity.builder()
                                    .defendantId(DEFENDANT_ID)
                                    .build())
                            .build()
            ))

            .build();

    private final CourtSession session = CourtSession.MORNING;

    @BeforeEach
    public void setUp() {
        courtCaseController = new CourtCaseController(courtCaseService, courtCaseHistoryService, offenderMatchService, offenderUpdateService, caseCommentsService, true);
    }

    @Test
    void getCourtCase_shouldReturnCourtCaseResponse() {
        when(courtCaseService.getHearingByCaseNumber(COURT_CODE, CASE_NO)).thenReturn(hearingEntity);
        when(offenderMatchService.getMatchCountByCaseIdAndDefendant(CASE_ID, DEFENDANT_ID)).thenReturn(Optional.of(3));
        var courtCase = courtCaseController.getCourtCase(COURT_CODE, CASE_NO);
        assertThat(courtCase.getCourtCode()).isEqualTo(COURT_CODE);
        assertThat(courtCase.getCaseNo()).isNull();
        assertThat(courtCase.getSource()).isEqualTo("COMMON_PLATFORM");
        assertThat(courtCase.getSessionStartTime()).isNotNull();
        assertThat(courtCase.getSession()).isSameAs(session);
        assertThat(courtCase.getNumberOfPossibleMatches()).isEqualTo(3);

        verify(courtCaseService).getHearingByCaseNumber(COURT_CODE, CASE_NO);
        verify(offenderMatchService).getMatchCountByCaseIdAndDefendant(CASE_ID, DEFENDANT_ID);
        verifyNoMoreInteractions(courtCaseService, offenderMatchService);
    }

    @Test
    void givenNoDefendants_whenGetCourtCase_thenShouldThrowExceptionWithCaseId() {
        when(courtCaseService.getHearingByCaseNumber(COURT_CODE, CASE_NO)).thenReturn(hearingEntity.withHearingDefendants(Collections.emptyList()));

        assertThatExceptionOfType(IllegalStateException.class)
                .isThrownBy(() -> courtCaseController.getCourtCase(COURT_CODE, CASE_NO))
                .withMessageContaining(hearingEntity.getCaseId());

        verifyNoMoreInteractions(courtCaseService, offenderMatchService);
    }

    @Test
    void getCourtCaseByHearingIdAndDefendantId_shouldReturnCourtCaseResponseNoCaseNo() {

        when(offenderMatchService.getMatchCountByCaseIdAndDefendant(CASE_ID, DEFENDANT_ID)).thenReturn(Optional.of(2));
        when(courtCaseService.getHearingByHearingIdAndDefendantId(CASE_ID, DEFENDANT_ID)).thenReturn(hearingEntity);

        var courtCase = courtCaseController.getCourtCaseByHearingIdAndDefendantId(CASE_ID, DEFENDANT_ID);
        assertThat(courtCase.getCourtCode()).isEqualTo(COURT_CODE);
        assertThat(courtCase.getCaseNo()).isNull();
        assertThat(courtCase.getSessionStartTime()).isNotNull();
        assertThat(courtCase.getNumberOfPossibleMatches()).isEqualTo(2);
        assertThat(courtCase.getSession()).isSameAs(session);

        verify(courtCaseService).getHearingByHearingIdAndDefendantId(CASE_ID, DEFENDANT_ID);
        verify(offenderMatchService).getMatchCountByCaseIdAndDefendant(CASE_ID, DEFENDANT_ID);
        verifyNoMoreInteractions(courtCaseService, offenderMatchService);
    }

    @Test
    void getCaseList_shouldReturnCourtCaseResponse() {

        var lastModified = Optional.of(LocalDateTime.of(LocalDate.of(2015, Month.OCTOBER, 21), LocalTime.of(7, 28)));
        when(courtCaseService.filterHearingsLastModified(COURT_CODE, DATE)).thenReturn(lastModified);

        final var courtCaseEntity = this.hearingEntity.withHearingDefendants(List.of(EntityHelper.aHearingDefendantEntity()))
                .withHearingDays(Collections.singletonList(EntityHelper.aHearingDayEntity()
                        .withDay(DATE)
                        .withTime(LocalTime.of(9, 0))
                        .withCourtCode(COURT_CODE)
                ));
        when(courtCaseService.filterHearings(COURT_CODE, DATE, CREATED_AFTER, CREATED_BEFORE))
                .thenReturn(Collections.singletonList(courtCaseEntity));
        var responseEntity = courtCaseController.getCaseList(COURT_CODE, DATE, CREATED_AFTER, CREATED_BEFORE, webRequest);

        assertThat(responseEntity.getBody().getCases()).hasSize(1);
        assertCourtCase(responseEntity.getBody().getCases().get(0), null, 0);
        assertThat(responseEntity.getHeaders().getFirst(HttpHeaders.LAST_MODIFIED)).isEqualTo("Wed, 21 Oct 2015 07:28:00 GMT");
    }

    @Test
    void givenSingleCaseWithMultipleDefendants_whenGetCaseList_shouldReturnMultipleCourtCaseResponse() {

        var defendantEntity1 = EntityHelper.aHearingDefendantEntity();
        var defendantEntity2 = EntityHelper.aHearingDefendantEntity(NamePropertiesEntity.builder().title("HRH").forename1("Catherine").forename2("The").surname("GREAT").build());
        var courtCaseEntity = HearingEntity.builder()
                .courtCase(CourtCaseEntity.builder()
                        .caseNo(CASE_NO)
                        .sourceType(COMMON_PLATFORM)
                        .build())
                .hearingDefendants(List.of(defendantEntity1, defendantEntity2))
                .hearingDays(Collections.singletonList(EntityHelper.aHearingDayEntity()
                        .withCourtCode(COURT_CODE)
                        .withDay(DATE)
                        .withTime(LocalTime.of(9, 0))))
                .build();

        var lastModified = Optional.of(LocalDateTime.of(LocalDate.of(2015, Month.OCTOBER, 21), LocalTime.of(7, 28)));
        when(courtCaseService.filterHearingsLastModified(COURT_CODE, DATE)).thenReturn(lastModified);
        when(courtCaseService.filterHearings(COURT_CODE, DATE, CREATED_AFTER, CREATED_BEFORE)).thenReturn(Collections.singletonList(courtCaseEntity));

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
        when(courtCaseService.filterHearingsLastModified(COURT_CODE, DATE)).thenReturn(lastModified);

        final var mornSession = LocalDateTime.of(DATE, LocalTime.of(9, 30));
        final var aftSession = LocalDateTime.of(DATE, LocalTime.of(14, 0));

        final var nicCage = NamePropertiesEntity.builder()
                .title("Mr")
                .forename1("Nicholas")
                .surname("Cage")
                .build();
        final var entity1 = buildCourtCaseEntity(nicCage, mornSession, "1");
        final var chrisPlummer = NamePropertiesEntity.builder()
                .title("Mr")
                .forename1("Christopher")
                .surname("PLUMMER")
                .build();
        final var entity2 = buildCourtCaseEntity(chrisPlummer, mornSession, "1");
        final var dazAronofsky = NamePropertiesEntity.builder()
                .title("Mr")
                .forename1("Darren")
                .surname("ARONOFSKY")
                .build();
        final var entity3 = buildCourtCaseEntity(dazAronofsky, aftSession, "1");
        final var minnieDriver = NamePropertiesEntity.builder()
                .title("Mrs")
                .forename1("Minnie")
                .surname("DRIVER")
                .build();
        final var entity4 = buildCourtCaseEntity(minnieDriver, mornSession, "3");
        final var julesBinoche = NamePropertiesEntity.builder()
                .title("Mr")
                .forename1("Juliette")
                .surname("BINOCHE")
                .build();
        final var entity5 = buildCourtCaseEntity(julesBinoche, aftSession, "3");

        // Add in reverse order
        final var createdAfter = LocalDateTime.now().minus(1, ChronoUnit.DAYS);
        when(courtCaseService.filterHearings(COURT_CODE, DATE, createdAfter, CREATED_BEFORE)).thenReturn(List.of(entity5, entity4, entity3, entity2, entity1));
        var responseEntity = courtCaseController.getCaseList(COURT_CODE, DATE, createdAfter, CREATED_BEFORE, webRequest);

        final var cases = responseEntity.getBody().getCases();
        assertThat(cases).hasSize(5);

        assertPosition(0, cases, "1", nicCage, mornSession);
        assertPosition(1, cases, "1", chrisPlummer, mornSession);
        assertPosition(2, cases, "1", dazAronofsky, aftSession);
        assertPosition(3, cases, "3", minnieDriver, mornSession);
        assertPosition(4, cases, "3", julesBinoche, aftSession);
        assertThat(responseEntity.getHeaders().getFirst(HttpHeaders.LAST_MODIFIED)).isEqualTo("Wed, 21 Oct 2015 07:28:00 GMT");
    }

    @Test
    void whenCreatedAfterIsNull_thenDefaultToALongTimeAgo() {
        final var lastModified = Optional.of(LocalDateTime.of(LocalDate.of(2015, Month.OCTOBER, 21), LocalTime.of(7, 28)));
        when(courtCaseService.filterHearingsLastModified(COURT_CODE, DATE)).thenReturn(lastModified);
        final LocalDateTime createdAfter = LocalDateTime.of(-4712, 1, 1, 0, 0);
        courtCaseController.getCaseList(COURT_CODE, DATE, null, CREATED_BEFORE, webRequest);

        verify(courtCaseService).filterHearings(COURT_CODE, DATE, createdAfter, CREATED_BEFORE);
    }

    @Test
    void whenCreatedBeforeIsNull_thenDefaultToMaxDate() {
        final var lastModified = Optional.of(LocalDateTime.of(LocalDate.of(2015, Month.OCTOBER, 21), LocalTime.of(7, 28)));
        when(courtCaseService.filterHearingsLastModified(COURT_CODE, DATE)).thenReturn(lastModified);
        final LocalDateTime createdBefore = LocalDateTime.of(294276, 12, 31, 23, 59);
        courtCaseController.getCaseList(COURT_CODE, DATE, CREATED_AFTER, null, webRequest);

        verify(courtCaseService).filterHearings(COURT_CODE, DATE, CREATED_AFTER, createdBefore);
    }

    @Test
    void whenListIsNotModified_thenReturn() {
        final var lastModified = Optional.of(LocalDateTime.of(LocalDate.of(2015, Month.OCTOBER, 21), LocalTime.of(7, 28)));
        when(courtCaseService.filterHearingsLastModified(COURT_CODE, DATE)).thenReturn(lastModified);
        when(webRequest.checkNotModified(lastModified.get().toInstant(ZoneOffset.UTC).toEpochMilli())).thenReturn(true);

        var responseEntity = courtCaseController.getCaseList(COURT_CODE, DATE, CREATED_AFTER, null, webRequest);

        assertThat(responseEntity.getStatusCode().value()).isEqualTo(304);
        assertThat(responseEntity.getHeaders().get("Cache-Control").get(0)).isEqualTo("max-age=1");
    }

    @Test
    void givenCacheableCaseListDisabled_whenListIsNotModified_thenReturnFullList() {
        final var nonCachingController = new CourtCaseController(courtCaseService, courtCaseHistoryService,
            offenderMatchService, offenderUpdateService, caseCommentsService, false);

        final var courtCaseEntity = this.hearingEntity.withHearingDefendants(List.of(EntityHelper.aHearingDefendantEntity()))
                .withHearingDays(Collections.singletonList(EntityHelper.aHearingDayEntity()
                        .withDay(DATE)
                        .withTime(LocalTime.of(9, 0))
                        .withCourtCode(COURT_CODE)
                ));
        when(courtCaseService.filterHearings(COURT_CODE, DATE, CREATED_AFTER, CREATED_BEFORE))
                .thenReturn(Collections.singletonList(courtCaseEntity));
        var responseEntity = nonCachingController.getCaseList(COURT_CODE, DATE, CREATED_AFTER, CREATED_BEFORE, webRequest);

        assertThat(responseEntity.getBody().getCases()).hasSize(1);
        assertCourtCase(responseEntity.getBody().getCases().get(0), null, 0);
        assertThat(responseEntity.getHeaders().getFirst(HttpHeaders.LAST_MODIFIED)).isEqualTo(null);
    }

    @Test
    void whenListHasNeverBeenModified_thenReturnNeverModifiedDate() {
        when(courtCaseService.filterHearingsLastModified(COURT_CODE, DATE)).thenReturn(Optional.empty());
        when(webRequest.checkNotModified(any(Long.class))).thenReturn(false);

        var responseEntity = courtCaseController.getCaseList(COURT_CODE, DATE, CREATED_AFTER, null, webRequest);

        assertThat(responseEntity.getHeaders().get("Last-Modified").get(0)).isEqualTo("Wed, 01 Jan 2020 00:00:00 GMT");
        assertThat(responseEntity.getHeaders().get("Cache-Control").get(0)).isEqualTo("max-age=1");
    }

    @Test
    void whenGetOffenderByDefendantId_shouldReturnOffender() {
        OffenderEntity offenderEntity = OffenderEntity.builder().crn(CRN).build();
        when(offenderUpdateService.getDefendantOffenderByDefendantId(DEFENDANT_ID)).thenReturn(Mono.just(offenderEntity));
        var actual = courtCaseController.getOffenderByDefendantId(DEFENDANT_ID).block();
        verify(offenderUpdateService).getDefendantOffenderByDefendantId(DEFENDANT_ID);
        assertThat(actual).isEqualTo(DefendantOffender.builder().crn(CRN).suspendedSentenceOrder(false)
            .breach(false).preSentenceActivity(false).build());
    }

    @Test
    void whenDeleteOffenderByDefendantId_shouldInvokeDeleteOffender() {
        courtCaseController.deleteOffender(DEFENDANT_ID);
        verify(offenderUpdateService).removeDefendantOffenderAssociation(DEFENDANT_ID);
    }

    @Test
    void whenUpdateOffenderByDefendantId_shouldInvokeUpdateOffender() {
        DefendantOffender testDefendant = DefendantOffender.builder().crn(CRN).build();
        when(offenderUpdateService.updateDefendantOffender(DEFENDANT_ID, testDefendant.asEntity())).
            thenReturn(Mono.just(OffenderEntity.builder().crn(CRN).build()));
        final var actual = courtCaseController.updateOffenderByDefendantId(DEFENDANT_ID, testDefendant).block();
        verify(offenderUpdateService).updateDefendantOffender(DEFENDANT_ID, testDefendant.asEntity());
        assertThat(actual).isEqualTo(DefendantOffender.builder().crn(CRN).suspendedSentenceOrder(false)
            .breach(false).preSentenceActivity(false).build());
    }

    @Test
    void whenCreateComment_shouldInvokeCaseCommentsService() {
        var caseId = "case-id-one";
        var caseCommentEntity = CaseCommentEntity.builder().comment("comment one").author("Author One").caseId(caseId).build();
        Mockito.lenient().when(caseCommentsService.createCaseComment(any(CaseCommentEntity.class))).thenReturn(caseCommentEntity.withId(3456L));
        final var actual = courtCaseController.createCaseComment(caseId,
            CaseCommentRequest.builder()
                .caseId(caseId)
                .comment("comment-one")
                .author("Test Author")
                .build());

        verify(caseCommentsService).createCaseComment(any(CaseCommentEntity.class));
        assertThat(actual).isEqualTo(
            CaseCommentResponse.builder()
                .comment("comment one")
                .caseId(caseId)
                .author("Author One")
                .commentId(3456L)
                .build()
        );
    }

    @Test
    void givenCaseIdDoesNotMatchCaseCommentRequestCaseId_whenCreateComment_shouldThrowConflictingInputException() {

        assertThrows(ConflictingInputException.class, () -> {
            courtCaseController.createCaseComment("case-id-one", CaseCommentRequest.builder().caseId("invalid-case-id").build());
        });

        Mockito.verifyNoMoreInteractions(caseCommentsService);
    }

    @Test
    void givenCaseIdShouldReturnCourtCaseHistory() {
        String caseId = "test-case-id";
        CourtCaseHistory courtCaseHistory = CourtCaseHistory.builder().build();
        given(courtCaseHistoryService.getCourtCaseHistory(caseId)).willReturn(courtCaseHistory);

        var actual = courtCaseController.getCaseHistory(caseId);
        verify(courtCaseHistoryService).getCourtCaseHistory(caseId);
        assertThat(actual).isEqualTo(courtCaseHistory);
    }

    @Test
    void givenCourtCaseHistoryServiceThrowsExceptionShouldCascadeIt() {
        String caseId = "test-case-id";
        given(courtCaseHistoryService.getCourtCaseHistory(caseId)).willThrow(new EntityNotFoundException("caseId not found"));

        assertThrows(EntityNotFoundException.class, () -> courtCaseController.getCaseHistory(caseId));
        verify(courtCaseHistoryService).getCourtCaseHistory(caseId);
    }

    private void assertPosition(int position, List<CourtCaseResponse> cases, String courtRoom, NamePropertiesEntity defendantName, LocalDateTime sessionTime) {
        assertThat(cases.get(position).getCourtRoom()).isEqualTo(courtRoom);
        assertThat(cases.get(position).getName()).isEqualTo(defendantName);
        assertThat(cases.get(position).getSessionStartTime()).isEqualTo(sessionTime);
    }

    private void assertCourtCase(CourtCaseResponse courtCase, String caseNo, int possibleMatchCount) {
        assertThat(courtCase.getCourtCode()).isEqualTo(COURT_CODE);
        assertThat(courtCase.getCaseNo()).isEqualTo(caseNo);
        assertThat(courtCase.getSessionStartTime()).isNotNull();
        assertThat(courtCase.getSession()).isSameAs(session);
        assertThat(courtCase.getSource()).isEqualTo(COMMON_PLATFORM.name());
        assertThat(courtCase.getNumberOfPossibleMatches()).isEqualTo(possibleMatchCount);
    }

    private HearingEntity buildCourtCaseEntity(NamePropertiesEntity name, LocalDateTime sessionStartTime, String courtRoom) {
        final var hearingDefendantEntity = EntityHelper.aHearingDefendant(name);
        return EntityHelper.aHearingEntity(UUID.randomUUID().toString())
                .withHearingDefendants(List.of(hearingDefendantEntity))
                .withHearingDays(List.of(EntityHelper.aHearingDayEntity(sessionStartTime).withCourtRoom(courtRoom)));
    }
}
