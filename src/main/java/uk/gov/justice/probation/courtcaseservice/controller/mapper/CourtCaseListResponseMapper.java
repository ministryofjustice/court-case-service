package uk.gov.justice.probation.courtcaseservice.controller.mapper;

import lombok.extern.slf4j.Slf4j;
import uk.gov.justice.probation.courtcaseservice.controller.model.*;
import uk.gov.justice.probation.courtcaseservice.controller.model.CourtCaseResponse.CourtCaseResponseBuilder;
import uk.gov.justice.probation.courtcaseservice.jpa.dto.*;
import uk.gov.justice.probation.courtcaseservice.jpa.entity.PleaEntity;
import uk.gov.justice.probation.courtcaseservice.jpa.entity.SourceType;
import uk.gov.justice.probation.courtcaseservice.jpa.entity.VerdictEntity;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@Slf4j
public class CourtCaseListResponseMapper {
    public static CourtCaseResponse mapFrom(HearingDTO hearingDTO, HearingDefendantDTO defendantEntity, int matchCount, LocalDate hearingDate) {
        // Core case-based
        final var builder = CourtCaseResponse.builder();

        buildCaseFields(builder, hearingDTO, defendantEntity.getDefendantId());
        buildHearings(builder, hearingDTO, hearingDate);

        // Defendant-based fields
        addDefendantFields(builder, defendantEntity);
        builder.numberOfPossibleMatches(matchCount);

        return builder.build();
    }

    private static void buildCaseFields(CourtCaseResponseBuilder builder, HearingDTO hearingDTO, String defendantId) {
        // Case-based fields
        builder.caseId(hearingDTO.getCaseId())
                .hearingType(hearingDTO.getHearingType())
                .hearingEventType(hearingDTO.getHearingEventType())
                .hearingId(hearingDTO.getHearingId())
                .urn(hearingDTO.getCourtCase().getUrn())
                .source(hearingDTO.getSourceType().name())
                .createdToday(LocalDate.now().isEqual(Optional.ofNullable(hearingDTO.getFirstCreated()).orElse(LocalDateTime.now()).toLocalDate()))
                .caseMarkers(buildCaseMarkers(hearingDTO));

        if (SourceType.LIBRA == hearingDTO.getSourceType()) {
            builder.caseNo(hearingDTO.getCaseNo());
        }
    }

    private static List<CaseMarker> buildCaseMarkers(HearingDTO hearingDTO) {
        return Optional.ofNullable(hearingDTO.getCourtCase().getCaseMarkers())
                .map(caseMarkerDTOs -> caseMarkerDTOs.stream().map(CaseMarkerDTO::of).collect(Collectors.toList()))
                .orElse(Collections.emptyList());
    }

    static void buildHearings(CourtCaseResponseBuilder builder, HearingDTO hearingDTO, LocalDate hearingDate) {
        var hearings = Optional.ofNullable(hearingDTO.getHearingDays())
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
                .listNo(hearingDTO.getListNo());
    }

    private static String getNormalisedCourtRoom(String courtRoom) {
        return courtRoom.contains("Courtroom") ? courtRoom.replaceAll("[a-zA-Z 0]", "") : courtRoom.replace("([0]*)?", "");
    }

    private static List<OffenceResponse> mapOffencesFromDefendantOffences(List<OffenceDTO> offenceDTOs) {
        return Optional.ofNullable(offenceDTOs).orElse(Collections.emptyList())
                .stream()
                .sorted(Comparator.comparing(offenceDTO ->
                        // Default to very high number so that unordered items are last
                        (offenceDTO.getSequence() != null ? offenceDTO.getSequence() : Integer.MAX_VALUE)))
                .map(CourtCaseListResponseMapper::mapFrom)
                .collect(Collectors.toList());
    }

    private static OffenceResponse mapFrom(OffenceDTO offenceDTO) {
        return OffenceResponse.builder()
                .offenceTitle(offenceDTO.getTitle())
                .offenceCode(offenceDTO.getOffenceCode())
                .offenceSummary(offenceDTO.getSummary())
                .act(offenceDTO.getAct())
                .sequenceNumber(offenceDTO.getSequence())
                .listNo(offenceDTO.getListNo())
                .offenceCode(offenceDTO.getOffenceCode())
                .plea(Optional.ofNullable(offenceDTO.getPlea()).map(PleaEntity::of).orElse(null))
                .verdict(Optional.ofNullable(offenceDTO.getVerdict()).map(VerdictEntity::of).orElse(null))
                .build();
    }

    private static void addDefendantFields(CourtCaseResponseBuilder builder, HearingDefendantDTO hearingDefendantDTO) {
        final var defendant = hearingDefendantDTO.getDefendant();
        addOffenderFields(builder, defendant.getOffender());
        builder
                .defendantName(defendant.getDefendantName())
                .name(defendant.getName())
                .defendantSurname(defendant.getDefendantSurname())
                .defendantForename(defendant.getName().getForename1())
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
                .crn(hearingDefendantDTO.getCrn())
                .probationStatus(hearingDefendantDTO.getProbationStatusForDisplay())
                .confirmedOffender(defendant.isOffenderConfirmed())
                .personId(defendant.getPersonId())
                .hearingPrepStatus(HearingPrepStatus.valueOf(hearingDefendantDTO.getPrepStatus()))
        ;

        // Offences
        builder.offences(mapOffencesFromDefendantOffences(hearingDefendantDTO.getOffences()));
    }

    private static void addOffenderFields(CourtCaseResponseBuilder builder, OffenderDTO offender) {
        builder
                .awaitingPsr(Optional.ofNullable(offender)
                        .map(OffenderDTO::getAwaitingPsr)
                        .orElse(null))
                .breach(Optional.ofNullable(offender)
                        .map(OffenderDTO::isBreach)
                        .orElse(null))
                .preSentenceActivity(Optional.ofNullable(offender)
                        .map(OffenderDTO::isPreSentenceActivity)
                        .orElse(null))
                .suspendedSentenceOrder(Optional.ofNullable(offender)
                        .map(OffenderDTO::isSuspendedSentenceOrder)
                        .orElse(null))
                .previouslyKnownTerminationDate(Optional.ofNullable(offender)
                        .map(OffenderDTO::getPreviouslyKnownTerminationDate)
                        .orElse(null))
        ;
    }
}
