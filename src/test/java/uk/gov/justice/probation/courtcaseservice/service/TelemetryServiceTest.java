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
import uk.gov.justice.probation.courtcaseservice.jpa.entity.OffenderMatchEntity;

import java.time.LocalDateTime;
import java.util.Arrays;
import java.util.Map;

import static java.util.Collections.emptyList;
import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static uk.gov.justice.probation.courtcaseservice.testUtil.DateHelper.standardDateOf;

@ExtendWith(MockitoExtension.class)
class TelemetryServiceTest {
    public static final String COURT_CODE = "SHF";
    public static final String CASE_NO = "1234567890";
    public static final String COURT_ROOM = "Number 2";
    private static final String CRN = "CRN";
    private static final String PNC = "PNC";

    @Mock
    private TelemetryClient telemetryClient;
    @Mock
    private ClientDetails clientDetails;

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
        assertThat(properties.size()).isEqualTo(7);
        assertThat(properties.get("courtCode")).isEqualTo(COURT_CODE);
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

        CourtCaseEntity courtCase = buildCourtCase();

        service.trackCourtCaseEvent(TelemetryEventType.COURT_CASE_CREATED, courtCase);

        verify(telemetryClient).trackEvent(eq("PiCCourtCaseCreated"), properties.capture(), metricsCaptor.capture());

        var properties = this.properties.getValue();
        assertThat(properties.size()).isEqualTo(8);
        assertThat(properties.get("courtCode")).isEqualTo(COURT_CODE);
        assertThat(properties.get("courtRoom")).isEqualTo(COURT_ROOM);
        assertThat(properties.get("caseNo")).isEqualTo(CASE_NO);
        assertThat(properties.get("crn")).isEqualTo(CRN);
        assertThat(properties.get("pnc")).isEqualTo(PNC);
        assertThat(properties.get("hearingDate")).isEqualTo(standardDateOf(2020, 9 , 22));
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
        assertThat(properties.size()).isEqualTo(6);
        assertThat(properties.get("courtCode")).isEqualTo(COURT_CODE);
        assertThat(properties.get("courtRoom")).isEqualTo(COURT_ROOM);
        assertThat(properties.get("caseNo")).isEqualTo(CASE_NO);
        assertThat(properties.get("crn")).isEqualTo(CRN);
        assertThat(properties.get("pnc")).isEqualTo(PNC);
        assertThat(properties.get("hearingDate")).isEqualTo(standardDateOf(2020, 9 , 22));

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
    public void whenTrackSimpleEvent_thenCallService() {

        service.trackTelemetryEvent(TelemetryEventType.GRACEFUL_DEGRADE);

        verify(telemetryClient).trackEvent(TelemetryEventType.GRACEFUL_DEGRADE.eventName);
    }

    private CourtCaseEntity buildCourtCase() {
        return CourtCaseEntity.builder()
                .courtCode(COURT_CODE)
                .caseNo(CASE_NO)
                .courtRoom(COURT_ROOM)
                .offences(emptyList())
                .crn(CRN)
                .pnc(PNC)
                .sessionStartTime(LocalDateTime.of(2020, 9, 22, 9, 30))
                .build();
    }
}
