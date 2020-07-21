package uk.gov.justice.probation.courtcaseservice.controller;

import java.time.LocalDate;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Map;
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

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

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
    public void whenDeleteMissingCases_ThenCallService() {

        Map<LocalDate, List<String>> existingCases = Map.of(LocalDate.now(), Arrays.asList("1000003", "1000007"));

        courtCaseController.purgeAbsentCases(COURT_CODE, existingCases);

        verify(courtCaseService).deleteAbsentCases(COURT_CODE, existingCases);
    }
}
