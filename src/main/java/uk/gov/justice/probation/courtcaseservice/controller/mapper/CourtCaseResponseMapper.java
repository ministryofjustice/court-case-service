package uk.gov.justice.probation.courtcaseservice.controller.mapper;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;
import org.springframework.stereotype.Component;
import uk.gov.justice.probation.courtcaseservice.controller.model.CourtCaseResponse;
import uk.gov.justice.probation.courtcaseservice.controller.model.OffenceResponse;
import uk.gov.justice.probation.courtcaseservice.jpa.entity.CourtCaseEntity;
import uk.gov.justice.probation.courtcaseservice.jpa.entity.GroupedOffenderMatchesEntity;
import uk.gov.justice.probation.courtcaseservice.jpa.entity.OffenceEntity;
import uk.gov.justice.probation.courtcaseservice.jpa.entity.OffenderMatchEntity;

@Component
public class CourtCaseResponseMapper {
    public CourtCaseResponse mapFrom(CourtCaseEntity courtCaseEntity, GroupedOffenderMatchesEntity groupedOffenderMatches) {
        return CourtCaseResponse.builder()
                .caseId(courtCaseEntity.getCaseId())
                .caseNo(courtCaseEntity.getCaseNo())
                .crn(courtCaseEntity.getCrn())
                .pnc(courtCaseEntity.getPnc())
                .cro(courtCaseEntity.getCro())
                .listNo(courtCaseEntity.getListNo())
                .courtRoom(courtCaseEntity.getCourtRoom())
                .courtCode(courtCaseEntity.getCourtCode())
                .lastUpdated(courtCaseEntity.getCreated())
                .offences(mapOffencesFrom(courtCaseEntity))
                .previouslyKnownTerminationDate(courtCaseEntity.getPreviouslyKnownTerminationDate())
                .probationStatus(courtCaseEntity.getProbationStatus())
                .sessionStartTime(courtCaseEntity.getSessionStartTime())
                .session(courtCaseEntity.getSession())
                .suspendedSentenceOrder(courtCaseEntity.getSuspendedSentenceOrder())
                .breach(courtCaseEntity.getBreach())
                .defendantName(courtCaseEntity.getDefendantName())
                .defendantAddress(courtCaseEntity.getDefendantAddress())
                .name(courtCaseEntity.getName())
                .defendantDob(courtCaseEntity.getDefendantDob())
                .defendantSex(courtCaseEntity.getDefendantSex())
                .defendantType(courtCaseEntity.getDefendantType())
                .nationality1(courtCaseEntity.getNationality1())
                .nationality2(courtCaseEntity.getNationality2())
                .createdToday(LocalDate.now().isEqual(Optional.ofNullable(courtCaseEntity.getCreated()).orElse(LocalDateTime.now()).toLocalDate()))
                .numberOfPossibleMatches(calculateNumberOfPossibleMatches(groupedOffenderMatches))
                .build();
    }

    public static long calculateNumberOfPossibleMatches(GroupedOffenderMatchesEntity groupedOffenderMatches) {
        if (groupedOffenderMatches == null) {
            return 0;
        }
        return groupedOffenderMatches.getOffenderMatches()
                .stream()
                .map(OffenderMatchEntity::getCrn)
                .distinct()
                .count();
    }

    private List<OffenceResponse> mapOffencesFrom(CourtCaseEntity courtCaseEntity) {
        return Optional.ofNullable(courtCaseEntity.getOffences()).orElse(Collections.emptyList())
                .stream()
                .sorted(Comparator.comparing(offenceEntity ->
                        // Default to very high number so that unordered items are last
                        (offenceEntity.getSequenceNumber() != null ? offenceEntity.getSequenceNumber() : Integer.MAX_VALUE)))
                .map(this::mapFrom)
                .collect(Collectors.toList());
    }

    private OffenceResponse mapFrom(OffenceEntity offenceEntity) {
        return OffenceResponse.builder()
                .offenceTitle(offenceEntity.getOffenceTitle())
                .offenceSummary(offenceEntity.getOffenceSummary())
                .act(offenceEntity.getAct())
                .sequenceNumber(offenceEntity.getSequenceNumber())
                .build();
    }
}
