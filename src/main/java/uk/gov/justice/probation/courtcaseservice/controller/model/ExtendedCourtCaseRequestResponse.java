package uk.gov.justice.probation.courtcaseservice.controller.model;

import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import uk.gov.justice.probation.courtcaseservice.jpa.entity.AddressPropertiesEntity;
import uk.gov.justice.probation.courtcaseservice.jpa.entity.CourtCaseEntity;
import uk.gov.justice.probation.courtcaseservice.jpa.entity.DefendantEntity;
import uk.gov.justice.probation.courtcaseservice.jpa.entity.HearingDayEntity;
import uk.gov.justice.probation.courtcaseservice.jpa.entity.HearingDefendantEntity;
import uk.gov.justice.probation.courtcaseservice.jpa.entity.HearingEntity;
import uk.gov.justice.probation.courtcaseservice.jpa.entity.OffenceEntity;
import uk.gov.justice.probation.courtcaseservice.jpa.entity.OffenderEntity;
import uk.gov.justice.probation.courtcaseservice.jpa.entity.OffenderProbationStatus;
import uk.gov.justice.probation.courtcaseservice.jpa.entity.Sex;
import uk.gov.justice.probation.courtcaseservice.jpa.entity.SourceType;

import javax.validation.Valid;
import javax.validation.constraints.NotBlank;
import javax.validation.constraints.NotEmpty;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

@Data
@Builder
@NoArgsConstructor(access = AccessLevel.PRIVATE, force = true)
@AllArgsConstructor
@Slf4j
@JsonInclude(JsonInclude.Include.NON_NULL)
public class ExtendedCourtCaseRequestResponse {
    static final SourceType DEFAULT_SOURCE = SourceType.COMMON_PLATFORM;
    private final String caseNo;
    @NotBlank
    private final String caseId;
    private final String hearingId;
    private final String source;
    private final String urn;
    @Valid
    @NotEmpty
    private final List<HearingDay> hearingDays;
    @Valid
    @NotEmpty
    private final List<Defendant> defendants;

    public static ExtendedCourtCaseRequestResponse of(HearingEntity hearing) {
        return ExtendedCourtCaseRequestResponse.builder()
                .caseNo(hearing.getCaseNo())
                .caseId(hearing.getCaseId())
                .hearingId(hearing.getHearingId())
                .urn(hearing.getCourtCase().getUrn())
                .source(hearing.getSourceType().name())
                .hearingDays(hearing.getHearingDays().stream()
                        .map(hearingEntity -> HearingDay.builder()
                                .courtCode(hearingEntity.getCourtCode())
                                .courtRoom(hearingEntity.getCourtRoom())
                                .sessionStartTime(Optional.ofNullable(hearingEntity.getDay())
                                        .map(day -> LocalDateTime.of(day, Optional.ofNullable(hearingEntity.getTime()).orElse(LocalTime.MIDNIGHT)))
                                        .orElse(null))
                                .listNo(hearingEntity.getListNo())
                                .build())
                        .toList())
                .defendants(hearing.getHearingDefendants().stream()
                        .map(hearingDefendantEntity -> {
                            final var defendant = hearingDefendantEntity.getDefendant();
                            return Defendant.builder()
                                    .defendantId(defendant.getDefendantId())
                                    .name(defendant.getName())
                                    .dateOfBirth(defendant.getDateOfBirth())
                                    .address(Optional.ofNullable(defendant.getAddress())
                                            .map(address -> AddressRequestResponse.builder()
                                                    .line1(address.getLine1())
                                                    .line2(address.getLine2())
                                                    .line3(address.getLine3())
                                                    .line4(address.getLine4())
                                                    .line5(address.getLine5())
                                                    .postcode(address.getPostcode())
                                                    .build())
                                            .orElse(null))
                                    .type(defendant.getType())
                                    .sex(Optional.ofNullable(defendant.getSex()).map(Enum::name).orElse(null))
                                    .pnc(defendant.getPnc())
                                    .cro(defendant.getCro())
                                    .crn(Optional.ofNullable(defendant.getOffender()).map(OffenderEntity::getCrn).orElse(null))
                                    .probationStatus(Optional.ofNullable(defendant.getOffender()).map(offender -> offender.getProbationStatus().name()).orElse(null))
                                    .awaitingPsr(Optional.ofNullable(defendant.getOffender()).map(OffenderEntity::getAwaitingPsr).orElse(null))
                                    .breach(Optional.ofNullable(defendant.getOffender()).map(OffenderEntity::isBreach).orElse(null))
                                    .preSentenceActivity(Optional.ofNullable(defendant.getOffender()).map(OffenderEntity::isPreSentenceActivity).orElse(null))
                                    .suspendedSentenceOrder(Optional.ofNullable(defendant.getOffender()).map(OffenderEntity::isSuspendedSentenceOrder).orElse(null))
                                    .previouslyKnownTerminationDate(Optional.ofNullable(defendant.getOffender()).map(OffenderEntity::getPreviouslyKnownTerminationDate).orElse(null))
                                    .phoneNumber(PhoneNumber.of(defendant.getPhoneNumber()))
                                    .offender(Offender.builder().pnc(Optional.ofNullable(defendant.getOffender()).map(OffenderEntity::getPnc).orElse(null)).build())
                                    .offences(Optional.ofNullable(hearingDefendantEntity)
                                            .map(HearingDefendantEntity::getOffences)
                                            .orElse(Collections.emptyList()).stream()
                                            .sorted(Comparator.comparingInt(OffenceEntity::getSequence))
                                            .map(offence ->  OffenceRequestResponse.builder()
                                                    .act(offence.getAct())
                                                    .offenceTitle(offence.getTitle())
                                                    .offenceSummary(offence.getSummary())
                                                    .listNo(offence.getListNo())
                                                    .build())
                                            .toList())

                                    .build();
                        })
                        .toList())
                .build();
    }

