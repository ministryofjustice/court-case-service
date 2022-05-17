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
import uk.gov.justice.probation.courtcaseservice.jpa.entity.DefendantEntity;
import uk.gov.justice.probation.courtcaseservice.jpa.entity.GroupedOffenderMatchesEntity;
import uk.gov.justice.probation.courtcaseservice.jpa.entity.HearingDayEntity;
import uk.gov.justice.probation.courtcaseservice.jpa.entity.HearingDefendantEntity;
import uk.gov.justice.probation.courtcaseservice.jpa.entity.HearingEntity;
import uk.gov.justice.probation.courtcaseservice.jpa.entity.OffenderMatchEntity;
import uk.gov.justice.probation.courtcaseservice.restclient.exception.OffenderNotFoundException;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static uk.gov.justice.probation.courtcaseservice.jpa.entity.EntityHelper.anOffender;
import static uk.gov.justice.probation.courtcaseservice.jpa.entity.SourceType.LIBRA;

@ExtendWith(MockitoExtension.class)
class TelemetryServiceTest {
    public static final String CASE_ID = "1234567890";
    public static final String HEARING_ID = "0987654321";
    private static final String CRN = "CRN";
    private static final String PNC = "PNC";
    private static final String DEFENDANT_ID = "DEFENDANT_ID";

    @Mock
    private TelemetryClient telemetryClient;
    @Mock
    private ClientDetails clientDetails;
    @Mock
    private HearingDayEntity firstHearing;
    @Mock
    private HearingDayEntity secondHearing;

    @Captor
    private ArgumentCaptor<Map<String, String>> properties;
    @Captor
    private ArgumentCaptor<Map<String, Double>> metricsCaptor;

    @InjectMocks
    private TelemetryService service;

    @Test
    void givenUserDetailsAvailable_whenTrackMatchEvent_thenAddAllProperties() {
        when(clientDetails.getUsername()).thenReturn("Arthur");
        when(clientDetails.getClientId()).thenReturn("Van der Linde");

        var match = OffenderMatchEntity.builder()
                .group(GroupedOffenderMatchesEntity.builder()
                        .offenderMatches(Arrays.asList(
                                OffenderMatchEntity.builder().build(),
                                OffenderMatchEntity.builder().build(),
                                OffenderMatchEntity.builder().build()))
                        .build())
                .pnc(PNC)
                .crn(CRN)
                .build();

        service.trackMatchEvent(TelemetryEventType.MATCH_CONFIRMED, match, buildHearing(), DEFENDANT_ID);

        verify(telemetryClient).trackEvent(eq("PiCMatchConfirmed"), properties.capture(), metricsCaptor.capture());

        var properties = this.properties.getValue();
        assertThat(properties.size()).isEqualTo(9);
        assertThat(properties.get("defendantId")).isEqualTo(DEFENDANT_ID);
        assertThat(properties.get("caseId")).isEqualTo(CASE_ID);
        assertThat(properties.get("hearingId")).isEqualTo(HEARING_ID);
        assertThat(properties.get("pnc")).isEqualTo(PNC);
        assertThat(properties.get("crn")).isEqualTo(CRN);
        assertThat(properties.get("matches")).isEqualTo("3");
        assertThat(properties.get("username")).isEqualTo("Arthur");
        assertThat(properties.get("clientId")).isEqualTo("Van der Linde");
        assertThat(properties.get("source")).isEqualTo("LIBRA");

        assertThat(metricsCaptor.getValue()).isEmpty();
    }

    @Test
    void givenUserDetailsAvailable_whenTrackCourtCaseEvent_thenAddAllProperties() {
        when(clientDetails.getUsername()).thenReturn("Arthur");
        when(clientDetails.getClientId()).thenReturn("Van der Linde");
        when(firstHearing.loggableString()).thenReturn("first-hearing-description");
        when(secondHearing.loggableString()).thenReturn("second-hearing-description");

        var courtCase = buildHearing();

        service.trackCourtCaseEvent(TelemetryEventType.COURT_CASE_CREATED, courtCase);

        verify(telemetryClient).trackEvent(eq("PiCCourtCaseCreated"), properties.capture(), metricsCaptor.capture());

        var properties = this.properties.getValue();
        assertThat(properties).hasSize(6);
        assertThat(properties.get("caseId")).isEqualTo(CASE_ID);
        assertThat(properties.get("hearingId")).isEqualTo(HEARING_ID);
        assertThat(properties.get("hearings")).isEqualTo("first-hearing-description,second-hearing-description");
        assertThat(properties.get("username")).isEqualTo("Arthur");
        assertThat(properties.get("clientId")).isEqualTo("Van der Linde");
        assertThat(properties.get("source")).isEqualTo("LIBRA");

        assertThat(metricsCaptor.getValue()).isEmpty();
    }

