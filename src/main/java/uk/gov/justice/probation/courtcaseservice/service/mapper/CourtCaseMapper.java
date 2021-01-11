package uk.gov.justice.probation.courtcaseservice.service.mapper;

import java.util.stream.Collectors;
import uk.gov.justice.probation.courtcaseservice.jpa.entity.CourtCaseEntity;
import uk.gov.justice.probation.courtcaseservice.jpa.entity.OffenceEntity;

public class CourtCaseMapper {

    public static CourtCaseEntity create(CourtCaseEntity courtCaseEntity, String updatedProbationStatus) {

        var newCourtCaseEntity = CourtCaseEntity.builder()
            .breach(courtCaseEntity.getBreach())
            .caseId(courtCaseEntity.getCaseId())
            .caseNo(courtCaseEntity.getCaseNo())
            .courtCode(courtCaseEntity.getCourtCode())
            .courtRoom(courtCaseEntity.getCourtRoom())
            .crn(courtCaseEntity.getCrn())
            .cro(courtCaseEntity.getCro())
            .defendantAddress(courtCaseEntity.getDefendantAddress())
            .defendantDob(courtCaseEntity.getDefendantDob())
            .defendantName(courtCaseEntity.getDefendantName())
            .defendantSex(courtCaseEntity.getDefendantSex())
            .defendantType(courtCaseEntity.getDefendantType())
            .firstCreated(courtCaseEntity.getFirstCreated())
            .listNo(courtCaseEntity.getListNo())
            .name(courtCaseEntity.getName())
            .nationality1(courtCaseEntity.getNationality1())
            .nationality2(courtCaseEntity.getNationality2())
            .pnc(courtCaseEntity.getPnc())
            .previouslyKnownTerminationDate(courtCaseEntity.getPreviouslyKnownTerminationDate())
            .probationStatus(updatedProbationStatus)
            .sessionStartTime(courtCaseEntity.getSessionStartTime())
            .suspendedSentenceOrder(courtCaseEntity.getSuspendedSentenceOrder())
            .offences(courtCaseEntity.getOffences()
                            .stream()
                            .map(CourtCaseMapper::createOffence)
                            .collect(Collectors.toList()))
            .build();

        newCourtCaseEntity.getOffences().forEach(offenceEntity -> offenceEntity.setCourtCase(newCourtCaseEntity));
        return newCourtCaseEntity;
    }

    static OffenceEntity createOffence(OffenceEntity offenceEntity) {
        return OffenceEntity.builder()
            .act(offenceEntity.getAct())
            .offenceSummary(offenceEntity.getOffenceSummary())
            .offenceTitle(offenceEntity.getOffenceTitle())
            .sequenceNumber(offenceEntity.getSequenceNumber())
            .build();
    }
}
