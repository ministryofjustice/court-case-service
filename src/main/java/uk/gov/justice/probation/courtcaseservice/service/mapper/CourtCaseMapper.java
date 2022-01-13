package uk.gov.justice.probation.courtcaseservice.service.mapper;

import uk.gov.justice.probation.courtcaseservice.jpa.entity.CourtCaseEntity;
import uk.gov.justice.probation.courtcaseservice.jpa.entity.DefendantEntity;
import uk.gov.justice.probation.courtcaseservice.jpa.entity.DefendantOffenceEntity;
import uk.gov.justice.probation.courtcaseservice.jpa.entity.HearingEntity;
import uk.gov.justice.probation.courtcaseservice.jpa.entity.OffenceEntity;

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

    public static CourtCaseEntity mergeDefendantsOnCase(CourtCaseEntity existingCase, CourtCaseEntity updatedCase, String defendantId) {
        final var existingDefendants = Optional.ofNullable(existingCase.getDefendants()).orElse(Collections.emptyList());
        final var existingHearings = CourtCaseMapper.createHearings(existingCase.getHearings());
        if (existingDefendants.size() <= 1) {
            final var caseToSave = updatedCase.withHearings(existingHearings);
            applyParentToCollections(caseToSave);
            return caseToSave;
        }

        final var allDefendants = existingCase.getDefendants().stream()
            .map(existingDefendant -> defendantId.equalsIgnoreCase(existingDefendant.getDefendantId()) ?
                                updatedCase.getDefendants().get(0) : CourtCaseMapper.createDefendant(existingDefendant, null))
            .collect(Collectors.toList());

        // rebuild the case with new defendants and hearings
        final var caseToSave = updatedCase.withDefendants(allDefendants)
                                                        .withHearings(CourtCaseMapper.createHearings(existingCase.getHearings()));
        applyParentToCollections(caseToSave);
        return caseToSave;
    }

    private static void applyParentToCollections(final CourtCaseEntity caseToSave) {
        Optional.ofNullable(caseToSave.getDefendants()).orElse(Collections.emptyList())
            .forEach(defendantEntity -> defendantEntity.setCourtCase(caseToSave));
        Optional.ofNullable(caseToSave.getHearings()).orElse(Collections.emptyList())
            .forEach(hearingEntity -> hearingEntity.setCourtCase(caseToSave));
        Optional.ofNullable(caseToSave.getOffences()).orElse(Collections.emptyList())
            .forEach(offenceEntity -> offenceEntity.setCourtCase(caseToSave));
    }

    public static HearingEntity createHearing(HearingEntity hearing) {
        return HearingEntity.builder()
            .courtCode(hearing.getCourtCode())
            .hearingTime(hearing.getHearingTime())
            .hearingDay(hearing.getHearingDay())
            .courtRoom(hearing.getCourtRoom())
            .listNo(hearing.getListNo())
            .build();
    }

    static List<HearingEntity> createHearings(List<HearingEntity> hearings) {
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
            .build();
    }

    /**
     * @deprecated offences directly on the court case will be retired in favour of DefendantOffenceEntity linked to DefendantEntity
     */
    @Deprecated(forRemoval = true)
    static OffenceEntity createOffence(OffenceEntity offenceEntity) {
        return OffenceEntity.builder()
            .act(offenceEntity.getAct())
            .offenceSummary(offenceEntity.getOffenceSummary())
            .offenceTitle(offenceEntity.getOffenceTitle())
            .sequenceNumber(offenceEntity.getSequenceNumber())
            .build();
    }


}