    public HearingEntity asHearingEntity() {

        final var hearingDayEntities = Optional.ofNullable(hearingDays).orElse(Collections.emptyList())
            .stream()
            .map(this::buildHearing)
            .toList();
        final var hearingDefendantEntities = Optional.ofNullable(defendants).orElse(Collections.emptyList())
            .stream()
            .map(this::buildDefendant)
            .toList();

        final var hearingEntity = HearingEntity.builder()
            .hearingDays(hearingDayEntities)
            .hearingDefendants(hearingDefendantEntities)
            .courtCase(CourtCaseEntity.builder()
                .caseNo(caseNo)
                .caseId(caseId)
                .urn(urn)
                .sourceType(SourceType.valueOf(Optional.ofNullable(source).orElse(DEFAULT_SOURCE.name())))
            .build())
            .hearingId(Optional.ofNullable(hearingId).orElse(caseId))
            .build();

        hearingDayEntities.forEach(hearingDayEntity -> hearingDayEntity.setHearing(hearingEntity));
        hearingDefendantEntities.forEach(hearingDefendantEntity -> hearingDefendantEntity.setHearing(hearingEntity));
        return hearingEntity;
    }

    private HearingDefendantEntity buildDefendant(Defendant defendant) {

        final var offences = buildDefendantOffences(defendant.getOffences());
        final var offender = buildOffender(defendant);

        final var hearingDefendantEntity = HearingDefendantEntity.builder()
            .defendantId(defendant.getDefendantId())
            .defendant(DefendantEntity.builder()
                .address(buildAddress(defendant.getAddress()))
                .crn(defendant.getCrn())
                .cro(defendant.getCro())
                .dateOfBirth(defendant.getDateOfBirth())
                .defendantName(defendant.getName().getFullName())
                .name(defendant.getName())
                .offender(offender)
                .pnc(defendant.getPnc())
                .sex(Sex.fromString(defendant.getSex()))
                .type(defendant.getType())
                .defendantId(defendant.getDefendantId())
                .phoneNumber(Optional.ofNullable(defendant.getPhoneNumber()).map(PhoneNumber::asEntity).orElse(null))
                .build())
            .offences(offences)
            .build();
        offences.forEach(offence -> offence.setHearingDefendant(hearingDefendantEntity));
        return hearingDefendantEntity;
    }

    private OffenderEntity buildOffender(Defendant defendant) {
        return Optional.ofNullable(defendant.getCrn())
                .map((crn) ->
                    OffenderEntity.builder()
                                .crn(crn)
                                .previouslyKnownTerminationDate(defendant.getPreviouslyKnownTerminationDate())
                                .probationStatus(OffenderProbationStatus.of(defendant.getProbationStatus()))
                                .awaitingPsr(defendant.getAwaitingPsr())
                                .breach(Optional.ofNullable(defendant.getBreach()).orElse(false))
                                .preSentenceActivity(Optional.ofNullable(defendant.getPreSentenceActivity()).orElse(false))
                                .suspendedSentenceOrder(Optional.ofNullable(defendant.getSuspendedSentenceOrder()).orElse(false))
                                .pnc(Optional.ofNullable(defendant.getOffender()).map(Offender::getPnc).orElse(null))
                                .build())
                    .orElse(null);
    }

    private List<OffenceEntity> buildDefendantOffences(List<OffenceRequestResponse> offences) {

        return IntStream.range(0, Optional.ofNullable(offences)
                                    .map(List::size)
                                    .orElse(0))
            .mapToObj(i -> {
                var offence = offences.get(i);
                return OffenceEntity.builder()
                    .sequence(i + 1)
                    .title(offence.getOffenceTitle())
                    .summary(offence.getOffenceSummary())
                    .act(offence.getAct())
                    .listNo(offence.getListNo())
                    .build();
            })
            .collect(Collectors.toList());
    }

    private HearingDayEntity buildHearing(HearingDay hearingDay) {
        return HearingDayEntity.builder()
            .courtCode(hearingDay.getCourtCode())
            .courtRoom(hearingDay.getCourtRoom())
            .listNo(hearingDay.getListNo())
            .time(hearingDay.getSessionStartTime().toLocalTime())
            .day(hearingDay.getSessionStartTime().toLocalDate())
            .build();
    }

    private AddressPropertiesEntity buildAddress(AddressRequestResponse addressRequest) {
        return Optional.ofNullable(addressRequest)
            .map(address -> AddressPropertiesEntity.builder()
                    .line1(address.getLine1())
                    .line2(address.getLine2())
                    .line3(address.getLine3())
                    .line4(address.getLine4())
                    .line5(address.getLine5())
                    .postcode(address.getPostcode())
                    .build())
            .orElse(null);
    }
}
