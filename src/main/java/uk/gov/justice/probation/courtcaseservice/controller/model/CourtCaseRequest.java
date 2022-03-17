package uk.gov.justice.probation.courtcaseservice.controller.model;

import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import uk.gov.justice.probation.courtcaseservice.controller.mapper.PhoneNumberMapper;
import uk.gov.justice.probation.courtcaseservice.jpa.entity.AddressPropertiesEntity;
import uk.gov.justice.probation.courtcaseservice.jpa.entity.CourtCaseEntity;
import uk.gov.justice.probation.courtcaseservice.jpa.entity.DefendantEntity;
import uk.gov.justice.probation.courtcaseservice.jpa.entity.DefendantOffenceEntity;
import uk.gov.justice.probation.courtcaseservice.jpa.entity.DefendantType;
import uk.gov.justice.probation.courtcaseservice.jpa.entity.HearingDayEntity;
import uk.gov.justice.probation.courtcaseservice.jpa.entity.HearingEntity;
import uk.gov.justice.probation.courtcaseservice.jpa.entity.NamePropertiesEntity;
import uk.gov.justice.probation.courtcaseservice.jpa.entity.OffenderEntity;
import uk.gov.justice.probation.courtcaseservice.jpa.entity.OffenderProbationStatus;
import uk.gov.justice.probation.courtcaseservice.jpa.entity.PhoneNumberEntity;
import uk.gov.justice.probation.courtcaseservice.jpa.entity.Sex;
import uk.gov.justice.probation.courtcaseservice.jpa.entity.SourceType;

import javax.validation.Valid;
import javax.validation.constraints.NotBlank;
import javax.validation.constraints.NotEmpty;
import javax.validation.constraints.NotNull;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.Collections;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

@Data
@Builder
@NoArgsConstructor(access = AccessLevel.PRIVATE, force = true)
@AllArgsConstructor
public class CourtCaseRequest {
    static final SourceType DEFAULT_SOURCE = SourceType.LIBRA;
    @NotBlank
    private final String caseId;
    private final String caseNo;
    @NotBlank
    private final String courtCode;
    @NotBlank
    private final String courtRoom;
    private final String source;
    @NotNull
    private final LocalDateTime sessionStartTime;
    private final String probationStatus;
    private final LocalDate previouslyKnownTerminationDate;
    private final Boolean suspendedSentenceOrder;
    private final Boolean breach;
    private final Boolean preSentenceActivity;
    @Valid
    @NotEmpty
    private final List<OffenceRequestResponse> offences;
    @NotNull
    private final NamePropertiesEntity name;
    @NotBlank
    private final String defendantName;
    private final AddressRequestResponse defendantAddress;
    private final LocalDate defendantDob;
    private final String defendantSex;
    @NotNull
    private final DefendantType defendantType;
    // This may be null in the CourtCaseRequest because it is assigned when null before saving
    private final String defendantId;
    private final String crn;
    private final String pnc;
    private final String cro;
    private final String listNo;
    private final String nationality1;
    private final String nationality2;
    private final Boolean awaitingPsr;
    private final PhoneNumber phoneNumber;

    public HearingEntity asEntity() {

        final List<HearingDayEntity> hearings = List.of(HearingDayEntity.builder()
            .courtCode(courtCode)
            .courtRoom(courtRoom)
            .day(sessionStartTime.toLocalDate())
            .time(sessionStartTime.toLocalTime())
            .listNo(listNo)
            .build());

        final List<DefendantEntity> defendants = buildDefendants();

        final HearingEntity entity = HearingEntity.builder()
                .courtCase(CourtCaseEntity.builder()
                    .caseId(caseId)
                    .caseNo(caseNo)
                    .sourceType(SourceType.valueOf(Optional.ofNullable(source).orElse(DEFAULT_SOURCE.name())))
                .build()) 
                // TODO: Remove. This is a temporary measure to allow the application to continue working whilst we update the data structures adding hearingId
                .hearingId(caseId)
                .hearingDays(hearings)
                .defendants(defendants)
                .build();

        hearings.forEach(hearingEntity -> hearingEntity.setHearing(entity));
        defendants.forEach(defendantEntity -> defendantEntity.setHearing(entity));
        return entity;
    }

    List<DefendantEntity> buildDefendants() {

        final List<DefendantOffenceEntity> offences = IntStream.range(0, Optional.ofNullable(getOffences())
                        .map(List::size)
                        .orElse(0)
                )
                .mapToObj(i -> {
                    var offence = getOffences().get(i);
                    return DefendantOffenceEntity.builder()
                            .sequence(i + 1)
                            .title(offence.getOffenceTitle())
                            .summary(offence.getOffenceSummary())
                            .act(offence.getAct())
                            .listNo(offence.getListNo())
                            .build();
                })
                .collect(Collectors.toList());

        final var defendant = DefendantEntity.builder()
            .address(Optional.ofNullable(defendantAddress)
                    .map(this::buildAddress)
                    .orElse(null))
            .offender(Optional.ofNullable(crn)
                .map(this::buildOffender)
                .orElse(null))
            .dateOfBirth(defendantDob)
            .defendantName(defendantName)
            .type(defendantType)
            .nationality1(nationality1)
            .nationality2(nationality2)
            .name(name)
            .sex(Sex.fromString(defendantSex))
            .defendantId(Optional.ofNullable(defendantId).orElse(UUID.randomUUID().toString()))
            .cro(cro)
            .pnc(pnc)
            .phoneNumber(PhoneNumberMapper.mapFrom(phoneNumber))
            .offences(offences)
            .build();

        offences.forEach(defendantOffence -> defendantOffence.setDefendant(defendant));
        return Collections.singletonList(defendant);
    }

    private AddressPropertiesEntity buildAddress(AddressRequestResponse addressRequest) {
        return AddressPropertiesEntity.builder()
            .line1(addressRequest.getLine1())
            .line2(addressRequest.getLine2())
            .line3(addressRequest.getLine3())
            .line4(addressRequest.getLine4())
            .line5(addressRequest.getLine5())
            .postcode(addressRequest.getPostcode())
            .build();
    }

    private OffenderEntity buildOffender(String crn) {
        return OffenderEntity.builder()
                .crn(crn)
                .probationStatus(OffenderProbationStatus.of(probationStatus))
                .previouslyKnownTerminationDate(previouslyKnownTerminationDate)
                .awaitingPsr(awaitingPsr)
                .breach(Optional.ofNullable(breach).orElse(false))
                .preSentenceActivity(Optional.ofNullable(preSentenceActivity).orElse(false))
                .suspendedSentenceOrder(Optional.ofNullable(suspendedSentenceOrder).orElse(false))
                .build();
    }

}
