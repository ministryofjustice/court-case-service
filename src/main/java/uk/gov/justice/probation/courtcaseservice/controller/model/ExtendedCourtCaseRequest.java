package uk.gov.justice.probation.courtcaseservice.controller.model;

import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import uk.gov.justice.probation.courtcaseservice.jpa.entity.AddressPropertiesEntity;
import uk.gov.justice.probation.courtcaseservice.jpa.entity.CourtCaseEntity;
import uk.gov.justice.probation.courtcaseservice.jpa.entity.DefendantEntity;
import uk.gov.justice.probation.courtcaseservice.jpa.entity.DefendantOffenceEntity;
import uk.gov.justice.probation.courtcaseservice.jpa.entity.HearingEntity;
import uk.gov.justice.probation.courtcaseservice.jpa.entity.OffenceEntity;
import uk.gov.justice.probation.courtcaseservice.jpa.entity.SourceType;

import java.util.Collections;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

@Data
@Builder
@NoArgsConstructor(access = AccessLevel.PRIVATE, force = true)
@AllArgsConstructor
public class ExtendedCourtCaseRequest {
    static final SourceType DEFAULT_SOURCE = SourceType.COMMON_PLATFORM;
    private final String caseNo;
    private final String caseId;
    private final String courtCode;
    private final String source;
    private final List<HearingDay> hearingDays;
    private final List<Defendant> defendants;

    public CourtCaseEntity asCourtCaseEntity() {

        final var hearingDayEntities = Optional.ofNullable(hearingDays).orElse(Collections.emptyList())
            .stream()
            .map(this::buildHearing)
            .collect(Collectors.toList());
        final var defendantEntities = Optional.ofNullable(defendants).orElse(Collections.emptyList())
            .stream()
            .map(this::buildDefendant)
            .collect(Collectors.toList());
        final var offenceEntities = buildOffences(defendantEntities);

        final var courtCaseEntity = buildLegacyFields(defendantEntities, hearingDayEntities)
            .offences(offenceEntities)
            .hearings(hearingDayEntities)
            .defendants(defendantEntities)
            .caseNo(caseNo)
            .courtCode(courtCode)
            .caseId(caseId)
            .sourceType(SourceType.valueOf(Optional.ofNullable(source).orElse(DEFAULT_SOURCE.name())))
            .build();

        hearingDayEntities.forEach(hearingEntity -> hearingEntity.setCourtCase(courtCaseEntity));
        defendantEntities.forEach(defendantEntity -> defendantEntity.setCourtCase(courtCaseEntity));
        offenceEntities.forEach(offenceEntity -> offenceEntity.setCourtCase(courtCaseEntity));
        return courtCaseEntity;
    }

    private DefendantEntity buildDefendant(Defendant defendant) {

        final var offences = buildDefendantOffences(defendant.getOffences());

        final var defendantEntity = DefendantEntity.builder()
            .address(buildAddress(defendant.getAddress()))
            .awaitingPsr(defendant.getAwaitingPsr())
            .breach(defendant.getBreach())
            .crn(defendant.getCrn())
            .cro(defendant.getCro())
            .dateOfBirth(defendant.getDateOfBirth())
            .defendantName(defendant.getName().getFullName())
            .name(defendant.getName())
            .offences(offences)
            .pnc(defendant.getPnc())
            .preSentenceActivity(defendant.getPreSentenceActivity())
            .previouslyKnownTerminationDate(defendant.getPreviouslyKnownTerminationDate())
            .probationStatus(defendant.getProbationStatus())
            .sex(defendant.getSex())
            .suspendedSentenceOrder(defendant.getSuspendedSentenceOrder())
            .type(defendant.getType())
            .defendantId(defendant.getDefendantId())
            .build();
        offences.forEach(offence -> offence.setDefendant(defendantEntity));
        return defendantEntity;
    }

