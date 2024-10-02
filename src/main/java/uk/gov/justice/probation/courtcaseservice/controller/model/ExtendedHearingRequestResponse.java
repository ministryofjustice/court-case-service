package uk.gov.justice.probation.courtcaseservice.controller.model;

import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import uk.gov.justice.probation.courtcaseservice.jpa.entity.AddressPropertiesEntity;
import uk.gov.justice.probation.courtcaseservice.jpa.entity.CaseMarkerEntity;
import uk.gov.justice.probation.courtcaseservice.jpa.entity.CourtCaseEntity;
import uk.gov.justice.probation.courtcaseservice.jpa.entity.DefendantEntity;
import uk.gov.justice.probation.courtcaseservice.jpa.entity.HearingDayEntity;
import uk.gov.justice.probation.courtcaseservice.jpa.entity.HearingDefendantEntity;
import uk.gov.justice.probation.courtcaseservice.jpa.entity.HearingEntity;
import uk.gov.justice.probation.courtcaseservice.jpa.entity.HearingEventType;
import uk.gov.justice.probation.courtcaseservice.jpa.entity.JudicialResultEntity;
import uk.gov.justice.probation.courtcaseservice.jpa.entity.OffenceEntity;
import uk.gov.justice.probation.courtcaseservice.jpa.entity.OffenderEntity;
import uk.gov.justice.probation.courtcaseservice.jpa.entity.OffenderProbationStatus;
import uk.gov.justice.probation.courtcaseservice.jpa.entity.PleaEntity;
import uk.gov.justice.probation.courtcaseservice.jpa.entity.Sex;
import uk.gov.justice.probation.courtcaseservice.jpa.entity.SourceType;
import uk.gov.justice.probation.courtcaseservice.jpa.entity.VerdictEntity;

import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotEmpty;
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
public class ExtendedHearingRequestResponse {
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

    private final String hearingEventType;

    private final String hearingType;
    private final String listNo;
    private final List<CaseMarker> caseMarkers;

    private final LocalDateTime lastUpdated;

