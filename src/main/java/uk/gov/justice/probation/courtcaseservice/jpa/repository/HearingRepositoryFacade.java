package uk.gov.justice.probation.courtcaseservice.jpa.repository;

import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;
import uk.gov.justice.probation.courtcaseservice.jpa.entity.HearingDefendantEntity;
import uk.gov.justice.probation.courtcaseservice.jpa.entity.HearingEntity;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Objects;
import java.util.Optional;

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
    private final OffenderRepositoryFacade offenderRepositoryFacade;
    private final HearingRepository hearingRepository;
    private final DefendantRepository defendantRepository;
    private final CaseCommentsRepository caseCommentsRepository;

    @Autowired
    public HearingRepositoryFacade(OffenderRepository offenderRepository, OffenderRepositoryFacade offenderRepositoryFacade,
                                   HearingRepository hearingRepository, DefendantRepository defendantRepository,
                                   CaseCommentsRepository caseCommentsRepository) {
        this.offenderRepository = offenderRepository;
        this.offenderRepositoryFacade = offenderRepositoryFacade;
        this.hearingRepository = hearingRepository;
        this.defendantRepository = defendantRepository;
        this.caseCommentsRepository = caseCommentsRepository;
    }

    public Optional<HearingEntity> findFirstByHearingIdOrderByIdDesc(String hearingId) {
        return hearingRepository.findFirstByHearingIdOrderByIdDesc(hearingId);
    }

    public Optional<HearingEntity> findByCourtCodeAndCaseNo(String courtCode, String caseNo, String listNo) {
        Optional<HearingEntity> hearing;
        if(StringUtils.isEmpty(listNo)) {
            hearing = hearingRepository.findByCourtCodeAndCaseNo(courtCode, caseNo);
        } else {
            hearing = hearingRepository.findByCourtCodeCaseNoAndListNo(courtCode, caseNo, listNo)
                .or(
                    () -> hearingRepository.findByCourtCodeAndCaseNo(courtCode, caseNo).map(hearingEntity -> hearingEntity.withHearingId(null))
                );
        }
        return hearing;
    }

    public Optional<HearingEntity> findByHearingIdAndDefendantId(String hearingId, String defendantId) {
        return hearingRepository.findFirstByHearingIdOrderByIdDesc(hearingId)
            .map(hearingEntity -> {
                return Objects.nonNull(hearingEntity.getHearingDefendant(defendantId)) ? hearingEntity : null;
            })
            .map(hearingEntity -> {
                hearingEntity.getCourtCase().setCaseComments(caseCommentsRepository.findAllByCaseIdAndDeletedFalse(hearingEntity.getCaseId()));
                return hearingEntity;
            });
    }

    @Deprecated
    /**
     * @deprecated Deprecated in favour of the version without createdAfter and createdBefore parameters as the lookup is
     * significantly more efficient without these constraints.
     */
    public List<HearingEntity> findByCourtCodeAndHearingDay(String courtCode, LocalDate hearingDay, LocalDateTime createdAfter, LocalDateTime createdBefore) {

        return canIgnoreCreatedDates(createdAfter, createdBefore)
            ? hearingRepository.findByCourtCodeAndHearingDay(courtCode, hearingDay)
            : hearingRepository.findByCourtCodeAndHearingDay(courtCode, hearingDay, createdAfter, createdBefore);
    }

    public List<HearingEntity> findByCourtCodeAndHearingDay(String courtCode, LocalDate hearingDay) {
        return hearingRepository.findByCourtCodeAndHearingDay(courtCode, hearingDay);
    }

    public Optional<LocalDateTime> findLastModifiedByHearingDay(String courtCode, LocalDate hearingDay) {
        return hearingRepository.findLastModifiedByHearingDay(courtCode, hearingDay);
    }
    public HearingEntity save(HearingEntity hearingEntity) {

        updateWithExistingOffenders(hearingEntity);

        updatedWithExistingDefendantsFromDb(hearingEntity);

        HearingEntity save = hearingRepository.save(hearingEntity);
        return save;
    }

    // TODO delete
    private void populateDefendants(HearingDefendantEntity hearingDefendantEntity) {
        hearingDefendantEntity.setDefendant(
            defendantRepository.findFirstByDefendantIdOrderByIdDesc(hearingDefendantEntity.getDefendantId())
                .map(defendantEntity -> {
                    Optional.ofNullable(defendantEntity.getCrn())
                        .flatMap(this.offenderRepository::findByCrn)
                        .ifPresent(offenderEntity -> defendantEntity.setOffender(offenderEntity));
                    return defendantEntity;
                })
                .orElse(null)
        );
    }

    private void updateWithExistingOffenders(HearingEntity hearingEntity) {
        hearingEntity.getHearingDefendants().stream().filter(hearingDefendantEntity ->
                Objects.nonNull(hearingDefendantEntity.getDefendant().getOffender()) &&
                    Objects.isNull(hearingDefendantEntity.getDefendant().getOffender().getId()))
            .forEach((HearingDefendantEntity hearingDefendantEntity) -> {
                var defendant = hearingDefendantEntity.getDefendant();
                defendant.setOffender(offenderRepositoryFacade.upsertOffender(defendant.getOffender()));
        });
    }

    private void updatedWithExistingDefendantsFromDb(HearingEntity hearingEntity) {
        // Check if incoming defendant already exists in the database and update
        hearingEntity.getHearingDefendants().stream()
            .filter(hearingDefendant -> Objects.isNull(hearingDefendant.getDefendant().getId())) // ID not null means this defendant has already been fetched and updated
            .forEach(defendantUpdate -> defendantRepository.findFirstByDefendantIdOrderByIdDesc(defendantUpdate.getDefendantId())
                .ifPresent(dbDefendant -> {
                    dbDefendant.update(defendantUpdate.getDefendant());
                    defendantUpdate.setDefendant(dbDefendant);
                }));
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
}
