package uk.gov.justice.probation.courtcaseservice.service.flags;

import kotlin.Pair;
import org.springframework.stereotype.Component;
import uk.gov.justice.probation.courtcaseservice.application.FeatureFlags;
import uk.gov.justice.probation.courtcaseservice.jpa.dto.HearingDefendantDTO;
import uk.gov.justice.probation.courtcaseservice.jpa.entity.CourtCaseEntity;
import uk.gov.justice.probation.courtcaseservice.jpa.entity.DefendantEntity;
import uk.gov.justice.probation.courtcaseservice.jpa.entity.HearingEntity;
import uk.gov.justice.probation.courtcaseservice.jpa.entity.OffenceMappaMappingEntity;
import uk.gov.justice.probation.courtcaseservice.jpa.repository.OffenceMappaMappingRepository;

import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

@Component
public class MultiAgencyPublicProtectionArrangementsFlagResolver {

    static final Set<String> ELIGIBLE_HEARING_TYPES = Set.of("sentence", "sentence (prosecution to attend)");

    private final OffenceMappaMappingRepository offenceMappaMappingRepository;
    private final FeatureFlags featureFlags;
    private final OffenceFlagHelper offenceFlagHelper;

    public MultiAgencyPublicProtectionArrangementsFlagResolver(final OffenceMappaMappingRepository offenceMappaMappingRepository,
                                                               final FeatureFlags featureFlags,
                                                               final OffenceFlagHelper offenceFlagHelper) {
        this.offenceMappaMappingRepository = offenceMappaMappingRepository;
        this.featureFlags = featureFlags;
        this.offenceFlagHelper = offenceFlagHelper;
    }

    public Map<String, Boolean> buildMultiAgencyPublicProtectionArrangementsFlagsMap(List<Pair<CourtCaseEntity, DefendantEntity>> results) {
        var allOffenceCodes = offenceFlagHelper.offenceCodesForResults(results, this::isEligibleHearingType);
        return offenceMappaMappingRepository.findByOffenceCodeIn(allOffenceCodes).stream()
            .collect(Collectors.toMap(OffenceMappaMappingEntity::getOffenceCode, OffenceMappaMappingEntity::isMultiAgencyPublicProtectionArrangementsFlag));
    }

    public Map<String, Boolean> buildMultiAgencyPublicProtectionArrangementsFlagsMapFromHearing(HearingEntity hearingEntity) {
        var allOffenceCodes = offenceFlagHelper.offenceCodesForHearing(hearingEntity, this::isEligibleHearingType);
        return offenceMappaMappingRepository.findByOffenceCodeIn(allOffenceCodes).stream()
            .collect(Collectors.toMap(OffenceMappaMappingEntity::getOffenceCode, OffenceMappaMappingEntity::isMultiAgencyPublicProtectionArrangementsFlag));
    }

    public Boolean resolveMultiAgencyPublicProtectionArrangementsFlagFromHearing(HearingEntity hearingEntity, DefendantEntity defendant, Map<String, Boolean> multiAgencyPublicProtectionArrangementsFlagsByCode) {
        if (!featureFlags.enableMultiAgencyPublicProtectionArrangements()) return false;
        var offenceCodes = offenceFlagHelper.offenceCodesForDefendant(hearingEntity, defendant.getDefendantId(), this::isEligibleHearingType);
        return offenceFlagHelper.resolveFlag(offenceCodes, multiAgencyPublicProtectionArrangementsFlagsByCode);
    }

    public Map<String, Boolean> buildMultiAgencyPublicProtectionArrangementsFlagsMapFromDTOs(List<HearingDefendantDTO> defendants) {
        var allOffenceCodes = offenceFlagHelper.offenceCodesForDTOs(defendants, this::isEligibleHearingType);
        return offenceMappaMappingRepository.findByOffenceCodeIn(allOffenceCodes).stream()
            .collect(Collectors.toMap(OffenceMappaMappingEntity::getOffenceCode, OffenceMappaMappingEntity::isMultiAgencyPublicProtectionArrangementsFlag));
    }

    public Boolean resolveMultiAgencyPublicProtectionArrangementsFlag(CourtCaseEntity courtCase, DefendantEntity defendant, Map<String, Boolean> multiAgencyPublicProtectionArrangementsFlagsByCode) {
        if (!featureFlags.enableMultiAgencyPublicProtectionArrangements()) return false;
        var offenceCodes = offenceFlagHelper.offenceCodesForDefendant(courtCase, defendant.getDefendantId(), this::isEligibleHearingType);
        return offenceFlagHelper.resolveFlag(offenceCodes, multiAgencyPublicProtectionArrangementsFlagsByCode);
    }

    public Boolean resolveMultiAgencyPublicProtectionArrangementsFlagFromDTO(HearingDefendantDTO defendant, Map<String, Boolean> multiAgencyPublicProtectionArrangementsFlagsByCode) {
        if (!featureFlags.enableMultiAgencyPublicProtectionArrangements()) return false;
        var offenceCodes = offenceFlagHelper.offenceCodesForDTOs(List.of(defendant), this::isEligibleHearingType);
        return offenceFlagHelper.resolveFlag(offenceCodes, multiAgencyPublicProtectionArrangementsFlagsByCode);
    }

    private boolean isEligibleHearingType(HearingEntity hearingEntity) {
        return isEligibleHearingType(hearingEntity.getHearingType());
    }

    private boolean isEligibleHearingType(HearingDefendantDTO defendant) {
        return isEligibleHearingType(defendant.getHearing() != null ? defendant.getHearing().getHearingType() : null);
    }

    private boolean isEligibleHearingType(String hearingType) {
        return hearingType != null && ELIGIBLE_HEARING_TYPES.contains(hearingType.toLowerCase());
    }
}
