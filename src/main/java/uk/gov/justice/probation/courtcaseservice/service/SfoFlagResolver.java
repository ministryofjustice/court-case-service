package uk.gov.justice.probation.courtcaseservice.service;

import kotlin.Pair;
import org.springframework.stereotype.Component;
import uk.gov.justice.probation.courtcaseservice.jpa.entity.CourtCaseEntity;
import uk.gov.justice.probation.courtcaseservice.jpa.entity.DefendantEntity;
import uk.gov.justice.probation.courtcaseservice.jpa.entity.HearingEntity;
import uk.gov.justice.probation.courtcaseservice.jpa.entity.OffenceEntity;
import uk.gov.justice.probation.courtcaseservice.jpa.entity.OffenceSfoMappingEntity;
import uk.gov.justice.probation.courtcaseservice.jpa.repository.OffenceSfoMappingRepository;

import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

@Component
public class SfoFlagResolver {

    private final OffenceSfoMappingRepository offenceSfoMappingRepository;

    public SfoFlagResolver(final OffenceSfoMappingRepository offenceSfoMappingRepository) {
        this.offenceSfoMappingRepository = offenceSfoMappingRepository;
    }

    public Map<String, Boolean> buildSfoFlagsMap(List<Pair<CourtCaseEntity, DefendantEntity>> results) {
        var allOffenceCodes = results.stream()
            .flatMap(pair -> offenceCodesForDefendant(pair.getFirst(), pair.getSecond().getDefendantId()).stream())
            .collect(Collectors.toSet());
        return offenceSfoMappingRepository.findByOffenceCodeIn(allOffenceCodes).stream()
            .collect(Collectors.toMap(OffenceSfoMappingEntity::getOffenceCode, OffenceSfoMappingEntity::isSfoFlag));
    }

    public Boolean resolveSfoFlag(CourtCaseEntity courtCase, DefendantEntity defendant, Map<String, Boolean> sfoFlagsByCode) {
        if (!defendant.isOffenderConfirmed()) {
            return null;
        }
        var offenceCodes = offenceCodesForDefendant(courtCase, defendant.getDefendantId());
        return offenceCodes.isEmpty() ? null
            : offenceCodes.stream().anyMatch(code -> Boolean.TRUE.equals(sfoFlagsByCode.get(code)));
    }

    private Set<String> offenceCodesForDefendant(CourtCaseEntity courtCase, String defendantId) {
        return courtCase.getHearings().stream()
            .map(HearingEntity::getHearingDefendants)
            .flatMap(Collection::stream)
            .filter(hd -> hd.getDefendantId().equalsIgnoreCase(defendantId))
            .flatMap(hd -> hd.getOffences().stream())
            .map(OffenceEntity::getOffenceCode)
            .filter(code -> code != null && !code.isBlank())
            .collect(Collectors.toSet());
    }
}

