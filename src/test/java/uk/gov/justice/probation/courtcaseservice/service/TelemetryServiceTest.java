package uk.gov.justice.probation.courtcaseservice.service;

import com.microsoft.applicationinsights.TelemetryClient;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Captor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import uk.gov.justice.probation.courtcaseservice.application.ClientDetails;
import uk.gov.justice.probation.courtcaseservice.jpa.entity.CourtCaseEntity;
import uk.gov.justice.probation.courtcaseservice.jpa.entity.GroupedOffenderMatchesEntity;
import uk.gov.justice.probation.courtcaseservice.jpa.entity.HearingEntity;
import uk.gov.justice.probation.courtcaseservice.jpa.entity.OffenderMatchEntity;
import uk.gov.justice.probation.courtcaseservice.restclient.exception.OffenderNotFoundException;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Map;

import static java.util.Collections.emptyList;
import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class TelemetryServiceTest {
    public static final String COURT_CODE = "SHF";
    public static final String CASE_NO = "1234567890";
    private static final String CRN = "CRN";
    private static final String PNC = "PNC";

    @Mock
    private TelemetryClient telemetryClient;
    @Mock
    private ClientDetails clientDetails;
    @Mock
    private HearingEntity firstHearing;
    @Mock
    private HearingEntity secondHearing;

    @Captor
    private ArgumentCaptor<Map<String, String>> properties;
    @Captor
    private ArgumentCaptor<Map<String, Double>> metricsCaptor;

    @InjectMocks
    private TelemetryService service;

    @Test
    public void givenUserDetailsAvailable_whenTrackMatchEvent_thenAddAllProperties() {
        when(clientDetails.getUsername()).thenReturn("Arthur");
        when(clientDetails.getClientId()).thenReturn("Van der Linde");

        OffenderMatchEntity match = OffenderMatchEntity.builder()
                .group(GroupedOffenderMatchesEntity.builder()
                        .offenderMatches(Arrays.asList(
                                OffenderMatchEntity.builder().build(),
                                OffenderMatchEntity.builder().build(),
                                OffenderMatchEntity.builder().build()))
                        .build())
                .pnc(PNC)
                .crn(CRN)
                .build();

        service.trackMatchEvent(TelemetryEventType.MATCH_CONFIRMED, match, buildCourtCase());

        verify(telemetryClient).trackEvent(eq("PiCMatchConfirmed"), properties.capture(), metricsCaptor.capture());

        var properties = this.properties.getValue();
        assertThat(properties.size()).isEqualTo(6);
        assertThat(properties.get("caseNo")).isEqualTo(CASE_NO);
        assertThat(properties.get("pnc")).isEqualTo(PNC);
        assertThat(properties.get("crn")).isEqualTo(CRN);
        assertThat(properties.get("matches")).isEqualTo("3");
        assertThat(properties.get("username")).isEqualTo("Arthur");
        assertThat(properties.get("clientId")).isEqualTo("Van der Linde");

        assertThat(metricsCaptor.getValue()).isEmpty();
    }

    @Test
    public void givenUserDetailsAvailable_whenTrackCourtCaseEvent_thenAddAllProperties() {
        when(clientDetails.getUsername()).thenReturn("Arthur");
        when(clientDetails.getClientId()).thenReturn("Van der Linde");
        when(firstHearing.loggableString()).thenReturn("first-hearing-description");
        when(secondHearing.loggableString()).thenReturn("second-hearing-description");

        CourtCaseEntity courtCase = buildCourtCase();

        service.trackCourtCaseEvent(TelemetryEventType.COURT_CASE_CREATED, courtCase);

        verify(telemetryClient).trackEvent(eq("PiCCourtCaseCreated"), properties.capture(), metricsCaptor.capture());

        var properties = this.properties.getValue();
        assertThat(properties.size()).isEqualTo(6);
        assertThat(properties.get("caseNo")).isEqualTo(CASE_NO);
        assertThat(properties.get("crn")).isEqualTo(CRN);
        assertThat(properties.get("pnc")).isEqualTo(PNC);
        assertThat(properties.get("hearings")).isEqualTo("first-hearing-description,second-hearing-description");
        assertThat(properties.get("username")).isEqualTo("Arthur");
        assertThat(properties.get("clientId")).isEqualTo("Van der Linde");

        assertThat(metricsCaptor.getValue()).isEmpty();
    }

    @Test
    public void givenNullProperties_whenTrackCourtCaseEvent_thenExcludeUserProperties() {
        CourtCaseEntity courtCase = buildCourtCase();

        service.trackCourtCaseEvent(TelemetryEventType.COURT_CASE_CREATED, courtCase);

        verify(telemetryClient).trackEvent(eq("PiCCourtCaseCreated"), properties.capture(), metricsCaptor.capture());

        var properties = this.properties.getValue();
        assertThat(properties.size()).isEqualTo(4);
        assertThat(properties.get("caseNo")).isEqualTo(CASE_NO);
        assertThat(properties.get("crn")).isEqualTo(CRN);
        assertThat(properties.get("pnc")).isEqualTo(PNC);

        assertThat(metricsCaptor.getValue()).isEmpty();
    }

    @Test
    public void givenAllValuesAreNull_whenTrackCourtCaseEvent_thenNoPropertiesReturned() {
        CourtCaseEntity courtCase = CourtCaseEntity.builder()
                .build();

        service.trackCourtCaseEvent(TelemetryEventType.COURT_CASE_CREATED, courtCase);

        verify(telemetryClient).trackEvent(eq("PiCCourtCaseCreated"), properties.capture(), metricsCaptor.capture());

        var properties = this.properties.getValue();
        assertThat(properties.size()).isEqualTo(0);

        assertThat(metricsCaptor.getValue()).isEmpty();
    }

    @Test
    public void whenTrackApplicationDegradationEvent_thenCallService() {

        var e = new OffenderNotFoundException(CRN);

        service.trackApplicationDegradationEvent("desc", e, CRN);

        var properties = Map.of("description", "desc", "crn", CRN, "cause", "Offender with CRN 'CRN' not found");

        verify(telemetryClient).trackEvent(TelemetryEventType.GRACEFUL_DEGRADE.eventName, properties, Collections.emptyMap());
    }

    private CourtCaseEntity buildCourtCase() {
        return CourtCaseEntity.builder()
                .caseNo(CASE_NO)
                .hearings(List.of(
                        firstHearing,
                        secondHearing
                ))
                .offences(emptyList())
                .crn(CRN)
                .pnc(PNC)
                .build();
    }
}
