package uk.gov.justice.probation.courtcaseservice.service;

import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import uk.gov.justice.probation.courtcaseservice.jpa.entity.CourtCaseEntity;
import uk.gov.justice.probation.courtcaseservice.jpa.repository.CourtCaseRepository;
import uk.gov.justice.probation.courtcaseservice.service.exceptions.EntityNotFoundException;

import java.util.List;


@Service
@Slf4j
public class CourtCaseService {

    public CourtCaseService(CourtCaseRepository courtCaseRepository) {
        this.courtCaseRepository = courtCaseRepository;
    }

    private CourtCaseRepository courtCaseRepository;

    public CourtCaseEntity getCaseByCaseNumber(String courtCode, String caseNo){
        var courtCaseEntity = courtCaseRepository.findByCaseNo(caseNo);
        if(!courtCode.equals("SHF")) {
            throw new EntityNotFoundException(String.format("Court %s not found", courtCode));
        }
        if(courtCaseEntity == null) {
            throw new EntityNotFoundException(String.format("Case %s not found", caseNo));
        }
        log.info("retrieved case for case number {}", caseNo);
        return courtCaseEntity;
    }

    public List<CourtCaseEntity> getAllCases(String courtCode) {
        var cases = courtCaseRepository.findAll();
        if(!courtCode.equals("SHF")) {
            throw new EntityNotFoundException(String.format("Court %s not found", courtCode));
        }
        log.info("retrieved all cases for court {}", courtCode);
        return cases;
    }





}
