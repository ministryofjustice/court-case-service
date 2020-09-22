package uk.gov.justice.probation.courtcaseservice.service;

import com.microsoft.applicationinsights.TelemetryClient;
import lombok.AllArgsConstructor;
import org.springframework.stereotype.Service;
import uk.gov.justice.probation.courtcaseservice.jpa.entity.CourtCaseEntity;

import java.time.format.DateTimeFormatter;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;
import java.util.Optional;

import static java.util.Optional.ofNullable;

@Service
@AllArgsConstructor
public class TelemetryService {
    private final TelemetryClient telemetryClient;

    private final Map<String, String> requestProperties;

    void trackCourtCaseEvent(String eventName, CourtCaseEntity courtCaseEntity) {

        Map<String, String> properties = new HashMap<>();

        ofNullable(courtCaseEntity.getCourtCode())
                .ifPresent((code) -> properties.put("courtCode", code));
        ofNullable(courtCaseEntity.getCourtRoom())
                .ifPresent((room) -> properties.put("courtRoom", room));
        ofNullable(courtCaseEntity.getSessionStartTime())
                .map(date -> date.format(DateTimeFormatter.ISO_DATE))
                .ifPresent((date) -> properties.put("hearingDate", date));
        ofNullable(courtCaseEntity.getCaseNo())
                .ifPresent((caseNo) -> properties.put("caseNo", caseNo));

        Optional.ofNullable(requestProperties.get("username"))
                .ifPresent((caseNo) -> properties.put("username", caseNo));

        Optional.ofNullable(requestProperties.get("clientId"))
                .ifPresent((caseNo) -> properties.put("clientId", caseNo));

        telemetryClient.trackEvent(eventName, properties, Collections.emptyMap());
    }

}
