package uk.gov.justice.probation.courtcaseservice.controller.mapper;

import lombok.extern.slf4j.Slf4j;
import uk.gov.justice.probation.courtcaseservice.controller.model.CourtCaseResponse;
import uk.gov.justice.probation.courtcaseservice.controller.model.CourtCaseResponse.CourtCaseResponseBuilder;
import uk.gov.justice.probation.courtcaseservice.controller.model.HearingResponse;
import uk.gov.justice.probation.courtcaseservice.controller.model.OffenceResponse;
import uk.gov.justice.probation.courtcaseservice.controller.model.ProbationStatus;
import uk.gov.justice.probation.courtcaseservice.jpa.entity.CourtCaseEntity;
import uk.gov.justice.probation.courtcaseservice.jpa.entity.DefendantEntity;
import uk.gov.justice.probation.courtcaseservice.jpa.entity.DefendantOffenceEntity;
import uk.gov.justice.probation.courtcaseservice.jpa.entity.OffenceEntity;
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

    public static CourtCaseResponse mapFrom(CourtCaseEntity courtCaseEntity, String defendantId, int matchCount) {
        // Core case-based
        final var builder = CourtCaseResponse.builder();

        buildCaseFields(builder, courtCaseEntity);
        buildHearings(builder, courtCaseEntity, null);

        Optional.ofNullable(courtCaseEntity.getDefendants()).orElse(Collections.emptyList())
                    .stream()
                    .filter(defendant -> defendantId.equalsIgnoreCase(defendant.getDefendantId()))
                    .findFirst()
                    .ifPresentOrElse((matchedDefendant) -> addDefendantFields(builder, matchedDefendant),
                            () -> log.error("Couldn't find defendant ID {} for case ID {} when mapping response.", defendantId, courtCaseEntity.getCaseId()));

        builder.numberOfPossibleMatches(matchCount);

        return builder.build();
    }

    public static CourtCaseResponse mapFrom(CourtCaseEntity courtCaseEntity, DefendantEntity defendantEntity, int matchCount, LocalDate hearingDate) {
        // Core case-based
        final var builder = CourtCaseResponse.builder();

        buildCaseFields(builder, courtCaseEntity);
        buildHearings(builder, courtCaseEntity, hearingDate);

        // Defendant-based fields
        addDefendantFields(builder, defendantEntity);
        builder.numberOfPossibleMatches(matchCount);

        return builder.build();
    }

    private static void buildCaseFields(CourtCaseResponseBuilder builder, CourtCaseEntity courtCaseEntity) {
        // Case-based fields
        builder.caseId(courtCaseEntity.getCaseId())
            .source(courtCaseEntity.getSourceType().name())
            .createdToday(LocalDate.now().isEqual(Optional.ofNullable(courtCaseEntity.getFirstCreated()).orElse(LocalDateTime.now()).toLocalDate()));
        if (SourceType.LIBRA == courtCaseEntity.getSourceType()) {
            builder.caseNo(courtCaseEntity.getCaseNo());
        }
    }

    static void buildHearings(CourtCaseResponseBuilder builder, CourtCaseEntity courtCaseEntity, LocalDate hearingDate) {
        var hearings = Optional.ofNullable(courtCaseEntity.getHearings())
            .orElse(Collections.emptyList());

        var targetHearing = hearings
            .stream()
            .filter(hearingEntity -> hearingDate == null || hearingDate.isEqual(hearingEntity.getHearingDay()))
            .findFirst();

        // Populate the top level fields with the details from the single hearing
        targetHearing.ifPresentOrElse((hearing) ->
                                    builder.courtCode(hearing.getCourtCode())
                                            .courtRoom(hearing.getCourtRoom())
                                            .sessionStartTime(hearing.getSessionStartTime())
                                            .session(hearing.getSession())
                                            .listNo(hearing.getListNo()),
                                () -> {
                                    // This should not happen and will not be possible when we retire COURT_CASE entity fields for hearing-based fields
                                    builder.courtCode(courtCaseEntity.getCourtCode())
                                        .courtRoom(courtCaseEntity.getCourtRoom())
                                        .sessionStartTime(courtCaseEntity.getSessionStartTime())
                                        .session(courtCaseEntity.getSession())
                                        .listNo(courtCaseEntity.getListNo());
                                    log.error("No hearings associated to court case {} ", courtCaseEntity.getCaseId());
                                });

        builder.hearings(
            hearings.stream()
                .map(hearingEntity -> HearingResponse.builder()
                    .courtCode(hearingEntity.getCourtCode())
                    .courtRoom(hearingEntity.getCourtRoom())
                    .listNo(hearingEntity.getListNo())
                    .session(hearingEntity.getSession())
                    .sessionStartTime(hearingEntity.getSessionStartTime())
                    .build())
            .collect(Collectors.toList()));
    }

    static String getDefendantId(List<DefendantEntity> defendantEntities) {
        return Optional.ofNullable(defendantEntities).orElse(Collections.emptyList())
            .stream()
            .findFirst()
            .map(DefendantEntity::getDefendantId)
            .orElse(null);
    }

    private static List<OffenceResponse> mapOffencesFrom(List<OffenceEntity> offenceEntities) {
        return Optional.ofNullable(offenceEntities).orElse(Collections.emptyList())
                .stream()
                .sorted(Comparator.comparing(offenceEntity ->
                        // Default to very high number so that unordered items are last
                        (offenceEntity.getSequenceNumber() != null ? offenceEntity.getSequenceNumber() : Integer.MAX_VALUE)))
                .map(CourtCaseResponseMapper::mapFrom)
                .collect(Collectors.toList());
    }

    private static OffenceResponse mapFrom(OffenceEntity offenceEntity) {
        return OffenceResponse.builder()
                .offenceTitle(offenceEntity.getOffenceTitle())
                .offenceSummary(offenceEntity.getOffenceSummary())
                .act(offenceEntity.getAct())
                .sequenceNumber(offenceEntity.getSequenceNumber())
                .build();
    }

    private static List<OffenceResponse> mapOffencesFromDefendantOffences(List<DefendantOffenceEntity> offenceEntities) {
        return Optional.ofNullable(offenceEntities).orElse(Collections.emptyList())
            .stream()
            .sorted(Comparator.comparing(offenceEntity ->
                // Default to very high number so that unordered items are last
                (offenceEntity.getSequence() != null ? offenceEntity.getSequence() : Integer.MAX_VALUE)))
            .map(CourtCaseResponseMapper::mapFrom)
            .collect(Collectors.toList());
    }

    private static OffenceResponse mapFrom(DefendantOffenceEntity offenceEntity) {
        return OffenceResponse.builder()
            .offenceTitle(offenceEntity.getTitle())
            .offenceSummary(offenceEntity.getSummary())
            .act(offenceEntity.getAct())
            .sequenceNumber(offenceEntity.getSequence())
            .build();
    }

    private static void addDefendantFields(CourtCaseResponseBuilder builder, DefendantEntity defendantEntity) {
        builder.awaitingPsr(defendantEntity.getAwaitingPsr())
            .previouslyKnownTerminationDate(defendantEntity.getPreviouslyKnownTerminationDate())
            .probationStatus(Optional.ofNullable(defendantEntity.getProbationStatus()).map(ProbationStatus::of).orElse(null))
            .suspendedSentenceOrder(defendantEntity.getSuspendedSentenceOrder())
            .breach(defendantEntity.getBreach())
            .preSentenceActivity(defendantEntity.getPreSentenceActivity())
            .defendantName(defendantEntity.getDefendantName())
            .name(defendantEntity.getName())
            .defendantAddress(defendantEntity.getAddress())
            .defendantDob(defendantEntity.getDateOfBirth())
            .defendantSex(defendantEntity.getSex())
            .defendantType(defendantEntity.getType())
            .defendantId(defendantEntity.getDefendantId())
            .nationality1(defendantEntity.getNationality1())
            .nationality2(defendantEntity.getNationality2())
            .cro(defendantEntity.getCro())
            .pnc(defendantEntity.getPnc())
            .crn(defendantEntity.getCrn());

        // Offences
        builder.offences(mapOffencesFromDefendantOffences(defendantEntity.getOffences()));
    }
}
