package uk.gov.justice.probation.courtcaseservice.service;

import com.microsoft.applicationinsights.TelemetryClient;
import lombok.AllArgsConstructor;
import org.springframework.stereotype.Service;
import uk.gov.justice.probation.courtcaseservice.jpa.entity.CourtCaseEntity;
import uk.gov.justice.probation.courtcaseservice.jpa.entity.GroupedOffenderMatchesEntity;
import uk.gov.justice.probation.courtcaseservice.jpa.entity.OffenderMatchEntity;

import java.time.format.DateTimeFormatter;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

import static java.util.Optional.ofNullable;

@Service
@AllArgsConstructor
public class TelemetryService {
    private final TelemetryClient telemetryClient;

    private final Map<String, String> requestProperties;

    void trackCourtCaseEvent(TelemetryEventType eventType, CourtCaseEntity courtCaseEntity) {

        Map<String, String> properties = new HashMap<>();

        ofNullable(courtCaseEntity.getCourtCode())
                .ifPresent((code) -> properties.put("courtCode", code));
        ofNullable(courtCaseEntity.getCourtRoom())
                .ifPresent((room) -> properties.put("courtRoom", room));
        ofNullable(courtCaseEntity.getSessionStartTime())
                .map(date -> date.format(DateTimeFormatter.ISO_DATE))
                .ifPresent((date) -> properties.put("hearingDate", date));
        ofNullable(courtCaseEntity.getCrn())
                .ifPresent((crn) -> properties.put("crn", crn));
        ofNullable(courtCaseEntity.getPnc())
                .ifPresent((pnc) -> properties.put("pnc", pnc));
        ofNullable(courtCaseEntity.getCaseNo())
                .ifPresent((caseNo) -> properties.put("caseNo", caseNo));

        addRequestProperties(properties);

        telemetryClient.trackEvent(eventType.eventName, properties, Collections.emptyMap());
    }

    public void trackMatchEvent(TelemetryEventType eventType, OffenderMatchEntity matchEntity) {

        Map<String, String> properties = new HashMap<>();

        ofNullable(matchEntity)
                .map(OffenderMatchEntity::getGroup)
                .map(GroupedOffenderMatchesEntity::getCourtCase)
                .map(CourtCaseEntity::getCourtCode)
                .ifPresent((code) -> properties.put("courtCode", code));
        ofNullable(matchEntity)
                .map(OffenderMatchEntity::getGroup)
                .map(GroupedOffenderMatchesEntity::getCourtCase)
                .map(CourtCaseEntity::getCaseNo)
                .ifPresent((caseNo) -> properties.put("caseNo", caseNo));
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
        Optional.ofNullable(requestProperties.get("username"))
                .ifPresent((caseNo) -> properties.put("username", caseNo));
        Optional.ofNullable(requestProperties.get("clientId"))
                .ifPresent((caseNo) -> properties.put("clientId", caseNo));
    }
}
