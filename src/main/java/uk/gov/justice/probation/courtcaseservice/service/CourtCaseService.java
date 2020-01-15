package uk.gov.justice.probation.courtcaseservice.service;

import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import uk.gov.justice.probation.courtcaseservice.jpa.entity.CourtEntity;
import uk.gov.justice.probation.courtcaseservice.jpa.entity.CourtCaseEntity;
import uk.gov.justice.probation.courtcaseservice.jpa.repository.CourtRepository;
import uk.gov.justice.probation.courtcaseservice.jpa.repository.CourtCaseRepository;
import uk.gov.justice.probation.courtcaseservice.service.exceptions.EntityNotFoundException;

import java.util.InputMismatchException;

@Service
@Slf4j
public class CourtCaseService {

    private final CourtRepository courtRepository;
    private final CourtCaseRepository courtCaseRepository;

    private void checkCourtByCode(String courtCode) throws EntityNotFoundException {
        CourtEntity courtEntity = courtRepository.findByCourtCode(courtCode);
        if (courtEntity == null) {
            throw new EntityNotFoundException(String.format("Court %s not found", courtCode));
        }
    }

    private CourtCaseEntity createCase(CourtCaseEntity courtCaseEntity) {
        log.info("Court case created for case number {}", courtCaseEntity.getCaseNo());
        return courtCaseRepository.save(courtCaseEntity);
    }

    public CourtCaseService(CourtRepository courtRepository, CourtCaseRepository courtCaseRepository) {
        this.courtRepository = courtRepository;
        this.courtCaseRepository = courtCaseRepository;
    }

    public CourtCaseEntity getCaseByCaseNumber(String courtCode, String caseNo) throws EntityNotFoundException {
        checkCourtByCode(courtCode);
        CourtCaseEntity courtCaseEntity = courtCaseRepository.findByCaseNo(caseNo);
        if (courtCaseEntity == null) {
            throw new EntityNotFoundException(String.format("Case %s not found", caseNo));
        }
        log.info("Court case requested for court {} for case {}", courtCode, caseNo);
        return courtCaseEntity;
    }

    public CourtCaseEntity createOrUpdateCase(String caseId, CourtCaseEntity courtCaseEntity) throws EntityNotFoundException, InputMismatchException {
        checkCourtByCode(courtCaseEntity.getCourtCode());
        String bodyCaseId = courtCaseEntity.getCaseId();
        if (!caseId.equals(bodyCaseId)) {
            throw new InputMismatchException(String.format("Case ID %s does not match with %s", caseId, bodyCaseId));
        }
        CourtCaseEntity existingCase = courtCaseRepository.findByCaseId(caseId);

        if (existingCase == null) {
            return createCase(courtCaseEntity);
        }

        existingCase.setCaseId(caseId);
        existingCase.setCaseNo(courtCaseEntity.getCaseNo());
        existingCase.setCourtCode(courtCaseEntity.getCourtCode());
        existingCase.setCourtRoom(courtCaseEntity.getCourtRoom());
        existingCase.setProbationRecord(courtCaseEntity.getProbationRecord());
        existingCase.setSessionStartTime(courtCaseEntity.getSessionStartTime());
        existingCase.setData(courtCaseEntity.getData());

        log.info("Court case updated for case {}", courtCaseEntity.getCaseNo());
        return courtCaseRepository.save(existingCase);
    }
}
