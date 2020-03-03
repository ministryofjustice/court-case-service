package uk.gov.justice.probation.courtcaseservice.service;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;
import reactor.core.publisher.Mono;
import uk.gov.justice.probation.courtcaseservice.restclient.OffenderRestClient;
import uk.gov.justice.probation.courtcaseservice.service.model.Offender;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.when;

@RunWith(MockitoJUnitRunner.class)
public class OffenderServiceTest {

    public static final String CRN = "CRN";
    @Mock
    private OffenderRestClient offenderRestClient;
    @Mock
    private Offender offender;

    private OffenderService service;

    @Before
    public void setUp() {
        service = new OffenderService(offenderRestClient);
        when(offenderRestClient.getOffenderByCrn(CRN)).thenReturn(Mono.just(offender));
    }

    @Test
    public void whenGetOffender_returnOffender() {
        Offender offender = service.getOffender(CRN);
        assertThat(offender).isNotNull();
        assertThat(offender).isEqualTo(offender);
    }

}