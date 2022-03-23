package uk.gov.justice.probation.courtcaseservice.jpa.repository;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;
import uk.gov.justice.probation.courtcaseservice.jpa.entity.DefendantEntity;
import uk.gov.justice.probation.courtcaseservice.jpa.entity.HearingDefendantEntity;
import uk.gov.justice.probation.courtcaseservice.jpa.entity.HearingEntity;
import uk.gov.justice.probation.courtcaseservice.jpa.entity.OffenderEntity;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Objects;
import java.util.Optional;
import java.util.stream.Collectors;

@Repository
@Transactional
/**
 * An intermediary between the Service layer and the JPA repository intended to present a clean interface
 * to Service layer consumers, abstracting away custom code required for maintaining immutable records.
 */
public class HearingRepositoryFacade {
    private static final int MAX_YEAR_SUPPORTED_BY_DB = 294276;
    private static final int MIN_YEAR_SUPPORTED_BY_DB = -4712;

    private final OffenderRepository offenderRepository;
    private final HearingRepository hearingRepository;
    private final DefendantRepository defendantRepository;

    @Autowired
    public HearingRepositoryFacade(OffenderRepository offenderRepository, HearingRepository hearingRepository, DefendantRepository defendantRepository) {
        this.offenderRepository = offenderRepository;
        this.hearingRepository = hearingRepository;
        this.defendantRepository = defendantRepository;
    }

    public Optional<HearingEntity> findFirstByHearingIdOrderByIdDesc(String hearingId) {
        return hearingRepository.findFirstByHearingIdOrderByCreatedDesc(hearingId)
                .map(this::updateWithDefendants);
    }

    public Optional<HearingEntity> findByCourtCodeAndCaseNo(String courtCode, String caseNo) {
        return hearingRepository.findByCourtCodeAndCaseNo(courtCode, caseNo)
                .map(this::updateWithDefendants);
    }

    @Deprecated(forRemoval = true)
    /**
     * @deprecated Strictly speaking this finds by hearingId which equals caseId only for a subset of cases created
     * before hearingId was fully rolled out. Using it after this point will produce unexpected results.To be removed
     * as part of PIC-2062.
     */
    public Optional<HearingEntity> findByCaseId(String caseId) {
        final var firstByHearingIdOrderByIdDesc = hearingRepository.findFirstByHearingIdOrderByCreatedDesc(caseId);
        return firstByHearingIdOrderByIdDesc
                .map(this::updateWithDefendants);
    }

    @Deprecated(forRemoval = true)
    /**
     * @deprecated Strictly speaking this finds by hearingId which equals caseId only for a subset of cases created
     * before hearingId was fully rolled out. Using it after this point will produce unexpected results.To be removed
     * as part of PIC-2062.
     */
    public Optional<HearingEntity> findByCaseIdAndDefendantId(String caseId, String defendantId) {

        return hearingRepository.findByHearingId(caseId)
                .map(hearingEntity -> findDefendant(hearingEntity, defendantId).isPresent() ? updateWithDefendants(hearingEntity) : null);
    }

    @Deprecated
    /**
     * @deprecated Deprecated in favour of the version without createdAfter and createdBefore parameters as the lookup is
     * significantly more efficient without these constraints.
     */
    public List<HearingEntity> findByCourtCodeAndHearingDay(String courtCode, LocalDate hearingDay, LocalDateTime createdAfter, LocalDateTime createdBefore) {

        return (canIgnoreCreatedDates(createdAfter, createdBefore)
                        ? hearingRepository.findByCourtCodeAndHearingDay(courtCode, hearingDay)
                        : hearingRepository.findByCourtCodeAndHearingDay(courtCode, hearingDay, createdAfter, createdBefore))
                .stream()
                .map(this::updateWithDefendants)
                .collect(Collectors.toList());
    }

    public List<HearingEntity> findByCourtCodeAndHearingDay(String courtCode, LocalDate hearingDay) {
        return hearingRepository.findByCourtCodeAndHearingDay(courtCode, hearingDay)
                .stream()
                .map(this::updateWithDefendants)
                .collect(Collectors.toList());
    }

