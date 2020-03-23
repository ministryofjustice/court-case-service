package uk.gov.justice.probation.courtcaseservice.controller;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;
import reactor.core.publisher.Mono;
import uk.gov.justice.probation.courtcaseservice.service.OffenderService;
import uk.gov.justice.probation.courtcaseservice.service.model.Offender;
import uk.gov.justice.probation.courtcaseservice.service.model.Requirement;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.when;

@RunWith(MockitoJUnitRunner.class)
public class OffenderControllerTest {
    public static final String CRN = "CRN";
    public static final String CONVICTION_ID = "CONVICTION_ID";

    @Mock
    private OffenderService service;
    @Mock
    private Offender offender;
    private OffenderController controller;

    @Before
    public void setUp() {
        when(service.getOffender(CRN)).thenReturn(offender);

        controller = new OffenderController(service);
    }

    @Test
    public void whenGetOffender_thenReturnIt() {
        Offender offenderResponse = controller.getOffender(CRN);
        assertThat(offenderResponse).isNotNull();
        assertThat(offenderResponse).isEqualTo(offenderResponse);
    }

    @Test
    public void whenGetRequirements_thenReturnIt() {
        Mono<List<Requirement>> requirementResponse = controller.getRequirements(CRN, CONVICTION_ID);
        assertThat(requirementResponse).isEqualTo(requirementResponse);
    }
}
