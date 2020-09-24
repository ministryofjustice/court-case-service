package uk.gov.justice.probation.courtcaseservice.service;

import com.microsoft.applicationinsights.TelemetryClient;
import lombok.AllArgsConstructor;
import org.hibernate.LazyInitializationException;
import org.springframework.stereotype.Service;
import uk.gov.justice.probation.courtcaseservice.controller.mapper.CourtCaseResponseMapper;
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
        try {
            ofNullable(courtCaseEntity.getGroupedOffenderMatches())
                    .map(CourtCaseResponseMapper::calculateNumberOfPossibleMatches)
                    .ifPresent((matchCount) -> properties.put("matches", matchCount.toString()));
        } catch (LazyInitializationException e) {
            // GroupedOffenderMatches are lazily loaded, this exception indicates that the DB session has already
            // been closed. We only care about the number of matches if it has changed, in which case it would have been
            // fetched before the session was closed - so we can safely ignore this.
        }

        Optional.ofNullable(requestProperties.get("username"))
                .ifPresent((caseNo) -> properties.put("username", caseNo));
        Optional.ofNullable(requestProperties.get("clientId"))
                .ifPresent((caseNo) -> properties.put("clientId", caseNo));

        telemetryClient.trackEvent(eventType.eventName, properties, Collections.emptyMap());
    }

}
