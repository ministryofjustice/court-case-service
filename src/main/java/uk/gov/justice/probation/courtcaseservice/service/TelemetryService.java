package uk.gov.justice.probation.courtcaseservice.service;

import com.microsoft.applicationinsights.TelemetryClient;
import lombok.AllArgsConstructor;
import org.springframework.stereotype.Service;
import uk.gov.justice.probation.courtcaseservice.application.ClientDetails;
import uk.gov.justice.probation.courtcaseservice.jpa.dto.HearingCourtCaseDTO;
import uk.gov.justice.probation.courtcaseservice.jpa.entity.CaseCommentEntity;
import uk.gov.justice.probation.courtcaseservice.jpa.entity.CourtCaseEntity;
import uk.gov.justice.probation.courtcaseservice.jpa.entity.DefendantEntity;
import uk.gov.justice.probation.courtcaseservice.jpa.entity.GroupedOffenderMatchesEntity;
import uk.gov.justice.probation.courtcaseservice.jpa.entity.HearingDayEntity;
import uk.gov.justice.probation.courtcaseservice.jpa.entity.HearingDefendantEntity;
import uk.gov.justice.probation.courtcaseservice.jpa.entity.HearingEntity;
import uk.gov.justice.probation.courtcaseservice.jpa.entity.HearingNoteEntity;
import uk.gov.justice.probation.courtcaseservice.jpa.entity.OffenderEntity;
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
        properties.put("defendantId", caseCommentEntity.getDefendantId());
        properties.put("createdDateTime", caseCommentEntity.getCreated().toString());
        properties.put("username", caseCommentEntity.getCreatedBy());

        addRequestProperties(properties);

        telemetryClient.trackEvent(eventType.eventName, properties, Collections.emptyMap());
    }

    void trackCreateHearingNoteEvent(HearingNoteEntity hearingNoteEntity) {

        trackHearingNoteEvent(TelemetryEventType.HEARING_NOTE_ADDED, hearingNoteEntity);
    }

    void trackDeleteHearingNoteEvent(HearingNoteEntity hearingNoteEntity) {

        trackHearingNoteEvent(TelemetryEventType.HEARING_NOTE_DELETED, hearingNoteEntity);
    }

    void trackUpdateHearingNoteEvent(HearingNoteEntity hearingNoteEntity) {

        trackHearingNoteEvent(TelemetryEventType.HEARING_NOTE_UPDATED, hearingNoteEntity);
    }

    private void trackHearingNoteEvent(TelemetryEventType eventType, HearingNoteEntity hearingNoteEntity) {
        Map<String, String> properties = new HashMap<>();
        properties.put("hearingId", hearingNoteEntity.getHearingId());
        properties.put("defendantId", Optional.ofNullable(hearingNoteEntity.getHearingDefendant()).map(HearingDefendantEntity::getDefendantId).orElse(null));
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
                .ifPresent((caseId) -> properties.put("caseId", caseId));
        hearingEntityNullable
                .map(HearingEntity::getSourceType)
                .ifPresent((sourceType) -> properties.put("source", sourceType.name()));
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

    public void trackOffenderProbationStatusUpdateEvent(OffenderEntity offenderEntity) {

        Map<String, String> properties = new HashMap<>();

        ofNullable(offenderEntity)
                .map(OffenderEntity::getCrn)
                .ifPresent((crn) -> properties.put("crn", crn));

        ofNullable(offenderEntity)
                .map(OffenderEntity::getProbationStatus)
                .ifPresent((probationStatus) -> properties.put("status", probationStatus.getName()));

        ofNullable(offenderEntity)
                .map(OffenderEntity::getPreviouslyKnownTerminationDate)
                .ifPresent((date) -> properties.put("previouslyKnownTerminationDate", date.toString()));

        ofNullable(offenderEntity)
                .map(OffenderEntity::isBreach)
                .ifPresent((isBreach) -> properties.put("inBreach", isBreach.toString()));

        ofNullable(offenderEntity)
                .map(OffenderEntity::isPreSentenceActivity)
                .ifPresent((preSentenceActivity) -> properties.put("preSentenceActivity", preSentenceActivity.toString()));

        ofNullable(offenderEntity)
                .map(OffenderEntity::getAwaitingPsr)
                .ifPresent((awaitingPsr) -> properties.put("awaitingPsr", awaitingPsr.toString()));

        addRequestProperties(properties);

        telemetryClient.trackEvent(TelemetryEventType.OFFENDER_PROBATION_STATUS_UPDATED.eventName, properties, Collections.emptyMap());
    }


    public void trackOffenderProbationStatusNotUpdateEvent(OffenderEntity offenderEntity) {

        Map<String, String> properties = new HashMap<>();

        ofNullable(offenderEntity)
                .map(OffenderEntity::getCrn)
                .ifPresent((crn) -> properties.put("crn", crn));

        ofNullable(offenderEntity)
                .map(OffenderEntity::getProbationStatus)
                .ifPresent((probationStatus) -> properties.put("status", probationStatus.getName()));

        ofNullable(offenderEntity)
                .map(OffenderEntity::getPreviouslyKnownTerminationDate)
                .ifPresent((date) -> properties.put("previouslyKnownTerminationDate", date.toString()));

        ofNullable(offenderEntity)
                .map(OffenderEntity::isBreach)
                .ifPresent((isBreach) -> properties.put("inBreach", isBreach.toString()));

        ofNullable(offenderEntity)
                .map(OffenderEntity::isPreSentenceActivity)
                .ifPresent((preSentenceActivity) -> properties.put("preSentenceActivity", preSentenceActivity.toString()));

        ofNullable(offenderEntity)
                .map(OffenderEntity::getAwaitingPsr)
                .ifPresent((awaitingPsr) -> properties.put("awaitingPsr", awaitingPsr.toString()));

        addRequestProperties(properties);

        telemetryClient.trackEvent(TelemetryEventType.OFFENDER_PROBATION_STATUS_NOT_UPDATED.eventName, properties, Collections.emptyMap());
    }

    public void trackPiCNewEngagementDefendantLinkedEvent(DefendantEntity defendantEntity) {
        Map<String, String> properties = new HashMap<>();

        ofNullable(defendantEntity)
                .map(DefendantEntity::getCrn)
                .ifPresent((crn) -> properties.put("crn", crn));

        ofNullable(defendantEntity)
                .map(DefendantEntity::getDefendantId)
                .ifPresent((defendantId) -> properties.put("defendantId", defendantId));

        ofNullable(defendantEntity)
                .map(DefendantEntity::getPnc)
                .ifPresent((pnc) -> properties.put("pnc", pnc));

        addRequestProperties(properties);

        telemetryClient.trackEvent(TelemetryEventType.PIC_NEW_ENGAGEMENT_DEFENDANT_LINKED.eventName, properties, Collections.emptyMap());

    }

    public void trackMoveUnResultedCasesToOutcomesFlowJob(int recordsProcessed, List<String> courts, Exception error) {
        Map<String, String> properties = new HashMap<>();

        properties.put("recordsProcessed", String.valueOf(recordsProcessed));

        ofNullable(courts)
                .ifPresent(c -> properties.put("courts", c.toString()));

        ofNullable(error)
                .ifPresent(e -> properties.put("error", e.getMessage()));

        addRequestProperties(properties);

        telemetryClient.trackEvent(TelemetryEventType.PIC_MOVE_UN_RESULTED_CASES_TO_OUTCOMES_WORKFLOW.eventName, properties, Collections.emptyMap());
    }

    public void trackMoveToResultedUnAuthorisedEvent(String hearingId, String defendantId, String userUuid, String assignedUuid, String userId, String userName, String authSource) {
        Map<String, String> properties = new HashMap<>();
        properties.put("hearingId", String.valueOf(hearingId));
        properties.put("defendantId", String.valueOf(defendantId));
        properties.put("userUuid", String.valueOf(userUuid));
        properties.put("assignedUuid", String.valueOf(assignedUuid));
        properties.put("userId", String.valueOf(userId));
        properties.put("userName", String.valueOf(userName));
        properties.put("authSource", String.valueOf(authSource));

        telemetryClient.trackEvent(TelemetryEventType.PIC_RESULT_OUTCOME_NOT_ASSIGNED_TO_CURRENT_USER.eventName, properties, Collections.emptyMap());
    }

    void trackDeleteHearingEvent(TelemetryEventType eventType, HearingCourtCaseDTO hearing, Boolean dryRun) {

        Map<String, String> properties = new HashMap<>();

        ofNullable(hearing.getHearingId())
                .ifPresent(id -> properties.put("hearingId", id));
        ofNullable(hearing.getCaseId())
                .ifPresent(caseId -> properties.put("caseId", caseId));
        ofNullable(dryRun)
                .ifPresent(isEnabled -> properties.put("dryRun", isEnabled.toString()));

        telemetryClient.trackEvent(eventType.eventName, properties, Collections.emptyMap());
    }

    private void addRequestProperties(Map<String, String> properties) {
        Optional.ofNullable(clientDetails.getUsername())
                .ifPresent((caseNo) -> properties.put("username", caseNo));
        Optional.ofNullable(clientDetails.getClientId())
                .ifPresent((caseNo) -> properties.put("clientId", caseNo));
    }
}
