package uk.gov.justice.probation.courtcaseservice.service;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;
import reactor.core.publisher.Mono;
import uk.gov.justice.probation.courtcaseservice.restclient.OffenderRestClient;
import uk.gov.justice.probation.courtcaseservice.restclient.exception.OffenderNotFoundException;
import uk.gov.justice.probation.courtcaseservice.service.model.Conviction;
import uk.gov.justice.probation.courtcaseservice.service.model.Offender;
import uk.gov.justice.probation.courtcaseservice.service.model.Requirement;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatExceptionOfType;
import static org.mockito.Mockito.when;

@RunWith(MockitoJUnitRunner.class)
public class OffenderServiceTest {

    public static final String CRN = "CRN";
    public static final String CONVICTION_ID = "CONVICTION_ID";
    @Mock
    private OffenderRestClient offenderRestClient;
    private Offender offender;
    @Mock
    private List<Conviction> expectedConvictions;
    @Mock
    private List<Requirement> expectedRequirements;

    private OffenderService service;

    @Before
    public void setUp() {
        service = new OffenderService(offenderRestClient);
        offender = Offender.builder().build();
        when(offenderRestClient.getOffenderByCrn(CRN)).thenReturn(Mono.just(offender));
        when(offenderRestClient.getConvictionsByCrn(CRN)).thenReturn(Mono.just(expectedConvictions));
        when(offenderRestClient.getConvictionRequirements(CRN, CONVICTION_ID)).thenReturn(Mono.just(expectedRequirements));
    }

    @Test
    public void whenGetOffender_returnOffender() {
        Offender offender = service.getOffender(CRN);
        assertThat(offender).isNotNull();
        assertThat(offender).isEqualTo(offender);
    }

    @Test
    public void givenOffenderNotFound_whenGetOffender_thenThrowException() {
        when(offenderRestClient.getOffenderByCrn(CRN)).thenReturn(Mono.empty());
        assertThatExceptionOfType(OffenderNotFoundException.class)
                .isThrownBy(() -> service.getOffender(CRN))
                .withMessageContaining(CRN);
    }

    @Test
    public void whenGetOffender_returnOffenderConvictions() {
        Offender offender = service.getOffender(CRN);
        assertThat(offender).isNotNull();
        assertThat(offender.getConvictions()).isEqualTo(expectedConvictions);
    }

    @Test
    public void givenConvictionsNotFound_whenGetOffender_thenThrowException() {
        when(offenderRestClient.getConvictionsByCrn(CRN)).thenReturn(Mono.empty());
        assertThatExceptionOfType(OffenderNotFoundException.class)
                .isThrownBy(() -> service.getOffender(CRN))
                .withMessageContaining(CRN);
    }

    @Test
    public void whenGetConvictionRequirements_returnRequirements() {
        List<Requirement> requirements = service.getConvictionRequirements(CRN, CONVICTION_ID);
        assertThat(requirements).isEqualTo(expectedRequirements);
    }
}
