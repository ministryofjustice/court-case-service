package uk.gov.justice.probation.courtcaseservice.controller;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.when;

import java.time.LocalDate;
import java.util.Collections;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import uk.gov.justice.probation.courtcaseservice.controller.mapper.CourtCaseResponseMapper;
import uk.gov.justice.probation.courtcaseservice.controller.model.CaseListResponse;
import uk.gov.justice.probation.courtcaseservice.controller.model.CourtCaseResponse;
import uk.gov.justice.probation.courtcaseservice.jpa.entity.CourtCaseEntity;
import uk.gov.justice.probation.courtcaseservice.service.CourtCaseService;

@ExtendWith(MockitoExtension.class)
public class CourtCaseControllerTest {

    private static final String COURT_CODE = "COURT_CODE";
    private static final String CASE_NO = "CASE_NO";
    private static final LocalDate DATE = LocalDate.of(2020, 2, 24);

    @Mock
    private CourtCaseService courtCaseService;
    @Mock
    private CourtCaseResponseMapper courtCaseResponseMapper;
    @Mock
    private CourtCaseResponse courtCaseResponse;
    @InjectMocks
    private CourtCaseController courtCaseController;
    private final CourtCaseEntity courtCaseEntity = CourtCaseEntity.builder().caseNo(CASE_NO).courtCode(COURT_CODE).build();
    private final CourtCaseEntity courtCaseUpdate = CourtCaseEntity.builder().caseNo(CASE_NO).courtCode(COURT_CODE).build();

    @BeforeEach
    public void setUp() {
        when(courtCaseResponseMapper.mapFrom(courtCaseEntity)).thenReturn(courtCaseResponse);
    }

    @Test
    public void getCourtCase_shouldReturnCourtCaseResponse() {
        when(courtCaseService.getCaseByCaseNumber(COURT_CODE, CASE_NO)).thenReturn(courtCaseEntity);
        CourtCaseResponse courtCase = courtCaseController.getCourtCase(COURT_CODE, CASE_NO);
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
        when(courtCaseService.filterCasesByCourtAndDate(COURT_CODE, DATE)).thenReturn(Collections.singletonList(courtCaseEntity));
        CaseListResponse caseList = courtCaseController.getCaseList(COURT_CODE, DATE);
        assertThat(caseList.getCases().get(0)).isEqualTo(courtCaseResponse);
    }
}
