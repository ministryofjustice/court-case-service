package uk.gov.justice.probation.courtcaseservice.service;

import java.util.Optional;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import uk.gov.justice.probation.courtcaseservice.jpa.entity.CourtCaseEntity;
import uk.gov.justice.probation.courtcaseservice.jpa.entity.CourtEntity;
import uk.gov.justice.probation.courtcaseservice.jpa.repository.CourtCaseRepository;
import uk.gov.justice.probation.courtcaseservice.jpa.repository.CourtRepository;
import uk.gov.justice.probation.courtcaseservice.service.exceptions.EntityNotFoundException;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.InputMismatchException;
import java.util.List;

@Service
@Slf4j
public class CourtCaseService {

    private final CourtRepository courtRepository;
    private final CourtCaseRepository courtCaseRepository;

    public CourtCaseService(CourtRepository courtRepository, CourtCaseRepository courtCaseRepository) {
        super();
        this.courtRepository = courtRepository;
        this.courtCaseRepository = courtCaseRepository;
    }

    private void checkCourtByCode(String courtCode) throws EntityNotFoundException {
        if (courtRepository.findByCourtCode(courtCode) == null) {
            throw new EntityNotFoundException(String.format("Court %s not found", courtCode));
        }
    }

    private CourtCaseEntity createCase(CourtCaseEntity courtCaseEntity) {
        log.info("Court case created for case number {}", courtCaseEntity.getCaseNo());
        return courtCaseRepository.save(courtCaseEntity);
    }

    public CourtCaseEntity getCaseByCaseNumber(String courtCode, String caseNo) throws EntityNotFoundException {
        checkCourtByCode(courtCode);
        log.info("Court case requested for court {} for case {}", courtCode, caseNo);
        return courtCaseRepository.findByCourtCodeAndCaseNo(courtCode, caseNo)
            .orElseThrow(() -> new EntityNotFoundException(String.format("Case %s not found for court %s", caseNo, courtCode)));
    }

    public CourtCaseEntity createOrUpdateCase(String caseId, CourtCaseEntity courtCaseEntity) throws EntityNotFoundException, InputMismatchException {
        checkCourtByCode(courtCaseEntity.getCourtCode());
        String bodyCaseId = courtCaseEntity.getCaseId();
        if (!caseId.equals(bodyCaseId)) {
            throw new InputMismatchException(String.format("Case ID %s does not match with %s", caseId, bodyCaseId));
        }

        return processAndSave(courtCaseEntity, courtCaseRepository.findByCaseId(caseId));
    }

    public CourtCaseEntity createOrUpdateCase(String courtCode, String caseNo, CourtCaseEntity courtCaseEntity) throws EntityNotFoundException, InputMismatchException {
        checkCourtByCode(courtCaseEntity.getCourtCode());
        if (!caseNo.equals(courtCaseEntity.getCaseNo()) || !courtCode.equals(courtCaseEntity.getCourtCode()) ) {
            throw new InputMismatchException(String.format("Case No %s and Court Code %s do not match with values from body %s and %s",
                caseNo, courtCode, courtCaseEntity.getCaseNo(), courtCaseEntity.getCourtCode()));
        }

        return processAndSave(courtCaseEntity,  courtCaseRepository.findByCourtCodeAndCaseNo(courtCode, caseNo));
    }

    public List<CourtCaseEntity> filterCasesByCourtAndDate(String courtCode, LocalDate date) {
        CourtEntity court = courtRepository.findByCourtCode(courtCode);

        if (court == null) {
            throw new EntityNotFoundException("Court %s not found", courtCode);
        }
        LocalDateTime start = LocalDateTime.of(date, LocalTime.MIDNIGHT);
        return courtCaseRepository.findByCourtCodeAndSessionStartTimeBetween(court.getCourtCode(), start, start.plusDays(1));
    }

    private CourtCaseEntity processAndSave(CourtCaseEntity courtCaseEntity, Optional<CourtCaseEntity> existingCourtCaseEntity) {

        linkOffencesToCourtCase(courtCaseEntity);

        if (existingCourtCaseEntity.isEmpty()) {
            return createCase(courtCaseEntity);
        }

        return updateAndSave(existingCourtCaseEntity.get(), courtCaseEntity);
    }

    private CourtCaseEntity updateAndSave(CourtCaseEntity existingCase, CourtCaseEntity courtCaseEntity) {
        // We have checked and matched court ode and case no. They are immutable fields. No need to update.
        existingCase.setCourtRoom(courtCaseEntity.getCourtRoom());
        existingCase.setProbationStatus(courtCaseEntity.getProbationStatus());
        existingCase.setSessionStartTime(courtCaseEntity.getSessionStartTime());
        existingCase.setPreviouslyKnownTerminationDate(courtCaseEntity.getPreviouslyKnownTerminationDate());
        existingCase.setSuspendedSentenceOrder(courtCaseEntity.getSuspendedSentenceOrder());
        existingCase.setBreach(courtCaseEntity.getBreach());
        existingCase.setDefendantName(courtCaseEntity.getDefendantName());
        existingCase.setDefendantAddress(courtCaseEntity.getDefendantAddress());
        existingCase.setPnc(courtCaseEntity.getPnc());
        existingCase.setListNo(courtCaseEntity.getListNo());
        existingCase.setNationality1(courtCaseEntity.getNationality1());
        existingCase.setNationality2(courtCaseEntity.getNationality2());
        existingCase.setLastUpdated(LocalDateTime.now());
        log.info("Court case updated for case no {}", courtCaseEntity.getCaseNo());
        return courtCaseRepository.save(existingCase);
    }

    private void linkOffencesToCourtCase(CourtCaseEntity courtCaseEntity) {
        courtCaseEntity.getOffences().forEach(offence -> offence.setCourtCase(courtCaseEntity));
    }
}
