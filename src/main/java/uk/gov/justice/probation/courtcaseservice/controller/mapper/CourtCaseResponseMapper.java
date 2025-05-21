package uk.gov.justice.probation.courtcaseservice.controller.mapper;

import lombok.extern.slf4j.Slf4j;
import org.jetbrains.annotations.NotNull;
import uk.gov.justice.probation.courtcaseservice.controller.model.CaseCommentResponse;
import uk.gov.justice.probation.courtcaseservice.controller.model.CaseDocumentResponse;
import uk.gov.justice.probation.courtcaseservice.controller.model.CaseMarker;
import uk.gov.justice.probation.courtcaseservice.controller.model.CourtCaseResponse;
import uk.gov.justice.probation.courtcaseservice.controller.model.CourtCaseResponse.CourtCaseResponseBuilder;
import uk.gov.justice.probation.courtcaseservice.controller.model.HearingPrepStatus;
import uk.gov.justice.probation.courtcaseservice.controller.model.OffenceResponse;
import uk.gov.justice.probation.courtcaseservice.controller.model.PhoneNumber;
import uk.gov.justice.probation.courtcaseservice.jpa.entity.*;
import uk.gov.justice.probation.courtcaseservice.service.model.CaseProgressHearing;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@Slf4j
public class CourtCaseResponseMapper {

    public static CourtCaseResponse mapFrom(HearingEntity hearingEntity, String defendantId, int matchCount, List<CaseProgressHearing> caseHearings) {
        // Core case-based
        final var builder = CourtCaseResponse.builder()
                .hearings(caseHearings);

        buildCaseFields(builder, hearingEntity, defendantId);
        buildHearings(builder, hearingEntity, null);
        builder.files(mapCaseDocuments(hearingEntity, defendantId));

        Optional.ofNullable(hearingEntity.getHearingDefendants()).orElse(Collections.emptyList())
                .stream()
                .filter(defendant -> defendantId.equalsIgnoreCase(defendant.getDefendant().getDefendantId()))
                .findFirst()
                .ifPresentOrElse((matchedDefendant) -> addDefendantFields(builder, matchedDefendant),
                        () -> log.error("Couldn't find defendant ID {} for case ID {} when mapping response.", defendantId, hearingEntity.getCaseId()));

        builder.numberOfPossibleMatches(matchCount);

        return builder.build();
    }

    private static List<CaseDocumentResponse> mapCaseDocuments(HearingEntity hearingEntity, String defendantId) {
        return hearingEntity.getCourtCase().getCaseDefendant(defendantId)
            .map(CaseDefendantEntity::getDocuments)
            .map(caseDefendantDocumentEntities -> caseDefendantDocumentEntities.stream()
                .map(doc -> new CaseDocumentResponse(doc.getDocumentId(),doc.getCreated(), new CaseDocumentResponse.FileResponse(doc.getDocumentName(), 0)))
                .collect(Collectors.toList())
            ).orElse(Collections.emptyList());
    }

    public static CourtCaseResponse mapFrom(HearingEntity hearingEntity, HearingDefendantEntity defendantEntity, int matchCount, LocalDate hearingDate) {
        // Core case-based
        final var builder = CourtCaseResponse.builder();

        buildCaseFields(builder, hearingEntity, defendantEntity.getDefendantId());
        buildHearings(builder, hearingEntity, hearingDate);

        // Defendant-based fields
        addDefendantFields(builder, defendantEntity);
        builder.numberOfPossibleMatches(matchCount);

        return builder.build();
    }

    private static void buildCaseFields(CourtCaseResponseBuilder builder, HearingEntity hearingEntity, String defendantId) {
        // Case-based fields
        builder.caseId(hearingEntity.getCaseId())
                .hearingType(hearingEntity.getHearingType())
                .hearingEventType(hearingEntity.getHearingEventType())
                .hearingId(hearingEntity.getHearingId())
                .urn(hearingEntity.getCourtCase().getUrn())
                .source(hearingEntity.getSourceType().name())
                .createdToday(LocalDate.now().isEqual(Optional.ofNullable(hearingEntity.getFirstCreated()).orElse(LocalDateTime.now()).toLocalDate()))
                .caseMarkers(buildCaseMarkers(hearingEntity))
                .caseComments(buildCaseComments(hearingEntity));

        if (SourceType.LIBRA == hearingEntity.getSourceType()) {
            builder.caseNo(hearingEntity.getCaseNo());
        }
    }

    @NotNull
    private static List<CaseCommentResponse> buildCaseComments(HearingEntity hearingEntity) {
        return Optional.ofNullable(hearingEntity.getCourtCase().getCaseComments())
                .map(caseCommentEntities -> caseCommentEntities.stream().map(CaseCommentResponse::of).collect(Collectors.toList()))
                .orElse(Collections.emptyList());
    }

