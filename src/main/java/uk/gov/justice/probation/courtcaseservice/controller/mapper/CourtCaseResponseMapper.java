package uk.gov.justice.probation.courtcaseservice.controller.mapper;

import uk.gov.justice.probation.courtcaseservice.controller.model.CourtCaseResponse;
import uk.gov.justice.probation.courtcaseservice.controller.model.OffenceResponse;
import uk.gov.justice.probation.courtcaseservice.controller.model.ProbationStatus;
import uk.gov.justice.probation.courtcaseservice.jpa.entity.CourtCaseEntity;
import uk.gov.justice.probation.courtcaseservice.jpa.entity.GroupedOffenderMatchesEntity;
import uk.gov.justice.probation.courtcaseservice.jpa.entity.OffenceEntity;
import uk.gov.justice.probation.courtcaseservice.jpa.entity.OffenderMatchEntity;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

public class CourtCaseResponseMapper {

    public static CourtCaseResponse mapFrom(CourtCaseEntity courtCaseEntity, int matchCount) {
        return CourtCaseResponse.builder()
            .caseId(courtCaseEntity.getCaseId())
            .caseNo(courtCaseEntity.getCaseNo())
            .crn(courtCaseEntity.getCrn())
            .pnc(courtCaseEntity.getPnc())
            .cro(courtCaseEntity.getCro())
            .listNo(courtCaseEntity.getListNo())
            .courtRoom(courtCaseEntity.getCourtRoom())
            .courtCode(courtCaseEntity.getCourtCode())
            .offences(mapOffencesFrom(courtCaseEntity))
            .previouslyKnownTerminationDate(courtCaseEntity.getPreviouslyKnownTerminationDate())
            .probationStatus(Optional.ofNullable(courtCaseEntity.getProbationStatus()).map(ProbationStatus::of).orElse(null))
            .sessionStartTime(courtCaseEntity.getSessionStartTime())
            .session(courtCaseEntity.getSession())
            .suspendedSentenceOrder(courtCaseEntity.getSuspendedSentenceOrder())
            .breach(courtCaseEntity.getBreach())
            .preSentenceActivity(courtCaseEntity.getPreSentenceActivity())
            .defendantName(courtCaseEntity.getDefendantName())
            .name(courtCaseEntity.getName())
            .defendantAddress(courtCaseEntity.getDefendantAddress())
            .defendantDob(courtCaseEntity.getDefendantDob())
            .defendantSex(courtCaseEntity.getDefendantSex())
            .defendantType(courtCaseEntity.getDefendantType())
            .nationality1(courtCaseEntity.getNationality1())
            .nationality2(courtCaseEntity.getNationality2())
            .createdToday(LocalDate.now().isEqual(Optional.ofNullable(courtCaseEntity.getFirstCreated()).orElse(LocalDateTime.now()).toLocalDate()))
            .numberOfPossibleMatches(matchCount)
            .build();
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