    public Optional<LocalDateTime> findLastModifiedByHearingDay(String courtCode, LocalDate hearingDay) {
        return hearingRepository.findLastModifiedByHearingDay(courtCode, hearingDay);
    }

    public HearingEntity save(HearingEntity hearingEntity) {
        hearingEntity.getHearingDefendants().forEach((HearingDefendantEntity hearingDefendantEntity) -> {
            hearingDefendantEntity.setDefendant(
                    hearingDefendantEntity.getDefendant()
                    .withOffender(Optional.ofNullable(hearingDefendantEntity.getDefendant().getOffender())
                            .map(this::updateOffenderIfItExists)
                            .orElse(null)));
        });

        final var defendantEntities = hearingEntity.getHearingDefendants()
                .stream()
                .map(HearingDefendantEntity::getDefendant)
                .collect(Collectors.toList());
        final var offenderEntities = defendantEntities.stream()
                .map(DefendantEntity::getOffender)
                .filter(Objects::nonNull)
                .collect(Collectors.toList());

        offenderRepository.saveAll(offenderEntities);
        defendantRepository.saveAll(defendantEntities);
        return hearingRepository.save(hearingEntity);
    }

    private boolean canIgnoreCreatedDates(LocalDateTime createdAfter, LocalDateTime createdBefore) {
        return (createdAfter == null && createdBefore == null)
                || (createdAfter.getYear() <= MIN_YEAR_SUPPORTED_BY_DB && createdBefore.getYear() >= MAX_YEAR_SUPPORTED_BY_DB);
    }

    private Optional<HearingDefendantEntity> findDefendant(HearingEntity hearingEntity, String defendantId) {
        return hearingEntity.getHearingDefendants()
                .stream()
                .filter(hearingDefendantEntity -> defendantId.equals(hearingDefendantEntity.getDefendantId()))
                .findFirst();
    }

    private OffenderEntity updateOffenderIfItExists(OffenderEntity updatedOffender) {
        return offenderRepository.findByCrn(updatedOffender.getCrn())
                .map(existingOffender -> existingOffender
                        .withBreach(updatedOffender.isBreach())
                        .withAwaitingPsr(updatedOffender.getAwaitingPsr())
                        .withProbationStatus(updatedOffender.getProbationStatus())
                        .withPreviouslyKnownTerminationDate(updatedOffender.getPreviouslyKnownTerminationDate())
                        .withSuspendedSentenceOrder(updatedOffender.isSuspendedSentenceOrder())
                        .withPreSentenceActivity(updatedOffender.isPreSentenceActivity())
                )
                .orElse(updatedOffender);
    }

    private HearingEntity updateWithDefendants(HearingEntity hearingEntity) {
        return hearingEntity.withHearingDefendants(hearingEntity.getHearingDefendants()
                .stream()
                .map(hearingDefendantEntity -> updateDefendantAndOffender(hearingDefendantEntity, hearingEntity.getCaseId()))
                .collect(Collectors.toList()));
    }

    private HearingDefendantEntity updateDefendantAndOffender(HearingDefendantEntity hearingDefendantEntity, String caseId) {
        return defendantRepository.findFirstByDefendantIdOrderByIdDesc(hearingDefendantEntity.getDefendantId())
                .map(defendant -> {
                    hearingDefendantEntity.setDefendant(defendant);
                    Optional.ofNullable(defendant.getCrn())
                            .map(crn -> offenderRepository.findByCrn(crn)
                                    .orElseThrow(() -> new RuntimeException(String.format("Unexpected state: Offender with CRN '%s' is specified on defendant '%s' but it does not exist", crn, defendant.getDefendantId())))
                            )
                            .ifPresent(defendant::setOffender);
                    return hearingDefendantEntity;
                })
                .orElseThrow(() -> {
                            throw new RuntimeException(String.format("Unexpected state: Defendant '%s' is specified on case '%s' but it does not exist", hearingDefendantEntity.getDefendantId(), caseId));
                        }
                );
    }
}