    private static List<CaseMarker> buildCaseMarkers(HearingEntity hearingEntity) {
        return Optional.ofNullable(hearingEntity.getCourtCase().getCaseMarkers())
                .map(caseMarkerEntities -> caseMarkerEntities.stream().map(CaseMarkerEntity::of).collect(Collectors.toList()))
                .orElse(Collections.emptyList());
    }

    static void buildHearings(CourtCaseResponseBuilder builder, HearingEntity hearingEntity, LocalDate hearingDate) {
        var hearings = Optional.ofNullable(hearingEntity.getHearingDays())
                .orElseThrow();

        var targetHearing = hearings
                .stream()
                .filter(hearingDayEntity -> hearingDate == null || hearingDate.isEqual(hearingDayEntity.getDay()))
                .findFirst()
                .orElseThrow();

        // Populate the top level fields with the details from the single hearing
        builder.courtCode(targetHearing.getCourtCode())
                .courtRoom(getNormalisedCourtRoom(targetHearing.getCourtRoom()))
                .sessionStartTime(targetHearing.getSessionStartTime())
                .session(targetHearing.getSession())
                .listNo(hearingEntity.getListNo());
    }

    private static String getNormalisedCourtRoom(String courtRoom) {
        return courtRoom.contains("Courtroom") ? courtRoom.replaceAll("[a-zA-Z 0]", "") : courtRoom.replace("([0]*)?", "");
    }

    private static List<OffenceResponse> mapOffencesFromDefendantOffences(List<OffenceEntity> offenceEntities) {
        return Optional.ofNullable(offenceEntities).orElse(Collections.emptyList())
                .stream()
                .sorted(Comparator.comparing(offenceEntity ->
                        // Default to very high number so that unordered items are last
                        (offenceEntity.getSequence() != null ? offenceEntity.getSequence() : Integer.MAX_VALUE)))
                .map(CourtCaseResponseMapper::mapFrom)
                .collect(Collectors.toList());
    }

    private static OffenceResponse mapFrom(OffenceEntity offenceEntity) {
        return OffenceResponse.builder()
                .offenceTitle(offenceEntity.getTitle())
                .offenceCode(offenceEntity.getOffenceCode())
                .offenceSummary(offenceEntity.getSummary())
                .act(offenceEntity.getAct())
                .sequenceNumber(offenceEntity.getSequence())
                .listNo(offenceEntity.getListNo())
                .offenceCode(offenceEntity.getOffenceCode())
                .plea(Optional.ofNullable(offenceEntity.getPlea()).map(PleaEntity::of).orElse(null))
                .verdict(Optional.ofNullable(offenceEntity.getVerdict()).map(VerdictEntity::of).orElse(null))
                .build();
    }

    private static void addDefendantFields(CourtCaseResponseBuilder builder, HearingDefendantEntity hearingDefendantEntity) {
        final var defendant = hearingDefendantEntity.getDefendant();
        addOffenderFields(builder, defendant.getOffender());
        builder
                .defendantName(defendant.getDefendantName())
                .defendantSurname(defendant.getDefendantSurname())
                .defendantForename(defendant.getName().getForename1())
                .name(defendant.getName())
                .defendantAddress(defendant.getAddress())
                .defendantDob(defendant.getDateOfBirth())
                .defendantSex(defendant.getSex())
                .defendantType(defendant.getType())
                .defendantId(defendant.getDefendantId())
                .phoneNumber(PhoneNumber.of(defendant.getPhoneNumber()))
                .nationality1(defendant.getNationality1())
                .nationality2(defendant.getNationality2())
                .cro(defendant.getCro())
                .pnc(defendant.getPnc())
                .crn(hearingDefendantEntity.getCrn())
                .probationStatus(hearingDefendantEntity.getProbationStatusForDisplay())
                .confirmedOffender(defendant.isOffenderConfirmed())
                .personId(defendant.getPersonId())
                .hearingPrepStatus(HearingPrepStatus.valueOf(hearingDefendantEntity.getPrepStatus()))
        ;

        // Offences
        builder.offences(mapOffencesFromDefendantOffences(hearingDefendantEntity.getOffences()));
    }

    private static void addOffenderFields(CourtCaseResponseBuilder builder, OffenderEntity offender) {
        builder
                .awaitingPsr(Optional.ofNullable(offender)
                        .map(OffenderEntity::getAwaitingPsr)
                        .orElse(null))
                .breach(Optional.ofNullable(offender)
                        .map(OffenderEntity::isBreach)
                        .orElse(null))
                .preSentenceActivity(Optional.ofNullable(offender)
                        .map(OffenderEntity::isPreSentenceActivity)
                        .orElse(null))
                .suspendedSentenceOrder(Optional.ofNullable(offender)
                        .map(OffenderEntity::isSuspendedSentenceOrder)
                        .orElse(null))
                .previouslyKnownTerminationDate(Optional.ofNullable(offender)
                        .map(OffenderEntity::getPreviouslyKnownTerminationDate)
                        .orElse(null))
        ;
    }
}
