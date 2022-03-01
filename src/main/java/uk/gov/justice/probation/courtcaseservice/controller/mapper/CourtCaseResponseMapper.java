package uk.gov.justice.probation.courtcaseservice.controller.mapper;

import lombok.extern.slf4j.Slf4j;
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

        Optional.ofNullable(hearingEntity.getDefendants()).orElse(Collections.emptyList())
                    .stream()
                    .filter(defendant -> defendantId.equalsIgnoreCase(defendant.getDefendantId()))
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
            .hearingId(hearingEntity.getHearingId())
            .source(hearingEntity.getSourceType().name())
            .createdToday(LocalDate.now().isEqual(Optional.ofNullable(hearingEntity.getFirstCreated()).orElse(LocalDateTime.now()).toLocalDate()));
        if (SourceType.LIBRA == hearingEntity.getSourceType()) {
            builder.caseNo(hearingEntity.getCaseNo());
        }
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
                .courtRoom(targetHearing.getCourtRoom())
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

    private static void addDefendantFields(CourtCaseResponseBuilder builder, HearingDefendantEntity defendantEntity) {
        addOffenderFields(builder, defendantEntity.getOffender());
        builder
            .defendantName(defendantEntity.getDefendantName())
            .name(defendantEntity.getName())
            .defendantAddress(defendantEntity.getAddress())
            .defendantDob(defendantEntity.getDateOfBirth())
            .defendantSex(defendantEntity.getSex())
            .defendantType(defendantEntity.getType())
            .defendantId(defendantEntity.getDefendantId())
            .phoneNumber(PhoneNumber.of(defendantEntity.getPhoneNumber()))
            .nationality1(defendantEntity.getNationality1())
            .nationality2(defendantEntity.getNationality2())
            .cro(defendantEntity.getCro())
            .pnc(defendantEntity.getPnc())
            .crn(defendantEntity.getCrn())
            .probationStatus(defendantEntity.getProbationStatusForDisplay())
        ;

        // Offences
        builder.offences(mapOffencesFromDefendantOffences(defendantEntity.getOffences()));
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
