package uk.gov.justice.probation.courtcaseservice.service;

import com.microsoft.applicationinsights.TelemetryClient;
import lombok.AllArgsConstructor;
import org.springframework.stereotype.Service;
import uk.gov.justice.probation.courtcaseservice.application.ClientDetails;
import uk.gov.justice.probation.courtcaseservice.jpa.entity.*;

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

    void trackCourtCaseEvent(TelemetryEventType eventType, HearingEntity hearingEntity) {

        Map<String, String> properties = new HashMap<>();

        ofNullable(hearingEntity)
                .map(HearingEntity::getCourtCase)
                .map(CourtCaseEntity::getCaseId)
                .ifPresent((caseId) -> properties.put("caseId", caseId));

        ofNullable(hearingEntity)
                .map(HearingEntity::getHearingId)
                .ifPresent((hearingId) -> properties.put("hearingId", hearingId));

        ofNullable(hearingEntity.getSourceType())
                .ifPresent((sourceType) -> properties.put("source", sourceType.name()));

        ofNullable(hearingEntity.getHearingDays())
                .ifPresent(hearings -> {
                    final var hearingsString = hearings.stream()
                            .map(HearingDayEntity::loggableString)
                            .collect(Collectors.joining(","));
                    properties.put("hearings", hearingsString);
                });

        addRequestProperties(properties);

        telemetryClient.trackEvent(eventType.eventName, properties, Collections.emptyMap());
    }

    void trackCourtCaseCommentEvent(TelemetryEventType eventType, CaseCommentEntity caseCommentEntity) {

        Map<String, String> properties = new HashMap<>();

        properties.put("caseId", caseCommentEntity.getCaseId());
        properties.put("createdByUuid", caseCommentEntity.getCreatedByUuid());
        properties.put("commentId", caseCommentEntity.getId().toString());
        properties.put("createdDateTime", caseCommentEntity.getCreated().toString());
        properties.put("username", caseCommentEntity.getCreatedBy());

        addRequestProperties(properties);

        telemetryClient.trackEvent(eventType.eventName, properties, Collections.emptyMap());
    }

    void trackCreateHearingNoteEvent(TelemetryEventType eventType, HearingNoteEntity hearingNoteEntity) {

        Map<String, String> properties = new HashMap<>();

        properties.put("hearingId", hearingNoteEntity.getHearingId());
        properties.put("createdByUuid", hearingNoteEntity.getCreatedByUuid());
        properties.put("noteId", hearingNoteEntity.getId().toString());
        properties.put("createdDateTime", hearingNoteEntity.getCreated().toString());
        properties.put("username", hearingNoteEntity.getCreatedBy());

        addRequestProperties(properties);

        telemetryClient.trackEvent(eventType.eventName, properties, Collections.emptyMap());
    }

    void trackCourtCaseDefendantEvent(TelemetryEventType eventType, HearingDefendantEntity defendantEntity, String caseId) {

        Map<String, String> properties = new HashMap<>();

        ofNullable(defendantEntity.getDefendant().getDefendantId())
            .ifPresent(id -> properties.put("defendantId", id));
        ofNullable(defendantEntity.getDefendant().getOffender())
            .ifPresent(offender -> properties.put("crn", offender.getCrn()));
        ofNullable(defendantEntity.getDefendant().getPnc())
            .ifPresent(pnc -> properties.put("pnc", pnc));
        ofNullable(caseId)
            .ifPresent(id -> properties.put("caseId", id));

        addRequestProperties(properties);

        telemetryClient.trackEvent(eventType.eventName, properties, Collections.emptyMap());
    }

    public void trackMatchEvent(TelemetryEventType eventType, OffenderMatchEntity matchEntity, HearingEntity hearingEntity, String defendantId) {

        Map<String, String> properties = new HashMap<>();

        properties.put("defendantId", defendantId);
        Optional<HearingEntity> hearingEntityNullable = ofNullable(hearingEntity);
        hearingEntityNullable
                .map(HearingEntity::getCaseId)
                .ifPresent((caseId) -> {
                    properties.put("caseId", caseId);
                });
        hearingEntityNullable
                .map(HearingEntity::getSourceType)
                .ifPresent((sourceType) -> {
                    properties.put("source", sourceType.name());
                });
        hearingEntityNullable
                .map(HearingEntity::getHearingId)
                .ifPresent((hearingId) -> properties.put("hearingId", hearingId));
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
