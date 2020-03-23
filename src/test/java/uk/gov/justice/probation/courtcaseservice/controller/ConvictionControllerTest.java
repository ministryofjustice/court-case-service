package uk.gov.justice.probation.courtcaseservice.controller;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoMoreInteractions;
import static org.mockito.Mockito.when;

import java.util.Collections;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import uk.gov.justice.probation.courtcaseservice.application.FeatureFlags;
import uk.gov.justice.probation.courtcaseservice.controller.model.AttendancesResponse;
import uk.gov.justice.probation.courtcaseservice.service.ConvictionService;

@ExtendWith(MockitoExtension.class)
class ConvictionControllerTest {

    static final String CRN = "X1234";
    static final Long SOME_EVENT_ID = 1234L;

    @Mock
    private ConvictionService convictionService;

    private FeatureFlags featureFlags;

    private ConvictionController convictionController;

    @BeforeEach
    void beforeEach() {
        featureFlags = new FeatureFlags();
        convictionController = new ConvictionController(convictionService, featureFlags);
    }

    @DisplayName("Normal service call returns response")
    @Test
    void callReturnsResponse() {
        final AttendancesResponse attendancesResponse = AttendancesResponse.builder().attendances(Collections.emptyList()).build();
        when(convictionService.getAttendances(CRN, SOME_EVENT_ID)).thenReturn(attendancesResponse);

        assertThat(convictionController.getAttendances(CRN, SOME_EVENT_ID)).isEqualTo(attendancesResponse);

        verify(convictionService).getAttendances(CRN, SOME_EVENT_ID);
        verifyNoMoreInteractions(convictionService);
    }

    @DisplayName("Feature toggle is off")
    @Test
    void featureToggleFalse() {
        featureFlags.setFlagValue("fetch-attendance-data", false);

        final AttendancesResponse attendancesResponse = AttendancesResponse.builder().crn(CRN).convictionId(SOME_EVENT_ID).build();

        assertThat(convictionController.getAttendances(CRN, SOME_EVENT_ID)).isEqualTo(attendancesResponse);
        verify(convictionService, times(0)).getAttendances(CRN, SOME_EVENT_ID);
    }
}