    private List<DefendantOffenceEntity> buildDefendantOffences(List<OffenceRequest> offences) {

        return IntStream.range(0, Optional.ofNullable(offences)
                                    .map(List::size)
                                    .orElse(0))
            .mapToObj(i -> {
                var offence = offences.get(i);
                return DefendantOffenceEntity.builder()
                    .sequence(i + 1)
                    .title(offence.getOffenceTitle())
                    .summary(offence.getOffenceSummary())
                    .act(offence.getAct())
                    .build();
            })
            .collect(Collectors.toList());
    }

    private HearingEntity buildHearing(HearingDay hearingDay) {
        return HearingEntity.builder()
            .courtCode(hearingDay.getCourtCode())
            .courtRoom(hearingDay.getCourtRoom())
            .listNo(hearingDay.getListNo())
            .hearingTime(hearingDay.getSessionStartTime().toLocalTime())
            .hearingDay(hearingDay.getSessionStartTime().toLocalDate())
            .build();
    }

    private AddressPropertiesEntity buildAddress(AddressRequest addressRequest) {
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

    // All these fields will be removed into the defendant and hearings
    @Deprecated(forRemoval = true)
    private CourtCaseEntity.CourtCaseEntityBuilder buildLegacyFields(List<DefendantEntity> defendantEntities, List<HearingEntity> hearingEntities) {

        final var firstDefendant = defendantEntities.stream().findFirst();
        final var firstHearingDay = hearingEntities.stream().findFirst();

        return CourtCaseEntity.builder()
            // Top level fields to be retired into the Defendant
            .awaitingPsr(firstDefendant.map(DefendantEntity::getAwaitingPsr).orElse(null))
            .defendantAddress(firstDefendant.map(DefendantEntity::getAddress).orElse(null))
            .defendantName(firstDefendant.map(DefendantEntity::getDefendantName).orElse(null))
            .name(firstDefendant.map(DefendantEntity::getName).orElse(null))
            .defendantType(firstDefendant.map(DefendantEntity::getType).orElse(null))
            .crn(firstDefendant.map(DefendantEntity::getCrn).orElse(null))
            .pnc(firstDefendant.map(DefendantEntity::getPnc).orElse(null))
            .cro(firstDefendant.map(DefendantEntity::getCro).orElse(null))
            .defendantDob(firstDefendant.map(DefendantEntity::getDateOfBirth).orElse(null))
            .defendantSex(firstDefendant.map(DefendantEntity::getSex).orElse(null))
            .previouslyKnownTerminationDate(firstDefendant.map(DefendantEntity::getPreviouslyKnownTerminationDate).orElse(null))
            .suspendedSentenceOrder(firstDefendant.map(DefendantEntity::getSuspendedSentenceOrder).orElse(null))
            .breach(firstDefendant.map(DefendantEntity::getBreach).orElse(null))
            .preSentenceActivity(firstDefendant.map(DefendantEntity::getPreSentenceActivity).orElse(null))
            .probationStatus(firstDefendant.map(DefendantEntity::getProbationStatus).orElse(null))

            // Top level fields to be retired into the Hearing
            .courtRoom(firstHearingDay.map(HearingEntity::getCourtRoom).orElse(null))
            .sessionStartTime(firstHearingDay.map(HearingEntity::getSessionStartTime).orElse(null))
            .listNo(firstHearingDay.map(HearingEntity::getListNo).orElse(null));
    }

    // Top level offence will be moved to the defendant
    @Deprecated(forRemoval = true)
    private List<OffenceEntity> buildOffences(List<DefendantEntity> defendantEntities) {
        final var offences = defendantEntities.stream().findFirst()
            .map(DefendantEntity::getOffences)
            .orElse(Collections.emptyList());
        return IntStream.range(0, Optional.ofNullable(offences).map(List::size).orElse(0))
            .mapToObj(i -> {
                var offence = offences.get(i);
                return OffenceEntity.builder()
                    .sequenceNumber(offence.getSequence())
                    .offenceTitle(offence.getTitle())
                    .offenceSummary(offence.getSummary())
                    .act(offence.getAct())
                    .build();
            })
            .collect(Collectors.toList());
    }

}