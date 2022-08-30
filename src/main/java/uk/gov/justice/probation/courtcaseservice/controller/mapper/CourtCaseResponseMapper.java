package uk.gov.justice.probation.courtcaseservice.controller.mapper;

import lombok.extern.slf4j.Slf4j;
import org.jetbrains.annotations.NotNull;
import uk.gov.justice.probation.courtcaseservice.controller.model.CaseCommentResponse;
import uk.gov.justice.probation.courtcaseservice.controller.model.CourtCaseResponse;
import uk.gov.justice.probation.courtcaseservice.controller.model.CourtCaseResponse.CourtCaseResponseBuilder;
import uk.gov.justice.probation.courtcaseservice.controller.model.HearingResponse;
import uk.gov.justice.probation.courtcaseservice.controller.model.OffenceResponse;
import uk.gov.justice.probation.courtcaseservice.controller.model.PhoneNumber;
import uk.gov.justice.probation.courtcaseservice.jpa.entity.HearingDefendantEntity;
import uk.gov.justice.probation.courtcaseservice.jpa.entity.HearingEntity;
import uk.gov.justice.probation.courtcaseservice.jpa.entity.OffenceEntity;
import uk.gov.justice.probation.courtcaseservice.jpa.entity.OffenderEntity;
import uk.gov.justice.probation.courtcaseservice.jpa.entity.SourceType;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@Slf4j
public class CourtCaseResponseMapper {

    public static CourtCaseResponse mapFrom(HearingEntity hearingEntity, String defendantId, int matchCount) {
        // Core case-based
        final var builder = CourtCaseResponse.builder();

        buildCaseFields(builder, hearingEntity);
        buildHearings(builder, hearingEntity, null);

        Optional.ofNullable(hearingEntity.getHearingDefendants()).orElse(Collections.emptyList())
                    .stream()
                    .filter(defendant -> defendantId.equalsIgnoreCase(defendant.getDefendant().getDefendantId()))
                    .findFirst()
                    .ifPresentOrElse((matchedDefendant) -> addDefendantFields(builder, matchedDefendant),
                            () -> log.error("Couldn't find defendant ID {} for case ID {} when mapping response.", defendantId, hearingEntity.getCaseId()));

        builder.numberOfPossibleMatches(matchCount);

        return builder.build();
    }

    public static CourtCaseResponse mapFrom(HearingEntity hearingEntity, HearingDefendantEntity defendantEntity, int matchCount, LocalDate hearingDate) {
        // Core case-based
        final var builder = CourtCaseResponse.builder();

        buildCaseFields(builder, hearingEntity);
        buildHearings(builder, hearingEntity, hearingDate);

        // Defendant-based fields
        addDefendantFields(builder, defendantEntity);
        builder.numberOfPossibleMatches(matchCount);

        return builder.build();
    }

    private static void buildCaseFields(CourtCaseResponseBuilder builder, HearingEntity hearingEntity) {
        // Case-based fields
        builder.caseId(hearingEntity.getCaseId())
            .hearingType(hearingEntity.getHearingType())
            .hearingId(hearingEntity.getHearingId())
            .urn(hearingEntity.getCourtCase().getUrn())
            .source(hearingEntity.getSourceType().name())
            .createdToday(LocalDate.now().isEqual(Optional.ofNullable(hearingEntity.getFirstCreated()).orElse(LocalDateTime.now()).toLocalDate()))
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
                .listNo(targetHearing.getListNo());

        builder.hearings(
            hearings.stream()
                .map(hearingDayEntity -> HearingResponse.builder()
                    .courtCode(hearingDayEntity.getCourtCode())
                    .courtRoom(hearingDayEntity.getCourtRoom())
                    .listNo(hearingDayEntity.getListNo())
                    .session(hearingDayEntity.getSession())
                    .sessionStartTime(hearingDayEntity.getSessionStartTime())
                    .build())
            .collect(Collectors.toList()));
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
            .offenceSummary(offenceEntity.getSummary())
            .act(offenceEntity.getAct())
            .sequenceNumber(offenceEntity.getSequence())
            .listNo(offenceEntity.getListNo())
            .build();
    }

    private static void addDefendantFields(CourtCaseResponseBuilder builder, HearingDefendantEntity hearingDefendantEntity) {
        final var defendant = hearingDefendantEntity.getDefendant();
        addOffenderFields(builder, defendant.getOffender());
        builder
            .defendantName(defendant.getDefendantName())
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
