package uk.gov.justice.probation.courtcaseservice.service;

import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import uk.gov.justice.probation.courtcaseservice.jpa.entity.CourtEntity;
import uk.gov.justice.probation.courtcaseservice.jpa.entity.CourtCaseEntity;
import uk.gov.justice.probation.courtcaseservice.jpa.repository.CourtRepository;
import uk.gov.justice.probation.courtcaseservice.jpa.repository.CourtCaseRepository;
import uk.gov.justice.probation.courtcaseservice.service.exceptions.EntityNotFoundException;

@Service
@Slf4j
public class CourtCaseService {

    private final CourtRepository courtRepository;
    private final CourtCaseRepository courtCaseRepository;

    private CourtCaseEntity createCase(CourtCaseEntity courtCaseEntity) {
        log.info("Court case created for case number {}", courtCaseEntity.getCaseNo());
        return courtCaseRepository.save(courtCaseEntity);
    }

    public CourtCaseService(CourtRepository courtRepository, CourtCaseRepository courtCaseRepository) {
        this.courtRepository = courtRepository;
        this.courtCaseRepository = courtCaseRepository;
    }

    public CourtCaseEntity getCaseByCaseNumber(String courtCode, String caseNo) throws EntityNotFoundException {
        if (courtRepository.findByCourtCode(courtCode) == null) {
            throw new EntityNotFoundException(String.format("Court %s not found", courtCode));
        }
        CourtCaseEntity courtCaseEntity = courtCaseRepository.findByCaseNo(caseNo);
        if (courtCaseEntity == null) {
            throw new EntityNotFoundException(String.format("Case %s not found", caseNo));
        }
        log.info("Court case requested for court {} for case {}", courtCode, caseNo);
        return courtCaseEntity;
    }

    public CourtCaseEntity createOrUpdateCase(Long caseId, CourtCaseEntity courtCaseEntity) throws EntityNotFoundException {
        CourtCaseEntity existingCase = courtCaseRepository.findByCaseId(caseId);

        if (existingCase != null) {
            existingCase.setCaseNo(courtCaseEntity.getCaseNo());
            existingCase.setCourtId(courtCaseEntity.getCourtId());
            existingCase.setCourtRoom(courtCaseEntity.getCourtRoom());
            existingCase.setSessionStartTime(courtCaseEntity.getSessionStartTime());
            existingCase.setData(courtCaseEntity.getData());

            log.info("Court case updated for case {}", courtCaseEntity.getCaseNo());
            return courtCaseRepository.save(existingCase);
        } else {
            return createCase(courtCaseEntity);
        }
    }
}
