package uk.gov.justice.probation.courtcaseservice.service;

import kotlin.Pair;
import org.springframework.stereotype.Component;
import uk.gov.justice.probation.courtcaseservice.application.FeatureFlags;
import uk.gov.justice.probation.courtcaseservice.jpa.dto.HearingDefendantDTO;
import uk.gov.justice.probation.courtcaseservice.jpa.dto.OffenceDTO;
import uk.gov.justice.probation.courtcaseservice.jpa.entity.CourtCaseEntity;
import uk.gov.justice.probation.courtcaseservice.jpa.entity.DefendantEntity;
import uk.gov.justice.probation.courtcaseservice.jpa.entity.HearingEntity;
import uk.gov.justice.probation.courtcaseservice.jpa.entity.OffenceEntity;
import uk.gov.justice.probation.courtcaseservice.jpa.entity.OffenceMappaMappingEntity;
import uk.gov.justice.probation.courtcaseservice.jpa.repository.OffenceMappaMappingRepository;

import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;

@Component
public class MultiAgencyPublicProtectionArrangementsFlagResolver {

    static final Set<String> ELIGIBLE_HEARING_TYPES = Set.of("sentence", "sentence (prosecution to attend)");

    private final OffenceMappaMappingRepository offenceMappaMappingRepository;
    private final FeatureFlags featureFlags;

    public MultiAgencyPublicProtectionArrangementsFlagResolver(final OffenceMappaMappingRepository offenceMappaMappingRepository,
                                                               final FeatureFlags featureFlags) {
        this.offenceMappaMappingRepository = offenceMappaMappingRepository;
        this.featureFlags = featureFlags;
    }

    public Map<String, Boolean> buildMultiAgencyPublicProtectionArrangementsFlagsMap(List<Pair<CourtCaseEntity, DefendantEntity>> results) {
        var allOffenceCodes = results.stream()
            .flatMap(pair -> offenceCodesForDefendant(pair.getFirst(), pair.getSecond().getDefendantId()).stream())
            .collect(Collectors.toSet());
        return offenceMappaMappingRepository.findByOffenceCodeIn(allOffenceCodes).stream()
            .collect(Collectors.toMap(OffenceMappaMappingEntity::getOffenceCode, OffenceMappaMappingEntity::isMultiAgencyPublicProtectionArrangementsFlag));
    }

    public Map<String, Boolean> buildMultiAgencyPublicProtectionArrangementsFlagsMapFromHearing(HearingEntity hearingEntity) {
        if (!isEligibleHearingType(hearingEntity.getHearingType())) return Map.of();
        var allOffenceCodes = Optional.ofNullable(hearingEntity.getHearingDefendants()).orElse(List.of()).stream()
            .flatMap(hd -> Optional.ofNullable(hd.getOffences()).orElse(List.of()).stream())
            .map(OffenceEntity::getOffenceCode)
            .filter(code -> code != null && !code.isBlank())
            .collect(Collectors.toSet());
        return offenceMappaMappingRepository.findByOffenceCodeIn(allOffenceCodes).stream()
            .collect(Collectors.toMap(OffenceMappaMappingEntity::getOffenceCode, OffenceMappaMappingEntity::isMultiAgencyPublicProtectionArrangementsFlag));
    }

    public Boolean resolveMultiAgencyPublicProtectionArrangementsFlagFromHearing(HearingEntity hearingEntity, DefendantEntity defendant, Map<String, Boolean> multiAgencyPublicProtectionArrangementsFlagsByCode) {
        if (!featureFlags.enableMultiAgencyPublicProtectionArrangements()) return false;
        if (!isEligibleHearingType(hearingEntity.getHearingType())) return null;
        var offenceCodes = Optional.ofNullable(hearingEntity.getHearingDefendants()).orElse(List.of()).stream()
            .filter(hd -> hd.getDefendant() != null && hd.getDefendant().getDefendantId().equalsIgnoreCase(defendant.getDefendantId()))
            .flatMap(hd -> Optional.ofNullable(hd.getOffences()).orElse(List.of()).stream())
            .map(OffenceEntity::getOffenceCode)
            .filter(code -> code != null && !code.isBlank())
            .collect(Collectors.toSet());
        return offenceCodes.isEmpty() ? null
            : offenceCodes.stream().anyMatch(code -> Boolean.TRUE.equals(multiAgencyPublicProtectionArrangementsFlagsByCode.get(code)));
    }

