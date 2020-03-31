package uk.gov.justice.probation.courtcaseservice.controller;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;
import uk.gov.justice.probation.courtcaseservice.controller.model.RequirementsResponse;
import uk.gov.justice.probation.courtcaseservice.service.OffenderService;
import uk.gov.justice.probation.courtcaseservice.service.model.Offender;
import uk.gov.justice.probation.courtcaseservice.service.model.Requirement;

import java.util.Collections;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.when;

@RunWith(MockitoJUnitRunner.class)
public class OffenderControllerTest {
    public static final String CRN = "CRN";
    public static final String CONVICTION_ID = "CONVICTION_ID";

    @Mock
    private OffenderService service;
    @Mock
    private Offender expectedOffender;
    @Mock
    private Requirement expectedRequirement;
    private OffenderController controller;

    @Before
    public void setUp() {
        when(service.getOffender(CRN)).thenReturn(expectedOffender);
        when(service.getConvictionRequirements(CRN, CONVICTION_ID)).thenReturn(Collections.singletonList(expectedRequirement));

        controller = new OffenderController(service);
    }

    @Test
    public void whenGetOffender_thenReturnIt() {
        Offender offenderResponse = controller.getOffender(CRN);
        assertThat(offenderResponse).isNotNull();
        assertThat(offenderResponse).isEqualTo(expectedOffender);
    }

    @Test
    public void whenGetRequirements_thenReturnIt() {
        RequirementsResponse requirementResponse = controller.getRequirements(CRN, CONVICTION_ID);
        assertThat(requirementResponse).isNotNull();
        assertThat(requirementResponse.getRequirements()).hasSize(1);
        assertThat(requirementResponse.getRequirements().get(0)).isEqualTo(expectedRequirement);
    }
}
