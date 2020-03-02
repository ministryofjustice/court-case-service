package uk.gov.justice.probation.courtcaseservice.controller;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;
import uk.gov.justice.probation.courtcaseservice.controller.mapper.OffenderResponseMapper;
import uk.gov.justice.probation.courtcaseservice.controller.model.OffenderResponse;
import uk.gov.justice.probation.courtcaseservice.service.OffenderService;
import uk.gov.justice.probation.courtcaseservice.service.model.Offender;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.when;

@RunWith(MockitoJUnitRunner.class)
public class OffenderControllerTest {
    public static final String CRN = "CRN";
    @Mock
    private OffenderService service;
    @Mock
    private OffenderResponseMapper mapper;
    @Mock
    private Offender offender;
    @Mock
    private OffenderResponse offenderResponse;
    private OffenderController controller;

    @Before
    public void setUp() {
        when(service.getOffender(CRN)).thenReturn(offender);
        when(mapper.mapFrom(offender)).thenReturn(offenderResponse);

        controller = new OffenderController(service, mapper);
    }

    @Test
    public void whenGetOffender_thenReturnOffenderResponse() {
        OffenderResponse offender = controller.getOffender(CRN);
        assertThat(offender).isNotNull();
        assertThat(offender).isEqualTo(offenderResponse);
    }
}