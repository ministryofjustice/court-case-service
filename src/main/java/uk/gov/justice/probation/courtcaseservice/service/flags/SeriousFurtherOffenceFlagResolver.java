package uk.gov.justice.probation.courtcaseservice.service.flags;

import kotlin.Pair;
import org.springframework.stereotype.Component;
import uk.gov.justice.probation.courtcaseservice.application.FeatureFlags;
import uk.gov.justice.probation.courtcaseservice.jpa.dto.HearingDefendantDTO;
import uk.gov.justice.probation.courtcaseservice.jpa.entity.CourtCaseEntity;
import uk.gov.justice.probation.courtcaseservice.jpa.entity.DefendantEntity;
import uk.gov.justice.probation.courtcaseservice.jpa.entity.HearingEntity;
import uk.gov.justice.probation.courtcaseservice.jpa.entity.OffenceSfoMappingEntity;
import uk.gov.justice.probation.courtcaseservice.jpa.repository.OffenceSfoMappingRepository;

import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Component
public class SeriousFurtherOffenceFlagResolver {

    private final OffenceSfoMappingRepository offenceSfoMappingRepository;
    private final FeatureFlags featureFlags;
    private final OffenceFlagHelper offenceFlagHelper;

    public SeriousFurtherOffenceFlagResolver(final OffenceSfoMappingRepository offenceSfoMappingRepository,
                                             final FeatureFlags featureFlags,
                                             final OffenceFlagHelper offenceFlagHelper) {
        this.offenceSfoMappingRepository = offenceSfoMappingRepository;
        this.featureFlags = featureFlags;
        this.offenceFlagHelper = offenceFlagHelper;
    }

    public Map<String, Boolean> buildSeriousFurtherOffenceFlagsMap(List<Pair<CourtCaseEntity, DefendantEntity>> results) {
        var allOffenceCodes = offenceFlagHelper.offenceCodesForResults(results, hearing -> true);
        return offenceSfoMappingRepository.findByOffenceCodeIn(allOffenceCodes).stream()
            .collect(Collectors.toMap(OffenceSfoMappingEntity::getOffenceCode, OffenceSfoMappingEntity::isSeriousFurtherOffenceFlag));
    }

    public Map<String, Boolean> buildSeriousFurtherOffenceFlagsMapFromHearing(HearingEntity hearingEntity) {
        var allOffenceCodes = offenceFlagHelper.offenceCodesForHearing(hearingEntity, hearing -> true);
        return offenceSfoMappingRepository.findByOffenceCodeIn(allOffenceCodes).stream()
            .collect(Collectors.toMap(OffenceSfoMappingEntity::getOffenceCode, OffenceSfoMappingEntity::isSeriousFurtherOffenceFlag));
    }

    public Boolean resolveSeriousFurtherOffenceFlagFromHearing(HearingEntity hearingEntity, DefendantEntity defendant, Map<String, Boolean> sfoFlagsByCode) {
        if (!featureFlags.enableSeriousFurtherOffence()) return false;
        var offenceCodes = offenceFlagHelper.offenceCodesForDefendant(hearingEntity, defendant.getDefendantId(), hearing -> true);
        return offenceFlagHelper.resolveFlag(offenceCodes, sfoFlagsByCode);
    }

    public Map<String, Boolean> buildSeriousFurtherOffenceFlagsMapFromDTOs(List<HearingDefendantDTO> defendants) {
        var allOffenceCodes = offenceFlagHelper.offenceCodesForDTOs(defendants, defendant -> true);
        return offenceSfoMappingRepository.findByOffenceCodeIn(allOffenceCodes).stream()
            .collect(Collectors.toMap(OffenceSfoMappingEntity::getOffenceCode, OffenceSfoMappingEntity::isSeriousFurtherOffenceFlag));
    }

    public Boolean resolveSeriousFurtherOffenceFlag(CourtCaseEntity courtCase, DefendantEntity defendant, Map<String, Boolean> seriousFurtherOffenceFlagsByCode) {
        if (!featureFlags.enableSeriousFurtherOffence()) return false;
        var offenceCodes = offenceFlagHelper.offenceCodesForDefendant(courtCase, defendant.getDefendantId(), hearing -> true);
        return offenceFlagHelper.resolveFlag(offenceCodes, seriousFurtherOffenceFlagsByCode);
    }

    public Boolean resolveSeriousFurtherOffenceFlagFromDTO(HearingDefendantDTO defendant, Map<String, Boolean> seriousFurtherOffenceFlagsByCode) {
        if (!featureFlags.enableSeriousFurtherOffence()) return false;
        var offenceCodes = offenceFlagHelper.offenceCodesForDefendantDTO(defendant);
        return offenceFlagHelper.resolveFlag(offenceCodes, seriousFurtherOffenceFlagsByCode);
    }
}
