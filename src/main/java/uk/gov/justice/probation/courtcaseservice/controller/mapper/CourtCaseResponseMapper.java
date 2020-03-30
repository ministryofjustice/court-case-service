package uk.gov.justice.probation.courtcaseservice.controller.mapper;

import org.springframework.stereotype.Component;
import uk.gov.justice.probation.courtcaseservice.controller.model.CourtCaseResponse;
import uk.gov.justice.probation.courtcaseservice.controller.model.OffenceResponse;
import uk.gov.justice.probation.courtcaseservice.jpa.entity.CourtCaseEntity;
import uk.gov.justice.probation.courtcaseservice.jpa.entity.OffenceEntity;

import java.util.Comparator;
import java.util.List;
import java.util.stream.Collectors;

@Component
public class CourtCaseResponseMapper {
    public CourtCaseResponse mapFrom(CourtCaseEntity courtCaseEntity) {
        return CourtCaseResponse.builder()
                .caseId(courtCaseEntity.getCaseId())
                .caseNo(courtCaseEntity.getCaseNo())
                .crn(courtCaseEntity.getCrn())
                .courtRoom(courtCaseEntity.getCourtRoom())
                .courtCode(courtCaseEntity.getCourtCode())
                .data(courtCaseEntity.getData())
                .lastUpdated(courtCaseEntity.getLastUpdated())
                .offences(mapOffencesFrom(courtCaseEntity))
                .previouslyKnownTerminationDate(courtCaseEntity.getPreviouslyKnownTerminationDate())
                .probationStatus(courtCaseEntity.getProbationStatus())
                .sessionStartTime(courtCaseEntity.getSessionStartTime())
                .suspendedSentenceOrder(courtCaseEntity.getSuspendedSentenceOrder())
                .breach(courtCaseEntity.getBreach())
                .defendantName(courtCaseEntity.getDefendantName())
                .defendantAddress(courtCaseEntity.getDefendantAddress())
                .build();
    }

    private List<OffenceResponse> mapOffencesFrom(CourtCaseEntity courtCaseEntity) {
        return courtCaseEntity.getOffences()
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
                .build();
    }
}