    public Map<String, Boolean> buildMultiAgencyPublicProtectionArrangementsFlagsMapFromDTOs(List<HearingDefendantDTO> defendants) {
        var allOffenceCodes = defendants.stream()
            .filter(dto -> isEligibleHearingType(dto.getHearing() != null ? dto.getHearing().getHearingType() : null))
            .flatMap(dto -> offenceCodesForDefendantDTO(dto).stream())
            .collect(Collectors.toSet());
        return offenceMappaMappingRepository.findByOffenceCodeIn(allOffenceCodes).stream()
            .collect(Collectors.toMap(OffenceMappaMappingEntity::getOffenceCode, OffenceMappaMappingEntity::isMultiAgencyPublicProtectionArrangementsFlag));
    }

    public Boolean resolveMultiAgencyPublicProtectionArrangementsFlag(CourtCaseEntity courtCase, DefendantEntity defendant, Map<String, Boolean> multiAgencyPublicProtectionArrangementsFlagsByCode) {
        if (!featureFlags.enableMultiAgencyPublicProtectionArrangements()) return false;
        var offenceCodes = offenceCodesForDefendant(courtCase, defendant.getDefendantId());
        return offenceCodes.isEmpty() ? null
            : offenceCodes.stream().anyMatch(code -> Boolean.TRUE.equals(multiAgencyPublicProtectionArrangementsFlagsByCode.get(code)));
    }

    public Boolean resolveMultiAgencyPublicProtectionArrangementsFlagFromDTO(HearingDefendantDTO defendant, Map<String, Boolean> multiAgencyPublicProtectionArrangementsFlagsByCode) {
        if (!featureFlags.enableMultiAgencyPublicProtectionArrangements()) return false;
        if (!isEligibleHearingType(defendant.getHearing() != null ? defendant.getHearing().getHearingType() : null)) return null;
        var offenceCodes = offenceCodesForDefendantDTO(defendant);
        return offenceCodes.isEmpty() ? null
            : offenceCodes.stream().anyMatch(code -> Boolean.TRUE.equals(multiAgencyPublicProtectionArrangementsFlagsByCode.get(code)));
    }

    private Set<String> offenceCodesForDefendant(CourtCaseEntity courtCase, String defendantId) {
        return courtCase.getHearings().stream()
            .filter(hearing -> isEligibleHearingType(hearing.getHearingType()))
            .map(HearingEntity::getHearingDefendants)
            .flatMap(Collection::stream)
            .filter(hd -> hd.getDefendant() != null && hd.getDefendant().getDefendantId().equalsIgnoreCase(defendantId))
            .flatMap(hearingDefendantEntity -> hearingDefendantEntity.getOffences().stream())
            .map(OffenceEntity::getOffenceCode)
            .filter(offenceCode -> offenceCode != null && !offenceCode.isBlank())
            .collect(Collectors.toSet());
    }

    private Set<String> offenceCodesForDefendantDTO(HearingDefendantDTO defendant) {
        return Optional.ofNullable(defendant.getOffences()).orElse(List.of()).stream()
            .map(OffenceDTO::getOffenceCode)
            .filter(offenceCode -> offenceCode != null && !offenceCode.isBlank())
            .collect(Collectors.toSet());
    }

    private boolean isEligibleHearingType(String hearingType) {
        return hearingType != null && ELIGIBLE_HEARING_TYPES.contains(hearingType.toLowerCase());
    }
}
