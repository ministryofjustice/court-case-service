package uk.gov.justice.probation.courtcaseservice.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatExceptionOfType;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoMoreInteractions;
import static org.mockito.Mockito.when;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import reactor.core.publisher.Mono;
import uk.gov.justice.probation.courtcaseservice.controller.model.AttendancesResponse;
import uk.gov.justice.probation.courtcaseservice.restclient.ConvictionRestClient;
import uk.gov.justice.probation.courtcaseservice.restclient.exception.OffenderNotFoundException;

@ExtendWith(MockitoExtension.class)
class ConvictionServiceTest {

    public static final String CRN = "X370652";
    public static final Long SOME_CONVICTION_ID = 1234L;

    @Mock
    private AttendancesResponse attendancesResponse;

    @Mock
    private ConvictionRestClient restClient;

    @InjectMocks
    private ConvictionService service;

    @DisplayName("Normal retrieval of attendances")
    @Test
    public void whenGetAttendances_returnAttendances() {

        when(restClient.getAttendancesByCrnAndConvictionId(CRN, SOME_CONVICTION_ID)).thenReturn(Mono.just(attendancesResponse));

        final AttendancesResponse response = service.getAttendances(CRN, SOME_CONVICTION_ID);

        assertThat(response).isSameAs(attendancesResponse);
        verify(restClient).getAttendancesByCrnAndConvictionId(CRN, SOME_CONVICTION_ID);
        verifyNoMoreInteractions(restClient);
    }

    @DisplayName("An empty response means that we get an exception")
    @Test
    void givenAttendancesNotFound_whenGetAttendances_thenThrowException() {

        when(restClient.getAttendancesByCrnAndConvictionId(CRN, SOME_CONVICTION_ID)).thenReturn(Mono.empty());

        assertThatExceptionOfType(OffenderNotFoundException.class)
            .isThrownBy(() -> service.getAttendances(CRN, SOME_CONVICTION_ID))
            .withMessageContaining(CRN);
    }
}
