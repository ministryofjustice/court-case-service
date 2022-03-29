package uk.gov.justice.probation.courtcaseservice.service.mapper;

import uk.gov.justice.probation.courtcaseservice.jpa.entity.DefendantEntity;
import uk.gov.justice.probation.courtcaseservice.jpa.entity.HearingDayEntity;
import uk.gov.justice.probation.courtcaseservice.jpa.entity.HearingDefendantEntity;
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

    public static HearingEntity mergeDefendantsOnHearing(HearingEntity existingHearing, HearingEntity updatedHearing, String defendantId) {
        final var existingHearingDefendants = Optional.ofNullable(existingHearing.getHearingDefendants()).orElse(Collections.emptyList());
        final var existingHearingDays = CourtCaseMapper.createHearings(existingHearing.getHearingDays());
        if (existingHearingDefendants.size() <= 1) {
            final var caseToSave = updatedHearing.withHearingDays(existingHearingDays);
            applyParentToCollections(caseToSave);
            return caseToSave;
        }

        final var allDefendants = existingHearing.getHearingDefendants().stream()
            .map(existingDefendant -> defendantId.equalsIgnoreCase(existingDefendant.getDefendantId()) ?
                                updatedHearing.getHearingDefendants().get(0) : CourtCaseMapper.createHearingDefendant(existingDefendant))
            .collect(Collectors.toList());

        // rebuild the case with new defendants and hearings
        final var caseToSave = updatedHearing.withHearingDefendants(allDefendants)
                                                        .withHearingDays(CourtCaseMapper.createHearings(existingHearing.getHearingDays()));
        applyParentToCollections(caseToSave);
        return caseToSave;
    }

    private static void applyParentToCollections(final HearingEntity hearingToSave) {
        Optional.ofNullable(hearingToSave.getHearingDefendants()).orElse(Collections.emptyList())
            .forEach(hearingDefendantEntity -> hearingDefendantEntity.setHearing(hearingToSave));

        Optional.ofNullable(hearingToSave.getHearingDays()).orElse(Collections.emptyList())
            .forEach(hearingDayEntity -> hearingDayEntity.setHearing(hearingToSave));
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

    public static HearingDefendantEntity createHearingDefendant(HearingDefendantEntity hearingDefendantEntity) {

        final var defendantEntity = Optional.of(hearingDefendantEntity).map(HearingDefendantEntity::getDefendant).orElseThrow();
        var newHearingDefendantEntity = HearingDefendantEntity.builder()
            .defendantId(defendantEntity.getDefendantId())
            .defendant(DefendantEntity.builder()
                .defendantId(defendantEntity.getDefendantId())
                .defendantName(defendantEntity.getDefendantName())
                .name(defendantEntity.getName())
                .type(defendantEntity.getType())
                .address(defendantEntity.getAddress())
                .offender(defendantEntity.getOffender())
                .crn(defendantEntity.getCrn())
                .pnc(defendantEntity.getPnc())
                .cro(defendantEntity.getCro())
                .dateOfBirth(defendantEntity.getDateOfBirth())
                .sex(defendantEntity.getSex())
                .nationality1(defendantEntity.getNationality1())
                .nationality2(defendantEntity.getNationality2())
            .build())
            .offences(Optional.ofNullable(hearingDefendantEntity.getOffences()).orElse(Collections.emptyList())
                .stream()
                .map(CourtCaseMapper::createDefendantOffence)
                .collect(Collectors.toList()))
            .build();

        newHearingDefendantEntity.getOffences().forEach(offenceEntity -> offenceEntity.setHearingDefendant(newHearingDefendantEntity));
        return newHearingDefendantEntity;
    }

    static OffenceEntity createDefendantOffence(OffenceEntity offenceEntity) {
        return OffenceEntity.builder()
            .act(offenceEntity.getAct())
            .summary(offenceEntity.getSummary())
            .title(offenceEntity.getTitle())
            .sequence(offenceEntity.getSequence())
            .listNo(offenceEntity.getListNo())
            .build();
    }
}
