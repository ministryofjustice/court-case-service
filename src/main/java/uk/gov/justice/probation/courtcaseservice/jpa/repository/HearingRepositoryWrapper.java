package uk.gov.justice.probation.courtcaseservice.jpa.repository;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Repository;
import uk.gov.justice.probation.courtcaseservice.jpa.entity.HearingEntity;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

@Repository("customisedRepository")
public class HearingRepositoryWrapper implements HearingRepositoryBase {

    private final HearingRepository hearingRepository;

    @Autowired
    public HearingRepositoryWrapper(HearingRepository hearingRepository) {
        this.hearingRepository = hearingRepository;
    }

    @Override
    public Optional<HearingEntity> findFirstByHearingIdOrderByIdDesc(String hearingId) {
        return hearingRepository.findFirstByHearingIdOrderByIdDesc(hearingId);
    }

    @Override
    public Optional<HearingEntity> findByCourtCodeAndCaseNo(String courtCode, String caseNo) {
        return hearingRepository.findByCourtCodeAndCaseNo(courtCode, caseNo);
    }

    @Override
    public Optional<HearingEntity> findByCaseId(String caseId) {
        return hearingRepository.findByCaseId(caseId);
    }

    @Override
    public Optional<HearingEntity> findByHearingIdAndDefendantId(String hearingId, String defendantId) {
        return hearingRepository.findByHearingIdAndDefendantId(hearingId, defendantId);
    }

    @Override
    public List<HearingEntity> findByCourtCodeAndHearingDay(String courtCode, LocalDate hearingDay, LocalDateTime createdAfter, LocalDateTime createdBefore) {
        return hearingRepository.findByCourtCodeAndHearingDay(courtCode, hearingDay, createdAfter, createdBefore);
    }

    @Override
    public Optional<LocalDateTime> findLastModifiedByHearingDay(String courtCode, LocalDate hearingDay) {
        return hearingRepository.findLastModifiedByHearingDay(courtCode, hearingDay);
    }

    public HearingEntity saveCustomised(HearingEntity hearingEntity) {
        return hearingRepository.save(hearingEntity);
    }
}
