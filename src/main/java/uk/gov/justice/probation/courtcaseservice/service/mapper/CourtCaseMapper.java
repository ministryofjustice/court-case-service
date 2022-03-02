package uk.gov.justice.probation.courtcaseservice.service.mapper;

import uk.gov.justice.probation.courtcaseservice.jpa.entity.DefendantEntity;
import uk.gov.justice.probation.courtcaseservice.jpa.entity.DefendantOffenceEntity;
import uk.gov.justice.probation.courtcaseservice.jpa.entity.HearingDayEntity;
import uk.gov.justice.probation.courtcaseservice.jpa.entity.HearingEntity;

import java.util.Collections;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

/**
 * This mapping class clones instances of the JPA entities, at all levels.
 *
 * It deliberately omits to clone the id value so that subsequent saves will create new records which is a key factor towards maintaining the immutable
 * database records.
 */
public class CourtCaseMapper {

    public static HearingEntity mergeDefendantsOnCase(HearingEntity existingCase, HearingEntity updatedCase, String defendantId) {
        final var existingDefendants = Optional.ofNullable(existingCase.getDefendants()).orElse(Collections.emptyList());
        final var existingHearings = CourtCaseMapper.createHearings(existingCase.getHearingDays());
        if (existingDefendants.size() <= 1) {
            final var caseToSave = updatedCase.withHearingDays(existingHearings);
            applyParentToCollections(caseToSave);
            return caseToSave;
        }

        final var allDefendants = existingCase.getDefendants().stream()
            .map(existingDefendant -> defendantId.equalsIgnoreCase(existingDefendant.getDefendantId()) ?
                                updatedCase.getDefendants().get(0) : CourtCaseMapper.createDefendant(existingDefendant, null))
            .collect(Collectors.toList());

        // rebuild the case with new defendants and hearings
        final var caseToSave = updatedCase.withDefendants(allDefendants)
                                                        .withHearingDays(CourtCaseMapper.createHearings(existingCase.getHearingDays()));
        applyParentToCollections(caseToSave);
        return caseToSave;
    }

    private static void applyParentToCollections(final HearingEntity caseToSave) {
        Optional.ofNullable(caseToSave.getDefendants()).orElse(Collections.emptyList())
            .forEach(defendantEntity -> defendantEntity.setHearing(caseToSave));
        Optional.ofNullable(caseToSave.getHearingDays()).orElse(Collections.emptyList())
            .forEach(hearingEntity -> hearingEntity.setHearing(caseToSave));
    }

    public static HearingDayEntity createHearing(HearingDayEntity hearing) {
        return HearingDayEntity.builder()
            .courtCode(hearing.getCourtCode())
            .time(hearing.getTime())
            .day(hearing.getDay())
            .courtRoom(hearing.getCourtRoom())
            .listNo(hearing.getListNo())
            .build();
    }

    static List<HearingDayEntity> createHearings(List<HearingDayEntity> hearings) {
        return Optional.ofNullable(hearings).orElse(Collections.emptyList())
            .stream()
            .map(CourtCaseMapper::createHearing)
            .collect(Collectors.toList());
    }

    public static DefendantEntity createDefendant(DefendantEntity defendantEntity, String newProbationStatus) {

        var newDefendantEntity = DefendantEntity.builder()
            .defendantId(defendantEntity.getDefendantId())
            .defendantName(defendantEntity.getDefendantName())
            .name(defendantEntity.getName())
            .type(defendantEntity.getType())
            .address(defendantEntity.getAddress())
            .offender(defendantEntity.getOffender())
            .pnc(defendantEntity.getPnc())
            .cro(defendantEntity.getCro())
            .dateOfBirth(defendantEntity.getDateOfBirth())
            .sex(defendantEntity.getSex())
            .nationality1(defendantEntity.getNationality1())
            .nationality2(defendantEntity.getNationality2())
            .offences(Optional.ofNullable(defendantEntity.getOffences()).orElse(Collections.emptyList())
                .stream()
                .map(CourtCaseMapper::createDefendantOffence)
                .collect(Collectors.toList()))
            .build();

        newDefendantEntity.getOffences().forEach(offenceEntity -> offenceEntity.setDefendant(newDefendantEntity));
        return newDefendantEntity;
    }

    static DefendantOffenceEntity createDefendantOffence(DefendantOffenceEntity offenceEntity) {
        return DefendantOffenceEntity.builder()
            .act(offenceEntity.getAct())
            .summary(offenceEntity.getSummary())
            .title(offenceEntity.getTitle())
            .sequence(offenceEntity.getSequence())
            .listNo(offenceEntity.getListNo())
            .build();
    }
}
