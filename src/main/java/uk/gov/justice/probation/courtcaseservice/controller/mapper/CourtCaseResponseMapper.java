package uk.gov.justice.probation.courtcaseservice.controller.mapper;

import lombok.extern.slf4j.Slf4j;
import uk.gov.justice.probation.courtcaseservice.controller.model.CourtCaseResponse;
import uk.gov.justice.probation.courtcaseservice.controller.model.CourtCaseResponse.CourtCaseResponseBuilder;
import uk.gov.justice.probation.courtcaseservice.controller.model.HearingResponse;
import uk.gov.justice.probation.courtcaseservice.controller.model.OffenceResponse;
import uk.gov.justice.probation.courtcaseservice.controller.model.ProbationStatus;
import uk.gov.justice.probation.courtcaseservice.jpa.entity.CourtCaseEntity;
import uk.gov.justice.probation.courtcaseservice.jpa.entity.DefendantEntity;
import uk.gov.justice.probation.courtcaseservice.jpa.entity.OffenceEntity;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@Slf4j
public class CourtCaseResponseMapper {

    public static CourtCaseResponse mapFrom(CourtCaseEntity courtCaseEntity, int matchCount, boolean includeCaseNo, LocalDate hearingDate) {
        final var builder = CourtCaseResponse.builder()
            .caseId(courtCaseEntity.getCaseId())
            .crn(courtCaseEntity.getCrn())
            .pnc(courtCaseEntity.getPnc())
            .cro(courtCaseEntity.getCro())
            .source(courtCaseEntity.getSourceType().name())
            .offences(mapOffencesFrom(courtCaseEntity))
            .previouslyKnownTerminationDate(courtCaseEntity.getPreviouslyKnownTerminationDate())
            .probationStatus(Optional.ofNullable(courtCaseEntity.getProbationStatus()).map(ProbationStatus::of).orElse(null))
            .suspendedSentenceOrder(courtCaseEntity.getSuspendedSentenceOrder())
            .breach(courtCaseEntity.getBreach())
            .preSentenceActivity(courtCaseEntity.getPreSentenceActivity())
            .defendantName(courtCaseEntity.getDefendantName())
            .name(courtCaseEntity.getName())
            .defendantAddress(courtCaseEntity.getDefendantAddress())
            .defendantDob(courtCaseEntity.getDefendantDob())
            .defendantSex(courtCaseEntity.getDefendantSex())
            .defendantType(courtCaseEntity.getDefendantType())
            .defendantId(getDefendantId(courtCaseEntity.getDefendants()))
            .nationality1(courtCaseEntity.getNationality1())
            .nationality2(courtCaseEntity.getNationality2())
            .createdToday(LocalDate.now().isEqual(Optional.ofNullable(courtCaseEntity.getFirstCreated()).orElse(LocalDateTime.now()).toLocalDate()))
            .numberOfPossibleMatches(matchCount)
            .awaitingPsr(courtCaseEntity.getAwaitingPsr());

        buildHearings(builder, courtCaseEntity, hearingDate);

        if (includeCaseNo) {
            builder.caseNo(courtCaseEntity.getCaseNo());
        }
        return builder.build();
    }

    public static CourtCaseResponse mapFrom(CourtCaseEntity courtCaseEntity, int matchCount, LocalDate hearingDate) {
        return mapFrom(courtCaseEntity, matchCount, true, hearingDate);
    }

    static void buildHearings(CourtCaseResponseBuilder builder, CourtCaseEntity courtCaseEntity, LocalDate hearingDate) {
        var hearings = Optional.ofNullable(courtCaseEntity.getHearings())
            .orElse(Collections.emptyList());

        var targetHearing = hearings
            .stream()
            .filter(hearingEntity -> hearingEntity.getHearingDay().isEqual(Optional.ofNullable(hearingDate).orElse(LocalDate.MIN)))
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

    private static List<OffenceResponse> mapOffencesFrom(CourtCaseEntity courtCaseEntity) {
        return Optional.ofNullable(courtCaseEntity.getOffences()).orElse(Collections.emptyList())
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

}