    @Test
    void givenNullProperties_whenTrackCourtCaseEvent_thenExcludeUserProperties() {
        var courtCase = buildHearing();
        when(firstHearing.loggableString()).thenReturn("first-hearing-description");
        when(secondHearing.loggableString()).thenReturn("second-hearing-description");

        service.trackCourtCaseEvent(TelemetryEventType.COURT_CASE_CREATED, courtCase);

        verify(telemetryClient).trackEvent(eq("PiCCourtCaseCreated"), properties.capture(), metricsCaptor.capture());

        var properties = this.properties.getValue();
        assertThat(properties).hasSize(4);
        assertThat(properties.get("caseId")).isEqualTo(CASE_ID);
        assertThat(properties.get("hearings")).isEqualTo("first-hearing-description,second-hearing-description");
        assertThat(properties.get("source")).isEqualTo("LIBRA");

        assertThat(metricsCaptor.getValue()).isEmpty();
    }

    @Test
    void givenAllValuesAreNull_whenTrackCourtCaseEvent_thenNoPropertiesReturned() {
        var courtCase = HearingEntity.builder()
            .courtCase(CourtCaseEntity.builder().build())
            .build();

        service.trackCourtCaseEvent(TelemetryEventType.COURT_CASE_CREATED, courtCase);

        verify(telemetryClient).trackEvent(eq("PiCCourtCaseCreated"), properties.capture(), metricsCaptor.capture());

        var properties = this.properties.getValue();
        assertThat(properties.size()).isEqualTo(0);

        assertThat(metricsCaptor.getValue()).isEmpty();
    }

    @Test
    void whenTrackApplicationDegradationEvent_thenCallService() {

        var e = new OffenderNotFoundException(CRN);

        service.trackApplicationDegradationEvent("desc", e, CRN);

        var properties = Map.of("description", "desc", "crn", CRN, "cause", "Offender with CRN 'CRN' not found");

        verify(telemetryClient).trackEvent(TelemetryEventType.GRACEFUL_DEGRADE.eventName, properties, Collections.emptyMap());
    }

    @Test
    void whenTrackDefendantEvent_thenCallService() {

        var defendant = HearingDefendantEntity.builder()
                .defendant(DefendantEntity.builder()
                        .pnc(PNC)
                        .defendantId(DEFENDANT_ID)
                        .offender(anOffender(CRN))
                        .build())
                .build();

        service.trackCourtCaseDefendantEvent(TelemetryEventType.DEFENDANT_LINKED, defendant, "caseId");

        var properties = Map.of("crn", CRN, "pnc", PNC, "caseId", "caseId", "defendantId", DEFENDANT_ID);

        verify(telemetryClient).trackEvent(TelemetryEventType.DEFENDANT_LINKED.eventName, properties, Collections.emptyMap());
    }

    @Test
    void givenNoOffender_whenTrackDefendantEvent_thenCallService() {

        var defendant = HearingDefendantEntity.builder()
                .defendant(DefendantEntity.builder()
                        .defendantId(DEFENDANT_ID)
                        .pnc(PNC)
                        .build())
                .build();

        service.trackCourtCaseDefendantEvent(TelemetryEventType.DEFENDANT_UNLINKED, defendant, "caseId");

        var properties = Map.of("caseId", "caseId", "defendantId", DEFENDANT_ID, "pnc", PNC);

        verify(telemetryClient).trackEvent(TelemetryEventType.DEFENDANT_UNLINKED.eventName, properties, Collections.emptyMap());
    }

    private HearingEntity buildHearing() {
        return HearingEntity.builder()
                .hearingId(HEARING_ID)
                .courtCase(CourtCaseEntity.builder()
                        .caseId(CASE_ID)
                        .sourceType(LIBRA)
                        .build())
                .hearingDays(List.of(
                        firstHearing,
                        secondHearing
                ))
                .build();
    }
}
