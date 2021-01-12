package uk.gov.justice.probation.courtcaseservice.controller.model;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;
import java.util.stream.IntStream;
import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import uk.gov.justice.probation.courtcaseservice.jpa.entity.AddressPropertiesEntity;
import uk.gov.justice.probation.courtcaseservice.jpa.entity.CourtCaseEntity;
import uk.gov.justice.probation.courtcaseservice.jpa.entity.DefendantType;
import uk.gov.justice.probation.courtcaseservice.jpa.entity.NamePropertiesEntity;
import uk.gov.justice.probation.courtcaseservice.jpa.entity.OffenceEntity;

@Data
@Builder
@NoArgsConstructor(access = AccessLevel.PRIVATE, force = true)
@AllArgsConstructor
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
    private final NamePropertiesEntity name;
    private final String defendantName;
    private final AddressRequest defendantAddress;
    private final LocalDate defendantDob;
    private final String defendantSex;
    private final DefendantType defendantType;
    private final String crn;
    private final String pnc;
    private final String cro;
    private final String listNo;
    private final String nationality1;
    private final String nationality2;

    public CourtCaseEntity asEntity() {
        final List<OffenceEntity> offences = IntStream.range(0, Optional.ofNullable(getOffences())
                .map(List::size)
                .orElse(0)
        )
                .mapToObj(i -> {
                    var offence = getOffences().get(i);
                    return OffenceEntity.builder()
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
                .defendantType(defendantType)
                .name(Optional.ofNullable(name)
                        .map(nameRequest -> NamePropertiesEntity.builder()
                            .title(name.getTitle())
                            .forename1(name.getForename1())
                            .forename2(name.getForename2())
                            .forename3(name.getForename3())
                            .surname(name.getSurname())
                            .build()
                        ).orElse(null) )
                .crn(crn)
                .pnc(pnc)
                .cro(cro)
                .listNo(listNo)
                .nationality1(nationality1)
                .nationality2(nationality2)
                .offences(offences)
                .defendantAddress(Optional.ofNullable(defendantAddress)
                        .map(addressRequest -> AddressPropertiesEntity.builder()
                                .line1(defendantAddress.getLine1())
                                .line2(defendantAddress.getLine2())
                                .line3(defendantAddress.getLine3())
                                .line4(defendantAddress.getLine4())
                                .line5(defendantAddress.getLine5())
                                .postcode(defendantAddress.getPostcode())
                            .build()
                        ).orElse(null))
                .build();

        offences.forEach(offence -> offence.setCourtCase(entity));
        return entity;
    }
}
