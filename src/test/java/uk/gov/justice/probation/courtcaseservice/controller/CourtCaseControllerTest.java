package uk.gov.justice.probation.courtcaseservice.controller;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.when;

import java.time.LocalDate;
import java.util.Collections;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;
import uk.gov.justice.probation.courtcaseservice.controller.mapper.CourtCaseResponseMapper;
import uk.gov.justice.probation.courtcaseservice.controller.model.CaseListResponse;
import uk.gov.justice.probation.courtcaseservice.controller.model.CourtCaseResponse;
import uk.gov.justice.probation.courtcaseservice.jpa.entity.CourtCaseEntity;
import uk.gov.justice.probation.courtcaseservice.service.CourtCaseService;

@RunWith(MockitoJUnitRunner.class)
public class CourtCaseControllerTest {

    private static final String COURT_CODE = "COURT_CODE";
    private static final String CASE_NO = "CASE_NO";
    public static final String CASE_ID = "CASE_ID";
    private static final LocalDate DATE = LocalDate.of(2020, 2, 24);

    @Mock
    private CourtCaseService courtCaseService;
    @Mock
    private CourtCaseResponseMapper courtCaseResponseMapper;

    private CourtCaseController courtCaseController;
    private final CourtCaseEntity courtCaseEntity = new CourtCaseEntity(COURT_CODE, CASE_NO);
    private final CourtCaseResponse courtCaseResponse = new CourtCaseResponse();
    private final CourtCaseEntity courtCaseUpdate = new CourtCaseEntity(COURT_CODE, CASE_NO);

    @Before
    public void setUp() {
        courtCaseController = new CourtCaseController(courtCaseService, courtCaseResponseMapper);
        when(courtCaseResponseMapper.mapFrom(courtCaseEntity)).thenReturn(courtCaseResponse);
        when(courtCaseService.getCaseByCaseNumber(COURT_CODE, CASE_NO)).thenReturn(courtCaseEntity);
        when(courtCaseService.createOrUpdateCase(CASE_ID, courtCaseUpdate)).thenReturn(courtCaseEntity);
        when(courtCaseService.filterCasesByCourtAndDate(COURT_CODE, DATE)).thenReturn(Collections.singletonList(courtCaseEntity));
    }

    @Test
    public void getCourtCase_shouldReturnCourtCaseResponse() {
        CourtCaseResponse courtCase = courtCaseController.getCourtCase(COURT_CODE, CASE_NO);
        assertThat(courtCase).isEqualTo(courtCaseResponse);
    }

    @Test
    public void updateCase_shouldReturnCourtCaseResponse() {
        CourtCaseResponse courtCase = courtCaseController.updateCase(CASE_ID, courtCaseUpdate);
        assertThat(courtCase).isEqualTo(courtCaseResponse);
    }

    @Test
    public void updateCaseByCourtAndCaseNo_shouldReturnCourtCaseResponse() {
        when(courtCaseService.createOrUpdateCase(COURT_CODE, CASE_NO, courtCaseUpdate)).thenReturn(courtCaseEntity);

        CourtCaseResponse courtCase = courtCaseController.updateCourtCaseNo(COURT_CODE, CASE_NO, courtCaseUpdate);
        assertThat(courtCase).isSameAs(courtCaseResponse);
    }

    @Test
    public void getCaseList_shouldReturnCourtCaseResponse() {
        CaseListResponse caseList = courtCaseController.getCaseList(COURT_CODE, DATE);
        assertThat(caseList.getCases().get(0)).isEqualTo(courtCaseResponse);
    }
}
