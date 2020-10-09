package uk.gov.justice.probation.courtcaseservice.controller;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import uk.gov.justice.probation.courtcaseservice.controller.mapper.CourtCaseResponseMapper;
import uk.gov.justice.probation.courtcaseservice.controller.model.CaseListResponse;
import uk.gov.justice.probation.courtcaseservice.controller.model.CourtCaseRequest;
import uk.gov.justice.probation.courtcaseservice.controller.model.CourtCaseResponse;
import uk.gov.justice.probation.courtcaseservice.jpa.entity.CourtCaseEntity;
import uk.gov.justice.probation.courtcaseservice.jpa.entity.GroupedOffenderMatchesEntity;
import uk.gov.justice.probation.courtcaseservice.service.CourtCaseService;
import uk.gov.justice.probation.courtcaseservice.service.OffenderMatchService;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
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

    @Mock
    private CourtCaseService courtCaseService;
    @Mock
    private CourtCaseResponseMapper courtCaseResponseMapper;
    @Mock
    private CourtCaseResponse courtCaseResponse;
    @Mock
    private CourtCaseRequest courtCaseUpdate;
    @Mock
    private OffenderMatchService offenderMatchService;
    @Mock
    private GroupedOffenderMatchesEntity groupedOffenderMatchesEntity;
    @InjectMocks
    private CourtCaseController courtCaseController;
    private final CourtCaseEntity courtCaseEntity = CourtCaseEntity.builder().caseNo(CASE_NO).courtCode(COURT_CODE).build();

    @Test
    public void getCourtCase_shouldReturnCourtCaseResponse() {
        when(courtCaseResponseMapper.mapFrom(courtCaseEntity, null)).thenReturn(courtCaseResponse);
        when(courtCaseService.getCaseByCaseNumber(COURT_CODE, CASE_NO)).thenReturn(courtCaseEntity);
        CourtCaseResponse courtCase = courtCaseController.getCourtCase(COURT_CODE, CASE_NO);
        assertThat(courtCase).isEqualTo(courtCaseResponse);
    }

    @Test
    public void updateCaseByCourtAndCaseNo_shouldReturnCourtCaseResponse() {
        when(courtCaseResponseMapper.mapFrom(courtCaseEntity, null)).thenReturn(courtCaseResponse);
        when(courtCaseUpdate.asEntity()).thenReturn(courtCaseEntity);
        when(courtCaseService.createCase(COURT_CODE, CASE_NO, courtCaseEntity)).thenReturn(courtCaseEntity);
        CourtCaseResponse courtCase = courtCaseController.updateCourtCaseNo(COURT_CODE, CASE_NO, courtCaseUpdate);
        assertThat(courtCase).isSameAs(courtCaseResponse);
    }

    @Test
    public void getCaseList_shouldReturnCourtCaseResponse() {
        when(courtCaseResponseMapper.mapFrom(courtCaseEntity, null)).thenReturn(courtCaseResponse);
        when(courtCaseService.filterCasesByCourtAndDate(COURT_CODE, DATE, CREATED_AFTER)).thenReturn(Collections.singletonList(courtCaseEntity));
        CaseListResponse caseList = courtCaseController.getCaseList(COURT_CODE, DATE, CREATED_AFTER);
        assertThat(caseList.getCases().get(0)).isEqualTo(courtCaseResponse);
    }


    @Test
    public void getCaseList_sorted() {
        CourtCaseController controller = new CourtCaseController(courtCaseService, new CourtCaseResponseMapper(), offenderMatchService);

        LocalDateTime mornSession = LocalDateTime.of(DATE, LocalTime.of(9, 30));
        LocalDateTime aftSession = LocalDateTime.of(DATE, LocalTime.of(14, 0));
        CourtCaseEntity entity1 = CourtCaseEntity.builder().courtRoom("1").sessionStartTime(mornSession).defendantName("Mr Nicholas CAGE").build();
        CourtCaseEntity entity2 = CourtCaseEntity.builder().courtRoom("1").sessionStartTime(mornSession).defendantName("Mr Christopher PLUMMER").build();
        CourtCaseEntity entity3 = CourtCaseEntity.builder().courtRoom("1").sessionStartTime(aftSession).defendantName("Mr Darren ARONOFSKY").build();
        CourtCaseEntity entity4 = CourtCaseEntity.builder().courtRoom("3").sessionStartTime(mornSession).defendantName("Mrs Minnie DRIVER").build();
        CourtCaseEntity entity5 = CourtCaseEntity.builder().courtRoom("3").sessionStartTime(aftSession).defendantName("Mrs Juliette BINOCHE").build();

        // Add in reverse order
        final LocalDateTime createdAfter = LocalDateTime.now().minus(1, ChronoUnit.DAYS);
        when(courtCaseService.filterCasesByCourtAndDate(COURT_CODE, DATE, createdAfter)).thenReturn(List.of(entity5, entity4, entity3, entity2, entity1));
        CaseListResponse caseList = controller.getCaseList(COURT_CODE, DATE, createdAfter);

        List<CourtCaseResponse> cases = caseList.getCases();
        assertThat(cases).hasSize(5);

        assertPosition(0, cases, "1", "Mr Nicholas CAGE", mornSession);
        assertPosition(1, cases, "1", "Mr Christopher PLUMMER", mornSession);
        assertPosition(2, cases, "1", "Mr Darren ARONOFSKY", aftSession);
        assertPosition(3, cases, "3", "Mrs Minnie DRIVER", mornSession);
        assertPosition(4, cases, "3", "Mrs Juliette BINOCHE", aftSession);
    }


    @Test
    public void whenCreatedAfterIsNull_thenDefaultToMidnightThisMorning() {
        CourtCaseController controller = new CourtCaseController(courtCaseService, new CourtCaseResponseMapper(), offenderMatchService);
        final LocalDateTime createdAfter = LocalDateTime.of(LocalDate.now(), LocalTime.MIDNIGHT);
        controller.getCaseList(COURT_CODE, DATE, null);

        verify(courtCaseService).filterCasesByCourtAndDate(COURT_CODE, DATE, createdAfter);
    }

    private void assertPosition(int position, List<CourtCaseResponse> cases, String courtRoom, String defendantName, LocalDateTime sessionTime) {
        assertThat(cases.get(position).getCourtRoom()).isEqualTo(courtRoom);
        assertThat(cases.get(position).getDefendantName()).isEqualTo(defendantName);
        assertThat(cases.get(position).getSessionStartTime()).isEqualTo(sessionTime);
    }
}
