package uk.gov.justice.probation.courtcaseservice.controller.model;

import lombok.Builder;
import lombok.Data;
import uk.gov.justice.probation.courtcaseservice.jpa.entity.AddressPropertiesEntity;
import uk.gov.justice.probation.courtcaseservice.jpa.entity.CourtCaseEntity;
import uk.gov.justice.probation.courtcaseservice.jpa.entity.ImmutableOffenceEntity;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

@Data
@Builder
public class CourtCaseRequest {
    private final String caseId;
    private final String caseNo;
    private final String courtCode;
    private final String courtRoom;
    private final LocalDateTime sessionStartTime;
    private final String probationStatus;
    private final LocalDate previouslyKnownTerminationDate;
    private final Boolean suspendedSentenceOrder;
    private final Boolean breach;
    private final List<OffenceRequest> offences;
    private final String defendantName;
    private final AddressRequest defendantAddress;
    private final LocalDate defendantDob;
    private final String defendantSex;
    private final String crn;
    private final String pnc;
    private final String cro;
    private final String listNo;
    private final String nationality1;
    private final String nationality2;

    public CourtCaseEntity asEntity() {
        final List<ImmutableOffenceEntity> offences = IntStream.range(0, Optional.ofNullable(getOffences())
                .map(List::size)
                .orElse(0)
        )
                .mapToObj(i -> {
                    var offence = getOffences().get(i);
                    return ImmutableOffenceEntity.builder()
                            .sequenceNumber(i + 1)
                            .offenceTitle(offence.getOffenceTitle())
                            .offenceSummary(offence.getOffenceSummary())
                            .act(offence.getAct())
                            .build();
                })
                .collect(Collectors.toList());
        final CourtCaseEntity entity = CourtCaseEntity.builder()
                .caseId(caseId)
                .caseNo(caseNo)
                .courtCode(courtCode)
                .courtRoom(courtRoom)
                .sessionStartTime(sessionStartTime)
                .probationStatus(probationStatus)
                .previouslyKnownTerminationDate(previouslyKnownTerminationDate)
                .suspendedSentenceOrder(suspendedSentenceOrder)
                .breach(breach)
                .defendantName(defendantName)
                .defendantDob(defendantDob)
                .defendantSex(defendantSex)
                .crn(crn)
                .pnc(pnc)
                .cro(cro)
                .listNo(listNo)
                .nationality1(nationality1)
                .nationality2(nationality2)
                .offences(offences)
                .defendantAddress(Optional.ofNullable(defendantAddress)
                        .map(addressRequest -> new AddressPropertiesEntity(
                                defendantAddress.getLine1(),
                                defendantAddress.getLine2(),
                                defendantAddress.getLine3(),
                                defendantAddress.getLine4(),
                                defendantAddress.getLine5(),
                                defendantAddress.getPostcode()
                        )).orElse(null))
                .build();

        offences.forEach(offence -> offence.setCourtCase(entity));
        return entity;
    }
}