    public static ExtendedHearingRequestResponse of(HearingEntity hearing) {
        return ExtendedHearingRequestResponse.builder()
                .caseNo(hearing.getCaseNo())
                .caseId(hearing.getCaseId())
                .hearingId(hearing.getHearingId())
                .listNo(hearing.getListNo())
                .urn(hearing.getCourtCase().getUrn())
                .source(hearing.getSourceType().name())
                .hearingEventType(Optional.ofNullable(hearing.getHearingEventType()).map(HearingEventType::getName).orElse(null))
                .hearingType(hearing.getHearingType())
                .lastUpdated(hearing.getLastUpdated())
                .hearingDays(hearing.getHearingDays().stream()
                        .map(hearingDayEntity -> HearingDay.builder()
                                .courtCode(hearingDayEntity.getCourtCode())
                                .courtRoom(hearingDayEntity.getCourtRoom())
                                .sessionStartTime(Optional.ofNullable(hearingDayEntity.getDay())
                                        .map(day -> LocalDateTime.of(day, Optional.ofNullable(hearingDayEntity.getTime()).orElse(LocalTime.MIDNIGHT)))
                                        .orElse(null))
                                .listNo(hearing.getListNo())
                                .build())
                        .toList())
                .caseMarkers(buildCaseMarkers(hearing.getCourtCase()))
                .defendants(hearing.getHearingDefendants().stream()
                        .map(hearingDefendantEntity -> {
                            final var defendant = hearingDefendantEntity.getDefendant();
                            return Defendant.builder()
                                    .defendantId(defendant.getDefendantId())
                                    .personId(defendant.getPersonId())
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
                                    .crn(defendant.getCrn())
                                    .probationStatus(Optional.ofNullable(defendant.getOffender()).map(offender -> offender.getProbationStatus().name()).orElse(null))
                                    .awaitingPsr(Optional.ofNullable(defendant.getOffender()).map(OffenderEntity::getAwaitingPsr).orElse(null))
                                    .breach(Optional.ofNullable(defendant.getOffender()).map(OffenderEntity::isBreach).orElse(null))
                                    .preSentenceActivity(Optional.ofNullable(defendant.getOffender()).map(OffenderEntity::isPreSentenceActivity).orElse(null))
                                    .suspendedSentenceOrder(Optional.ofNullable(defendant.getOffender()).map(OffenderEntity::isSuspendedSentenceOrder).orElse(null))
                                    .previouslyKnownTerminationDate(Optional.ofNullable(defendant.getOffender()).map(OffenderEntity::getPreviouslyKnownTerminationDate).orElse(null))
                                    .phoneNumber(PhoneNumber.of(defendant.getPhoneNumber()))
                                    .offender(Offender.builder().pnc(Optional.ofNullable(defendant.getOffender()).map(OffenderEntity::getPnc).orElse(null)).build())
                                    .confirmedOffender(defendant.isOffenderConfirmed())
                                    .offences(Optional.of(hearingDefendantEntity)
                                            .map(HearingDefendantEntity::getOffences)
                                            .orElse(Collections.emptyList()).stream()
                                            .sorted(Comparator.comparingInt(OffenceEntity::getSequence))
                                            .map(offenceEntity -> OffenceRequestResponse.builder()
                                                    .act(offenceEntity.getAct())
                                                    .offenceTitle(offenceEntity.getTitle())
                                                    .offenceSummary(offenceEntity.getSummary())
                                                    .offenceCode(offenceEntity.getOffenceCode())
                                                    .listNo(offenceEntity.getListNo())
                                                    .plea(buildPleaFromEntity(offenceEntity))
                                                    .verdict(buildVerdictFromEntity(offenceEntity))
                                                    .judicialResults(Optional.of(offenceEntity)
                                                            .map(OffenceEntity::getJudicialResults)
                                                            .orElse(Collections.emptyList()).stream()
                                                            .map(judicialResultEntity -> JudicialResult.builder()
                                                                    .isConvictedResult(judicialResultEntity.isConvictedResult())
                                                                    .label(judicialResultEntity.getLabel())
                                                                    .judicialResultTypeId(judicialResultEntity.getJudicialResultTypeId())
                                                                    .resultText(judicialResultEntity.getResultText())
                                                                    .build())
                                                            .toList())
                                                    .build())
                                            .toList())

                                    .build();
                        })
                        .toList())
                .build();
    }

    private static List<CaseMarker> buildCaseMarkers(CourtCaseEntity courtCaseEntity) {
        return Optional.ofNullable(courtCaseEntity.getCaseMarkers())
                .map(caseMarkersList -> caseMarkersList.stream()
                        .map(caseMarkerEntity -> CaseMarker.builder()
                                .markerTypeDescription(caseMarkerEntity.getTypeDescription())
                                .build())
                        .toList()).orElse(null);

    }

    private static Plea buildPleaFromEntity(OffenceEntity offenceEntity) {
        if (offenceEntity.getPlea() != null) {
            return Plea.builder()
                    .pleaValue(offenceEntity.getPlea().getValue())
                    .pleaDate(offenceEntity.getPlea().getDate())
                    .build();
        }
        return null;
    }

    private static Verdict buildVerdictFromEntity(OffenceEntity offenceEntity) {
        if (offenceEntity.getVerdict() != null) {
            return Verdict.builder()
                    .verdictType(VerdictType.builder().description(offenceEntity.getVerdict().getTypeDescription()).build())
                    .date(offenceEntity.getVerdict().getDate())
                    .build();
        }
        return null;
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
                        .personId(defendant.getPersonId())
                        .build())
                .offences(offences)
                .build();
        offences.forEach(offence -> offence.setHearingDefendant(hearingDefendantEntity));
        offences.forEach(offenceEntity -> offenceEntity.getJudicialResults().forEach(judicialResultEntity -> judicialResultEntity.setOffence(offenceEntity)));
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

