package uk.gov.justice.probation.courtcaseservice.controller.mapper;

import org.springframework.stereotype.Component;
import uk.gov.justice.probation.courtcaseservice.controller.model.CourtCaseResponse;
import uk.gov.justice.probation.courtcaseservice.jpa.entity.CourtCaseEntity;

@Component
public class CourtCaseResponseMapper {
    public CourtCaseResponse mapFrom(CourtCaseEntity courtCaseEntity) {
        return CourtCaseResponse.builder()
                .caseId(courtCaseEntity.getCaseId())
                .caseNo(courtCaseEntity.getCaseNo())
                .courtRoom(courtCaseEntity.getCourtRoom())
                .courtCode(courtCaseEntity.getCourtCode())
                .data(courtCaseEntity.getData())
                .lastUpdated(courtCaseEntity.getLastUpdated())
                .offences(courtCaseEntity.getOffences())
                .previouslyKnownTerminationDate(courtCaseEntity.getPreviouslyKnownTerminationDate())
                .probationStatus(courtCaseEntity.getProbationStatus())
                .sessionStartTime(courtCaseEntity.getSessionStartTime())
                .suspendedSentenceOrder(courtCaseEntity.getSuspendedSentenceOrder())
                .build();
    }
}
