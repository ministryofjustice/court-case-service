package uk.gov.justice.probation.courtcaseservice.service;

import static java.util.stream.Collectors.toMap;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.Collection;
import java.util.Comparator;
import java.util.InputMismatchException;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Collectors;
import java.util.stream.IntStream;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import uk.gov.justice.probation.courtcaseservice.jpa.entity.CourtCaseEntity;
import uk.gov.justice.probation.courtcaseservice.jpa.entity.CourtEntity;
import uk.gov.justice.probation.courtcaseservice.jpa.entity.OffenceEntity;
import uk.gov.justice.probation.courtcaseservice.jpa.repository.CourtCaseRepository;
import uk.gov.justice.probation.courtcaseservice.jpa.repository.CourtRepository;
import uk.gov.justice.probation.courtcaseservice.service.exceptions.EntityNotFoundException;

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
        courtRepository.findByCourtCode(courtCode)
            .orElseThrow(() -> new EntityNotFoundException(String.format("Court %s not found", courtCode)));
    }

    private CourtCaseEntity createCase(CourtCaseEntity courtCaseEntity) {
        log.info("Court case being created for case number {}", courtCaseEntity.getCaseNo());
        return courtCaseRepository.save(courtCaseEntity);
    }

    public CourtCaseEntity getCaseByCaseNumber(String courtCode, String caseNo) throws EntityNotFoundException {
        checkCourtByCode(courtCode);
        log.info("Court case requested for court {} for case {}", courtCode, caseNo);
        return courtCaseRepository.findByCourtCodeAndCaseNo(courtCode, caseNo)
            .orElseThrow(() -> new EntityNotFoundException(String.format("Case %s not found for court %s", caseNo, courtCode)));
    }

    public CourtCaseEntity createOrUpdateCase(String courtCode, String caseNo, CourtCaseEntity courtCaseEntity)
        throws EntityNotFoundException, InputMismatchException {
        checkCourtByCode(courtCaseEntity.getCourtCode());
        if (!caseNo.equals(courtCaseEntity.getCaseNo()) || !courtCode.equals(courtCaseEntity.getCourtCode())) {
            throw new InputMismatchException(String.format("Case No %s and Court Code %s do not match with values from body %s and %s",
                caseNo, courtCode, courtCaseEntity.getCaseNo(), courtCaseEntity.getCourtCode()));
        }

        return processAndSave(courtCaseEntity, courtCaseRepository.findByCourtCodeAndCaseNo(courtCode, caseNo));
    }

    public List<CourtCaseEntity> filterCasesByCourtAndDate(String courtCode, LocalDate date) {
        CourtEntity court = courtRepository.findByCourtCode(courtCode)
            .orElseThrow(() -> new EntityNotFoundException("Court %s not found", courtCode));

        LocalDateTime start = LocalDateTime.of(date, LocalTime.MIDNIGHT);
        return courtCaseRepository.findByCourtCodeAndSessionStartTimeBetween(court.getCourtCode(), start, start.plusDays(1));
    }

    public void delete(String courtCode, String caseNo) {
        courtCaseRepository.deleteById(getCaseByCaseNumber(courtCode, caseNo).getId());
    }

    private CourtCaseEntity processAndSave(CourtCaseEntity courtCaseEntity, Optional<CourtCaseEntity> existingCourtCaseEntity) {

        if (existingCourtCaseEntity.isEmpty()) {
            applyOffenceSequencing(courtCaseEntity.getOffences());
            return createCase(courtCaseEntity);
        }

        return updateAndSave(existingCourtCaseEntity.get(), courtCaseEntity);
    }

    private CourtCaseEntity updateAndSave(CourtCaseEntity existingCase, CourtCaseEntity courtCaseEntity) {
        // We have checked and matched court ode and case no. They are immutable fields. No need to update.

        existingCase.setCaseId(courtCaseEntity.getCaseId());
        existingCase.setCourtRoom(courtCaseEntity.getCourtRoom());
        existingCase.setSessionStartTime(courtCaseEntity.getSessionStartTime());
        existingCase.setProbationStatus(courtCaseEntity.getProbationStatus());
        existingCase.setPreviouslyKnownTerminationDate(courtCaseEntity.getPreviouslyKnownTerminationDate());
        existingCase.setSuspendedSentenceOrder(courtCaseEntity.getSuspendedSentenceOrder());
        existingCase.setBreach(courtCaseEntity.getBreach());
        existingCase.setDefendantName(courtCaseEntity.getDefendantName());
        existingCase.setDefendantAddress(courtCaseEntity.getDefendantAddress());
        existingCase.setDefendantDob(courtCaseEntity.getDefendantDob());
        existingCase.setDefendantSex(courtCaseEntity.getDefendantSex());
        existingCase.setCrn(courtCaseEntity.getCrn());
        existingCase.setCro(courtCaseEntity.getCro());
        existingCase.setPnc(courtCaseEntity.getPnc());
        existingCase.setListNo(courtCaseEntity.getListNo());
        existingCase.setNationality1(courtCaseEntity.getNationality1());
        existingCase.setNationality2(courtCaseEntity.getNationality2());

        // Update offences
        updateOffences(existingCase, courtCaseEntity);

        log.info("Court case updated for case no {}", courtCaseEntity.getCaseNo());
        return courtCaseRepository.save(existingCase);
    }

    private void updateOffences(CourtCaseEntity existingCase, CourtCaseEntity incomingCase) {

        if (incomingCase.getOffences().isEmpty()) {
            existingCase.clearOffences();
            return;
        }

        Map<Integer, OffenceEntity> offencesToSave = applyOffenceSequencing(incomingCase.getOffences()).stream()
            .collect(toMap(OffenceEntity::getSequenceNumber, offence -> offence));
        Map<Integer, OffenceEntity> existingOffences = applyOffenceSequencing(existingCase.getOffences()).stream()
            .collect(toMap(OffenceEntity::getSequenceNumber, offence -> offence));

        // Update all existing offences where there is a match to sequence number
        offencesToSave.forEach((key, value) -> {
            log.debug("Processing offences : " + key + ", " + value.getOffenceTitle());
            OffenceEntity offenceEntity = existingOffences.getOrDefault(key,
                OffenceEntity.builder()
                    .sequenceNumber(value.getSequenceNumber()).build());
            offenceEntity.setOffenceSummary(value.getOffenceSummary());
            offenceEntity.setOffenceTitle(value.getOffenceTitle());
            offenceEntity.setAct(value.getAct());
            if (offenceEntity.getCourtCase() == null) {
                existingCase.addOffence(offenceEntity);
            }
        });

        if (offencesToSave.size() < existingOffences.size()) {
            List<OffenceEntity> deletions = existingOffences.values().stream()
                .filter(value -> !offencesToSave.containsKey(value.getSequenceNumber()))
                .collect(Collectors.toList());
            existingCase.removeOffences(deletions);
        }
    }

    List<OffenceEntity> applyOffenceSequencing(Collection<OffenceEntity> offences) {
        final List<OffenceEntity> sortedOffences = offences.stream()
            .sorted(Comparator.comparingInt(value -> value.getSequenceNumber() == null ? Integer.MAX_VALUE : value.getSequenceNumber()))
            .collect(Collectors.toList());

        IntStream.range(0, sortedOffences.size()).forEach(index -> sortedOffences.get(index).setSequenceNumber(index + 1));
        return sortedOffences;
    }

}
