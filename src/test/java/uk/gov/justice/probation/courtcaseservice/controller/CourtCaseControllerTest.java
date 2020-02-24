package uk.gov.justice.probation.courtcaseservice.controller;

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

import java.time.LocalDate;
import java.util.Collections;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.when;

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
    private CourtCaseEntity courtCaseEntity = new CourtCaseEntity();
    private CourtCaseResponse courtCaseResponse = new CourtCaseResponse();
    private CourtCaseEntity courtCaseUpdate = new CourtCaseEntity();

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
    public void getCaseList_shouldReturnCourtCaseResponse() {
        CaseListResponse caseList = courtCaseController.getCaseList(COURT_CODE, DATE);
        assertThat(caseList.getCases().get(0)).isEqualTo(courtCaseResponse);
    }
}