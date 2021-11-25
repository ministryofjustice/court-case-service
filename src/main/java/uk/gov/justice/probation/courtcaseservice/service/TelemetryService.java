package uk.gov.justice.probation.courtcaseservice.service;

import com.microsoft.applicationinsights.TelemetryClient;
import lombok.AllArgsConstructor;
import org.springframework.stereotype.Service;
import uk.gov.justice.probation.courtcaseservice.application.ClientDetails;
import uk.gov.justice.probation.courtcaseservice.jpa.entity.CourtCaseEntity;
import uk.gov.justice.probation.courtcaseservice.jpa.entity.DefendantEntity;
import uk.gov.justice.probation.courtcaseservice.jpa.entity.GroupedOffenderMatchesEntity;
import uk.gov.justice.probation.courtcaseservice.jpa.entity.HearingEntity;
import uk.gov.justice.probation.courtcaseservice.jpa.entity.OffenderMatchEntity;

import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Collectors;

import static java.util.Optional.ofNullable;

@Service
@AllArgsConstructor
public class TelemetryService {
    private final TelemetryClient telemetryClient;

    private final ClientDetails clientDetails;

    void trackApplicationDegradationEvent(String description, Exception exception, String crn) {

        Map<String, String> properties = new HashMap<>(3);
        properties.put("description", description);
        properties.put("crn", crn);
        ofNullable(exception).ifPresent((code) -> properties.put("cause", exception.getMessage()));
        telemetryClient.trackEvent(TelemetryEventType.GRACEFUL_DEGRADE.eventName, properties, Collections.emptyMap());
    }

    void trackCourtCaseEvent(TelemetryEventType eventType, CourtCaseEntity courtCaseEntity) {

        Map<String, String> properties = new HashMap<>();

        ofNullable(courtCaseEntity.getCaseId())
                .ifPresent((caseId) -> properties.put("caseId", caseId));

        ofNullable(courtCaseEntity.getHearings())
                .ifPresent(hearings -> {
                    final var hearingsString = hearings.stream()
                            .map(HearingEntity::loggableString)
                            .collect(Collectors.joining(","));
                    properties.put("hearings", hearingsString);
                });

        addRequestProperties(properties);

        telemetryClient.trackEvent(eventType.eventName, properties, Collections.emptyMap());
    }

    void trackCourtCaseDefendantEvent(TelemetryEventType eventType, DefendantEntity defendantEntity, String caseId) {

        Map<String, String> properties = new HashMap<>();

        ofNullable(defendantEntity.getDefendantId())
            .ifPresent(id -> properties.put("defendantId", id));
        ofNullable(defendantEntity.getOffender())
            .ifPresent(offender -> properties.put("crn", offender.getCrn()));
        ofNullable(defendantEntity.getPnc())
            .ifPresent(pnc -> properties.put("pnc", pnc));
        ofNullable(caseId)
            .ifPresent(id -> properties.put("caseId", id));

        addRequestProperties(properties);

        telemetryClient.trackEvent(eventType.eventName, properties, Collections.emptyMap());
    }

    public void trackMatchEvent(TelemetryEventType eventType, OffenderMatchEntity matchEntity, CourtCaseEntity courtCaseEntity, String defendantId) {

        Map<String, String> properties = new HashMap<>();

        properties.put("defendantId", defendantId);
        ofNullable(courtCaseEntity)
                .map(CourtCaseEntity::getCaseId)
                .ifPresent((caseId) -> properties.put("caseId", caseId));
        ofNullable(matchEntity)
                .map(OffenderMatchEntity::getPnc)
                .ifPresent((pnc) -> properties.put("pnc", pnc));
        ofNullable(matchEntity)
                .map(OffenderMatchEntity::getCrn)
                .ifPresent((crn) -> properties.put("crn", crn));
        ofNullable(matchEntity)
                .map(OffenderMatchEntity::getGroup)
                .map(GroupedOffenderMatchesEntity::getOffenderMatches)
                .map(List::size)
                .ifPresent((matches) -> properties.put("matches", matches.toString()));

        addRequestProperties(properties);

        telemetryClient.trackEvent(eventType.eventName, properties, Collections.emptyMap());
    }

    private void addRequestProperties(Map<String, String> properties) {
        Optional.ofNullable(clientDetails.getUsername())
                .ifPresent((caseNo) -> properties.put("username", caseNo));
        Optional.ofNullable(clientDetails.getClientId())
                .ifPresent((caseNo) -> properties.put("clientId", caseNo));
    }
}
