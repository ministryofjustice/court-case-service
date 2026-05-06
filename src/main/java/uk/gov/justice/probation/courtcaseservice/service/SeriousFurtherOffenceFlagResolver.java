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
import uk.gov.justice.probation.courtcaseservice.jpa.entity.OffenceSfoMappingEntity;
import uk.gov.justice.probation.courtcaseservice.jpa.repository.OffenceSfoMappingRepository;

import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;

@Component
public class SeriousFurtherOffenceFlagResolver {

    private final OffenceSfoMappingRepository offenceSfoMappingRepository;
    private final FeatureFlags featureFlags;

    public SeriousFurtherOffenceFlagResolver(final OffenceSfoMappingRepository offenceSfoMappingRepository,
                                             final FeatureFlags featureFlags) {
        this.offenceSfoMappingRepository = offenceSfoMappingRepository;
        this.featureFlags = featureFlags;
    }

    public Map<String, Boolean> buildSeriousFurtherOffenceFlagsMap(List<Pair<CourtCaseEntity, DefendantEntity>> results) {
        var allOffenceCodes = results.stream()
            .flatMap(pair -> offenceCodesForDefendant(pair.getFirst(), pair.getSecond().getDefendantId()).stream())
            .collect(Collectors.toSet());
        return offenceSfoMappingRepository.findByOffenceCodeIn(allOffenceCodes).stream()
            .collect(Collectors.toMap(OffenceSfoMappingEntity::getOffenceCode, OffenceSfoMappingEntity::isSeriousFurtherOffenceFlag));
    }

    public Map<String, Boolean> buildSeriousFurtherOffenceFlagsMapFromHearing(HearingEntity hearingEntity) {
        var allOffenceCodes = Optional.ofNullable(hearingEntity.getHearingDefendants()).orElse(List.of()).stream()
            .flatMap(hd -> Optional.ofNullable(hd.getOffences()).orElse(List.of()).stream())
            .map(OffenceEntity::getOffenceCode)
            .filter(code -> code != null && !code.isBlank())
            .collect(Collectors.toSet());
        return offenceSfoMappingRepository.findByOffenceCodeIn(allOffenceCodes).stream()
            .collect(Collectors.toMap(OffenceSfoMappingEntity::getOffenceCode, OffenceSfoMappingEntity::isSeriousFurtherOffenceFlag));
    }

    public Boolean resolveSeriousFurtherOffenceFlagFromHearing(HearingEntity hearingEntity, DefendantEntity defendant, Map<String, Boolean> sfoFlagsByCode) {
        if (!featureFlags.enableSeriousFurtherOffence()) return false;
        var offenceCodes = Optional.ofNullable(hearingEntity.getHearingDefendants()).orElse(List.of()).stream()
            .filter(hd -> hd.getDefendant() != null && hd.getDefendant().getDefendantId().equalsIgnoreCase(defendant.getDefendantId()))
            .flatMap(hd -> Optional.ofNullable(hd.getOffences()).orElse(List.of()).stream())
            .map(OffenceEntity::getOffenceCode)
            .filter(code -> code != null && !code.isBlank())
            .collect(Collectors.toSet());
        return offenceCodes.isEmpty() ? null
            : offenceCodes.stream().anyMatch(code -> Boolean.TRUE.equals(sfoFlagsByCode.get(code)));
    }

    public Map<String, Boolean> buildSeriousFurtherOffenceFlagsMapFromDTOs(List<HearingDefendantDTO> defendants) {
        var allOffenceCodes = defendants.stream()
            .flatMap(dto -> offenceCodesForDefendantDTO(dto).stream())
            .collect(Collectors.toSet());
        return offenceSfoMappingRepository.findByOffenceCodeIn(allOffenceCodes).stream()
            .collect(Collectors.toMap(OffenceSfoMappingEntity::getOffenceCode, OffenceSfoMappingEntity::isSeriousFurtherOffenceFlag));
    }

    public Boolean resolveSeriousFurtherOffenceFlag(CourtCaseEntity courtCase, DefendantEntity defendant, Map<String, Boolean> seriousFurtherOffenceFlagsByCode) {
        if (!featureFlags.enableSeriousFurtherOffence()) return false;
        var offenceCodes = offenceCodesForDefendant(courtCase, defendant.getDefendantId());
        return offenceCodes.isEmpty() ? null
            : offenceCodes.stream().anyMatch(code -> Boolean.TRUE.equals(seriousFurtherOffenceFlagsByCode.get(code)));
    }

    public Boolean resolveSeriousFurtherOffenceFlagFromDTO(HearingDefendantDTO defendant, Map<String, Boolean> seriousFurtherOffenceFlagsByCode) {
        if (!featureFlags.enableSeriousFurtherOffence()) return false;
        var offenceCodes = offenceCodesForDefendantDTO(defendant);
        return offenceCodes.isEmpty() ? null
            : offenceCodes.stream().anyMatch(code -> Boolean.TRUE.equals(seriousFurtherOffenceFlagsByCode.get(code)));
    }

    private Set<String> offenceCodesForDefendant(CourtCaseEntity courtCase, String defendantId) {
        return courtCase.getHearings().stream()
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
}