    public HearingEntity asHearingEntity() {

        List<HearingDay> hearingDays = Optional.ofNullable(this.hearingDays).orElse(Collections.emptyList());
        final var hearingDayEntities = hearingDays
                .stream()
                .map(this::buildHearingDayEntity)
                .toList();
        final var hearingDefendantEntities = Optional.ofNullable(defendants).orElse(Collections.emptyList())
                .stream()
                .map(this::buildDefendant)
                .toList();

        List<CaseMarker> caseMarkers = Optional.ofNullable(this.caseMarkers).orElse(Collections.emptyList());
        final var caseMarkerEntities = caseMarkers.stream()
                .map(this::buildCaseMarkerEntity)
                .toList();

        final var hearingEntity = HearingEntity.builder()
                .hearingDays(hearingDayEntities)
                .hearingDefendants(hearingDefendantEntities)
                .courtCase(CourtCaseEntity.builder()
                        .caseNo(caseNo)
                        .caseId(caseId)
                        .urn(urn)
                        .sourceType(SourceType.valueOf(Optional.ofNullable(source).orElse(DEFAULT_SOURCE.name())))
                        .caseMarkers(caseMarkerEntities)
                        .build())
                .hearingId(Optional.ofNullable(hearingId).orElse(caseId))
                .hearingEventType(HearingEventType.fromString(hearingEventType))
                .hearingType(hearingType)
                .listNo(
                        Optional.ofNullable(this.getListNo()).orElseGet(
                                () -> !hearingDays.isEmpty() ? hearingDays.get(0).getListNo() : null
                        )
                )
                .build();

        hearingDayEntities.forEach(hearingDayEntity -> hearingDayEntity.setHearing(hearingEntity));
        hearingDefendantEntities.forEach(hearingDefendantEntity -> hearingDefendantEntity.setHearing(hearingEntity));
        caseMarkerEntities.forEach(caseMarkerEntity -> caseMarkerEntity.setCourtCase(hearingEntity.getCourtCase()));
        return hearingEntity;
    }

    private CaseMarkerEntity buildCaseMarkerEntity(CaseMarker caseMarker) {
        return CaseMarkerEntity.builder()
                .typeDescription(caseMarker.getMarkerTypeDescription())
                .build();
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
                            .judicialResults(buildJudicialResults(offence.getJudicialResults()))
                            .offenceCode(offence.getOffenceCode())
                            .plea(buildPleaEntity(offence))
                            .verdict(buildVerdictEntity(offence))
                            .build();
                })
                .collect(Collectors.toList());
    }

    private PleaEntity buildPleaEntity(OffenceRequestResponse offence) {
        if (offence.getPlea() != null) {
            return PleaEntity.builder()
                    .value(offence.getPlea().getPleaValue())
                    .date(offence.getPlea().getPleaDate())
                    .build();
        }
        return null;
    }

    private VerdictEntity buildVerdictEntity(OffenceRequestResponse offence) {
        if (offence.getVerdict() != null) {
            return VerdictEntity.builder().
                    typeDescription(offence.getVerdict().getVerdictType().getDescription())
                    .date(offence.getVerdict().getDate())
                    .build();
        }
        return null;
    }

    private List<JudicialResultEntity> buildJudicialResults(List<JudicialResult> judicialResults) {

        return IntStream.range(0, Optional.ofNullable(judicialResults)
                        .map(List::size)
                        .orElse(0))
                .mapToObj(i -> {
                    var judicialResult = judicialResults.get(i);
                    return JudicialResultEntity.builder()
                            .isConvictedResult(judicialResult.isConvictedResult())
                            .label(judicialResult.getLabel())
                            .judicialResultTypeId(judicialResult.getJudicialResultTypeId())
                            .resultText(judicialResult.getResultText())
                            .build();
                })
                .collect(Collectors.toList());
    }

    private HearingDayEntity buildHearingDayEntity(HearingDay hearingDay) {
        return HearingDayEntity.builder()
                .courtCode(hearingDay.getCourtCode())
                .courtRoom(hearingDay.getCourtRoom())
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

    private List<CaseMarkerEntity> buildCaseMarkers(List<CaseMarker> caseMarkers) {
        return IntStream.range(0, Optional.ofNullable(caseMarkers)
                        .map(List::size)
                        .orElse(0))
                .mapToObj(i -> {
                    var caseMarker = caseMarkers.get(i);
                    return CaseMarkerEntity.builder()
                            .typeDescription(caseMarker.getMarkerTypeDescription())
                            .build();
                })
                .collect(Collectors.toList());
    }
}
