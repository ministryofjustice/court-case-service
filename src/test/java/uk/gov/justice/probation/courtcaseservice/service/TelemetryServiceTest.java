package uk.gov.justice.probation.courtcaseservice.service;

import com.microsoft.applicationinsights.TelemetryClient;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Captor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import uk.gov.justice.probation.courtcaseservice.jpa.entity.CourtCaseEntity;
import uk.gov.justice.probation.courtcaseservice.jpa.entity.GroupedOffenderMatchesEntity;
import uk.gov.justice.probation.courtcaseservice.jpa.entity.OffenderMatchEntity;

import java.time.LocalDateTime;
import java.util.Arrays;
import java.util.Map;

import static java.util.Collections.emptyList;
import static java.util.Collections.singletonList;
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

    @Mock
    private TelemetryClient telemetryClient;
    @Mock
    private Map<String, String> requestProperties;

    @Captor
    private ArgumentCaptor<Map<String, String>> properties;
    @Captor
    private ArgumentCaptor<Map<String, Double>> metricsCaptor;

    @InjectMocks
    private TelemetryService service;

    @Test
    public void givenUserDetailsAvailable_whenTrackCourtCaseEvent_thenAddAllProperties() {
        when(requestProperties.get("username")).thenReturn("Arthur");
        when(requestProperties.get("clientId")).thenReturn("Van der Linde");

        CourtCaseEntity courtCase = buildCourtCase();

        service.trackCourtCaseEvent(TelemetryEventType.COURT_CASE_CREATED, courtCase);

        verify(telemetryClient).trackEvent(eq("PiCCourtCaseCreated"), properties.capture(), metricsCaptor.capture());

        var properties = this.properties.getValue();
        assertThat(properties.size()).isEqualTo(7);
        assertThat(properties.get("courtCode")).isEqualTo(COURT_CODE);
        assertThat(properties.get("courtRoom")).isEqualTo(COURT_ROOM);
        assertThat(properties.get("caseNo")).isEqualTo(CASE_NO);
        assertThat(properties.get("hearingDate")).isEqualTo(standardDateOf(2020, 9 , 22));
        assertThat(properties.get("matches")).isEqualTo("2");
        assertThat(properties.get("username")).isEqualTo("Arthur");
        assertThat(properties.get("clientId")).isEqualTo("Van der Linde");

        assertThat(metricsCaptor.getValue().isEmpty());
    }

    @Test
    public void givenNullProperties_whenTrackCourtCaseEvent_thenExcludeUserProperties() {
        CourtCaseEntity courtCase = buildCourtCase();

        service.trackCourtCaseEvent(TelemetryEventType.COURT_CASE_CREATED, courtCase);

        verify(telemetryClient).trackEvent(eq("PiCCourtCaseCreated"), properties.capture(), metricsCaptor.capture());

        var properties = this.properties.getValue();
        assertThat(properties.size()).isEqualTo(5);
        assertThat(properties.get("courtCode")).isEqualTo(COURT_CODE);
        assertThat(properties.get("courtRoom")).isEqualTo(COURT_ROOM);
        assertThat(properties.get("caseNo")).isEqualTo(CASE_NO);
        assertThat(properties.get("hearingDate")).isEqualTo(standardDateOf(2020, 9 , 22));
        assertThat(properties.get("matches")).isEqualTo("2");

        assertThat(metricsCaptor.getValue().isEmpty());
    }

    @Test
    public void givenAllValuesAreNull_whenTrackCourtCaseEvent_thenNoPropertiesReturned() {
        CourtCaseEntity courtCase = CourtCaseEntity.builder()
                .build();

        service.trackCourtCaseEvent(TelemetryEventType.COURT_CASE_CREATED, courtCase);

        verify(telemetryClient).trackEvent(eq("PiCCourtCaseCreated"), properties.capture(), metricsCaptor.capture());

        var properties = this.properties.getValue();
        assertThat(properties.size()).isEqualTo(0);

        assertThat(metricsCaptor.getValue().isEmpty());
    }

    private CourtCaseEntity buildCourtCase() {
        return CourtCaseEntity.builder()
                .courtCode(COURT_CODE)
                .caseNo(CASE_NO)
                .courtRoom(COURT_ROOM)
                .offences(emptyList())
                .groupedOffenderMatches(Arrays.asList(
                        GroupedOffenderMatchesEntity.builder()
                                .offenderMatches(singletonList(OffenderMatchEntity.builder()
                                        .crn("1")
                                        .build()))
                                .build(),
                        GroupedOffenderMatchesEntity.builder()
                                .offenderMatches(singletonList(OffenderMatchEntity.builder()
                                        .crn("2")
                                        .build()))
                                .build()
                ))
                .sessionStartTime(LocalDateTime.of(2020, 9, 22, 9, 30))
                .build();
    }
}