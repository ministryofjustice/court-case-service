package uk.gov.justice.probation.courtcaseservice.service.flags;

import kotlin.Pair;
import org.springframework.stereotype.Component;
import uk.gov.justice.probation.courtcaseservice.jpa.dto.HearingDefendantDTO;
import uk.gov.justice.probation.courtcaseservice.jpa.dto.OffenceDTO;
import uk.gov.justice.probation.courtcaseservice.jpa.entity.CourtCaseEntity;
import uk.gov.justice.probation.courtcaseservice.jpa.entity.DefendantEntity;
import uk.gov.justice.probation.courtcaseservice.jpa.entity.HearingEntity;
import uk.gov.justice.probation.courtcaseservice.jpa.entity.OffenceEntity;

import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;

@Component
public class OffenceFlagHelper {

    public Set<String> offenceCodesForResults(List<Pair<CourtCaseEntity, DefendantEntity>> results) {
        return results.stream()
            .flatMap(pair -> offenceCodesForDefendant(pair.getFirst(), pair.getSecond().getDefendantId()).stream())
            .collect(Collectors.toSet());
    }

    public Set<String> offenceCodesForHearing(HearingEntity hearingEntity) {
        return Optional.ofNullable(hearingEntity.getHearingDefendants()).orElse(List.of()).stream()
            .flatMap(hd -> Optional.ofNullable(hd.getOffences()).orElse(List.of()).stream())
            .map(OffenceEntity::getOffenceCode)
            .filter(this::hasValue)
            .collect(Collectors.toSet());
    }

    public Set<String> offenceCodesForDefendant(HearingEntity hearingEntity,
                                                String defendantId) {
        return Optional.ofNullable(hearingEntity.getHearingDefendants()).orElse(List.of()).stream()
            .filter(hd -> hd.getDefendant() != null && hd.getDefendant().getDefendantId().equalsIgnoreCase(defendantId))
            .flatMap(hd -> Optional.ofNullable(hd.getOffences()).orElse(List.of()).stream())
            .map(OffenceEntity::getOffenceCode)
            .filter(this::hasValue)
            .collect(Collectors.toSet());
    }

    public Set<String> offenceCodesForDTOs(List<HearingDefendantDTO> defendants) {
        return defendants.stream()
            .flatMap(dto -> offenceCodesForDefendantDTO(dto).stream())
            .collect(Collectors.toSet());
    }

    public Set<String> offenceCodesForDefendant(CourtCaseEntity courtCaseEntity,
                                                String defendantId) {
        return courtCaseEntity.getHearings().stream()
            .map(HearingEntity::getHearingDefendants)
            .flatMap(Collection::stream)
            .filter(hd -> hd.getDefendant() != null && hd.getDefendant().getDefendantId().equalsIgnoreCase(defendantId))
            .flatMap(hearingDefendantEntity -> hearingDefendantEntity.getOffences().stream())
            .map(OffenceEntity::getOffenceCode)
            .filter(this::hasValue)
            .collect(Collectors.toSet());
    }

    public Set<String> offenceCodesForDefendantDTO(HearingDefendantDTO defendant) {
        return Optional.ofNullable(defendant.getOffences()).orElse(List.of()).stream()
            .map(OffenceDTO::getOffenceCode)
            .filter(this::hasValue)
            .collect(Collectors.toSet());
    }

    public Boolean resolveFlag(Set<String> offenceCodes, Map<String, Boolean> flagsByCode) {
        return offenceCodes.isEmpty() ? null
            : offenceCodes.stream().anyMatch(code -> Boolean.TRUE.equals(flagsByCode.get(code)));
    }

    private boolean hasValue(String offenceCode) {
        return offenceCode != null && !offenceCode.isBlank();
    }
}
