package uk.gov.justice.probation.courtcaseservice.controller;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import uk.gov.justice.probation.courtcaseservice.application.FeatureFlags;
import uk.gov.justice.probation.courtcaseservice.controller.model.ConvictionResponse;
import uk.gov.justice.probation.courtcaseservice.controller.model.RequirementsResponse;
import uk.gov.justice.probation.courtcaseservice.service.ConvictionService;
import uk.gov.justice.probation.courtcaseservice.service.OffenderService;
import uk.gov.justice.probation.courtcaseservice.service.model.ProbationRecord;
import uk.gov.justice.probation.courtcaseservice.service.model.Requirement;
import uk.gov.justice.probation.courtcaseservice.service.model.UnpaidWork;

import java.util.Collections;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
public class OffenderControllerTest {
    public static final String CRN = "CRN";
    public static final String CONVICTION_ID = "CONVICTION_ID";
    static final Long SOME_EVENT_ID = 1234L;

    @Mock
    private OffenderService offenderService;
    @Mock
    private ProbationRecord expectedProbationRecord;
    @Mock
    private Requirement expectedRequirement;

    @Mock
    private ConvictionService convictionService;

    private FeatureFlags featureFlags;

    private OffenderController controller;

    @BeforeEach
    void beforeEach() {
        featureFlags = new FeatureFlags();
        controller = new OffenderController(offenderService, convictionService, featureFlags);
    }

    @DisplayName("Normal service call returns response")
    @Test
    void callReturnsResponse() {
        final ConvictionResponse attendancesResponse = ConvictionResponse.builder().attendances(Collections.emptyList()).build();
        when(convictionService.getConviction(CRN, SOME_EVENT_ID)).thenReturn(attendancesResponse);

        assertThat(controller.getConviction(CRN, SOME_EVENT_ID)).isEqualTo(attendancesResponse);

        verify(convictionService).getConviction(CRN, SOME_EVENT_ID);
        verifyNoMoreInteractions(convictionService);
    }

    @DisplayName("Feature toggle is off")
    @Test
    void featureToggleFalse() {
        featureFlags.setFlagValue("fetch-attendance-data", false);
        final ConvictionResponse convictionResponse = ConvictionResponse.builder()
                .attendances(Collections.emptyList())
                .unpaidWork(UnpaidWork.builder().build())
                .build();
        when(convictionService.getConvictionNoAttendances(CRN, SOME_EVENT_ID)).thenReturn(convictionResponse);

        assertThat(controller.getConviction(CRN, SOME_EVENT_ID)).isEqualToComparingFieldByField(convictionResponse);
        verify(convictionService).getConvictionNoAttendances(CRN, SOME_EVENT_ID);
        verifyNoMoreInteractions(convictionService);
    }

    @DisplayName("Ensues that the controller calls the service and returns the same offender probation record")
    @Test
    public void whenGetProbationRecord_thenReturnIt() {

        final boolean applyFilter = true;
        when(offenderService.getProbationRecord(CRN, applyFilter)).thenReturn(expectedProbationRecord);

        ProbationRecord probationRecordResponse = controller.getProbationRecord(CRN, applyFilter);

        assertThat(probationRecordResponse).isNotNull();
        assertThat(probationRecordResponse).isEqualTo(expectedProbationRecord);
        verify(offenderService).getProbationRecord(CRN, applyFilter);
        verifyNoMoreInteractions(offenderService);
    }

    @DisplayName("Ensues that the controller calls the service and returns the same list of requirements")
    @Test
    public void whenGetRequirements_thenReturnIt() {

        when(offenderService.getConvictionRequirements(CRN, CONVICTION_ID)).thenReturn(Collections.singletonList(expectedRequirement));

        RequirementsResponse requirementResponse = controller.getRequirements(CRN, CONVICTION_ID);

        assertThat(requirementResponse).isNotNull();
        assertThat(requirementResponse.getRequirements()).hasSize(1);
        assertThat(requirementResponse.getRequirements().get(0)).isEqualTo(expectedRequirement);
        verify(offenderService).getConvictionRequirements(CRN, CONVICTION_ID);
    }
}
