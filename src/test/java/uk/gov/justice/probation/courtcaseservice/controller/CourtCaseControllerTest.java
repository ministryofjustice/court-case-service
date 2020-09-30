package uk.gov.justice.probation.courtcaseservice.controller;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import uk.gov.justice.probation.courtcaseservice.controller.mapper.CourtCaseResponseMapper;
import uk.gov.justice.probation.courtcaseservice.controller.model.CaseListResponse;
import uk.gov.justice.probation.courtcaseservice.controller.model.CourtCaseResponse;
import uk.gov.justice.probation.courtcaseservice.jpa.entity.CourtCaseEntity;
import uk.gov.justice.probation.courtcaseservice.service.MutableCourtCaseService;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
public class CourtCaseControllerTest {

    private static final String COURT_CODE = "COURT_CODE";
    private static final String CASE_NO = "CASE_NO";
    private static final LocalDate DATE = LocalDate.of(2020, 2, 24);

    @Mock
    private MutableCourtCaseService courtCaseService;
    @Mock
    private CourtCaseResponseMapper courtCaseResponseMapper;
    @Mock
    private CourtCaseResponse courtCaseResponse;
    @InjectMocks
    private CourtCaseController courtCaseController;
    private final CourtCaseEntity courtCaseEntity = CourtCaseEntity.builder().caseNo(CASE_NO).courtCode(COURT_CODE).build();
    private final CourtCaseEntity courtCaseUpdate = CourtCaseEntity.builder().caseNo(CASE_NO).courtCode(COURT_CODE).build();

    @Test
    public void getCourtCase_shouldReturnCourtCaseResponse() {
        when(courtCaseResponseMapper.mapFrom(courtCaseEntity)).thenReturn(courtCaseResponse);
        when(courtCaseService.getCaseByCaseNumber(COURT_CODE, CASE_NO)).thenReturn(courtCaseEntity);
        CourtCaseResponse courtCase = courtCaseController.getCourtCase(COURT_CODE, CASE_NO);
        assertThat(courtCase).isEqualTo(courtCaseResponse);
    }

    @Test
    public void updateCaseByCourtAndCaseNo_shouldReturnCourtCaseResponse() {
        when(courtCaseResponseMapper.mapFrom(courtCaseEntity)).thenReturn(courtCaseResponse);
        when(courtCaseService.createOrUpdateCase(COURT_CODE, CASE_NO, courtCaseUpdate)).thenReturn(courtCaseEntity);
        CourtCaseResponse courtCase = courtCaseController.updateCourtCaseNo(COURT_CODE, CASE_NO, courtCaseUpdate);
        assertThat(courtCase).isSameAs(courtCaseResponse);
    }

    @Test
    public void getCaseList_shouldReturnCourtCaseResponse() {
        when(courtCaseResponseMapper.mapFrom(courtCaseEntity)).thenReturn(courtCaseResponse);
        when(courtCaseService.filterCasesByCourtAndDate(COURT_CODE, DATE)).thenReturn(Collections.singletonList(courtCaseEntity));
        CaseListResponse caseList = courtCaseController.getCaseList(COURT_CODE, DATE);
        assertThat(caseList.getCases().get(0)).isEqualTo(courtCaseResponse);
    }


    @Test
    public void getCaseList_sorted() {
        CourtCaseController controller = new CourtCaseController(courtCaseService, new CourtCaseResponseMapper());

        LocalDateTime mornSession = LocalDateTime.of(DATE, LocalTime.of(9, 30));
        LocalDateTime aftSession = LocalDateTime.of(DATE, LocalTime.of(14, 0));
        CourtCaseEntity entity1 = CourtCaseEntity.builder().courtRoom("1").sessionStartTime(mornSession).defendantName("Mr Nicholas CAGE").build();
        CourtCaseEntity entity2 = CourtCaseEntity.builder().courtRoom("1").sessionStartTime(mornSession).defendantName("Mr Christopher PLUMMER").build();
        CourtCaseEntity entity3 = CourtCaseEntity.builder().courtRoom("1").sessionStartTime(aftSession).defendantName("Mr Darren ARONOFSKY").build();
        CourtCaseEntity entity4 = CourtCaseEntity.builder().courtRoom("3").sessionStartTime(mornSession).defendantName("Mrs Minnie DRIVER").build();
        CourtCaseEntity entity5 = CourtCaseEntity.builder().courtRoom("3").sessionStartTime(aftSession).defendantName("Mrs Juliette BINOCHE").build();

        // Add in reverse order
        when(courtCaseService.filterCasesByCourtAndDate(COURT_CODE, DATE)).thenReturn(List.of(entity5, entity4, entity3, entity2, entity1));
        CaseListResponse caseList = controller.getCaseList(COURT_CODE, DATE);

        List<CourtCaseResponse> cases = caseList.getCases();
        assertThat(cases).hasSize(5);

        assertPosition(0, cases, "1", "Mr Nicholas CAGE", mornSession);
        assertPosition(1, cases, "1", "Mr Christopher PLUMMER", mornSession);
        assertPosition(2, cases, "1", "Mr Darren ARONOFSKY", aftSession);
        assertPosition(3, cases, "3", "Mrs Minnie DRIVER", mornSession);
        assertPosition(4, cases, "3", "Mrs Juliette BINOCHE", aftSession);
    }

    @Test
    public void whenDeleteMissingCases_ThenCallService() {

        Map<LocalDate, List<String>> existingCases = Map.of(LocalDate.now(), Arrays.asList("1000003", "1000007"));

        courtCaseController.purgeAbsentCases(COURT_CODE, existingCases);

        verify(courtCaseService).deleteAbsentCases(COURT_CODE, existingCases);
    }

    private void assertPosition(int position, List<CourtCaseResponse> cases, String courtRoom, String defendantName, LocalDateTime sessionTime) {
        assertThat(cases.get(position).getCourtRoom()).isEqualTo(courtRoom);
        assertThat(cases.get(position).getDefendantName()).isEqualTo(defendantName);
        assertThat(cases.get(position).getSessionStartTime()).isEqualTo(sessionTime);
    }
}
