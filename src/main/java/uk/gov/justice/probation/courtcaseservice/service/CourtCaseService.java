package uk.gov.justice.probation.courtcaseservice.service;

import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import uk.gov.justice.probation.courtcaseservice.jpa.entity.CourtCaseEntity;
import uk.gov.justice.probation.courtcaseservice.jpa.entity.CourtEntity;
import uk.gov.justice.probation.courtcaseservice.jpa.entity.OffenceEntity;
import uk.gov.justice.probation.courtcaseservice.jpa.repository.CourtCaseRepository;
import uk.gov.justice.probation.courtcaseservice.jpa.repository.CourtRepository;
import uk.gov.justice.probation.courtcaseservice.service.exceptions.EntityNotFoundException;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.Collection;
import java.util.Comparator;
import java.util.HashSet;
import java.util.InputMismatchException;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;
import java.util.stream.IntStream;
import java.util.stream.Stream;

import static java.util.stream.Collectors.toMap;

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

    public CourtCaseEntity getCaseByCaseNumber(String courtCode, String caseNo) throws EntityNotFoundException {
        checkCourtExists(courtCode);
        log.info("Court case requested for court {} for case {}", courtCode, caseNo);
        return courtCaseRepository.findByCourtCodeAndCaseNo(courtCode, caseNo)
            .orElseThrow(() -> new EntityNotFoundException(String.format("Case %s not found for court %s", caseNo, courtCode)));
    }

    public CourtCaseEntity createOrUpdateCase(String courtCode, String caseNo, CourtCaseEntity updatedCase)
        throws EntityNotFoundException, InputMismatchException {
        validateEntity(courtCode, caseNo, updatedCase);

        return courtCaseRepository.findByCourtCodeAndCaseNo(courtCode, caseNo)
                .map(existingCase -> updateAndSaveCase(existingCase, updatedCase))
                .orElseGet(() -> createCase(updatedCase));
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

    @Transactional
    public void deleteAbsentCases(String courtCode, Map<LocalDate, List<String>> existingCasesByDate) {
        courtRepository.findByCourtCode(courtCode).orElseThrow(() -> new EntityNotFoundException("Court %s not found", courtCode));

        final Set<CourtCaseEntity> casesToDelete = new HashSet<>();
        existingCasesByDate.forEach((dateOfHearing, existingCaseNos) -> {
            log.debug("Delete cases for court {} on date {}, not in list {}", courtCode, dateOfHearing, existingCaseNos);
            LocalDateTime start = LocalDateTime.of(dateOfHearing, LocalTime.MIDNIGHT);
            LocalDateTime end = start.plusDays(1);
            if (existingCaseNos.isEmpty()) {
                casesToDelete.addAll(courtCaseRepository.findByCourtCodeAndSessionStartTimeBetween(courtCode, start, end));
            }
            else {
                casesToDelete.addAll(courtCaseRepository.findCourtCasesNotIn(courtCode, start, end, existingCaseNos));
            }
        });

        if (log.isDebugEnabled()) {
            casesToDelete.forEach(aCase -> log.debug("Soft delete case no {} for court {}, session time {} ",
                aCase.getCaseNo(), aCase.getCourtCode(), aCase.getSessionStartTime()));
        }

        courtCaseRepository.deleteAll(casesToDelete);
    }

    private void validateEntity(String courtCode, String caseNo, CourtCaseEntity updatedCase) {
        checkCourtExists(updatedCase.getCourtCode());
        checkEntityCaseNoAndCourtAgree(courtCode, caseNo, updatedCase);
    }

    private void checkEntityCaseNoAndCourtAgree(String courtCode, String caseNo, CourtCaseEntity updatedCase) {
        if (!caseNo.equals(updatedCase.getCaseNo()) || !courtCode.equals(updatedCase.getCourtCode())) {
            throw new InputMismatchException(String.format("Case No %s and Court Code %s do not match with values from body %s and %s",
                    caseNo, courtCode, updatedCase.getCaseNo(), updatedCase.getCourtCode()));
        }
    }

    private void checkCourtExists(String courtCode) throws EntityNotFoundException {
        courtRepository.findByCourtCode(courtCode)
                .orElseThrow(() -> new EntityNotFoundException(String.format("Court %s not found", courtCode)));
    }

    private CourtCaseEntity createCase(CourtCaseEntity courtCaseEntity) {
        applyOffenceSequencing(courtCaseEntity.getOffences());
        log.info("Court case being created for case number {}", courtCaseEntity.getCaseNo());
        return courtCaseRepository.save(courtCaseEntity);
    }

    private CourtCaseEntity updateAndSaveCase(CourtCaseEntity existingCase, CourtCaseEntity updatedCase) {
        // We have checked and matched court code and case no. They are immutable fields. No need to update.

        existingCase.setCaseId(updatedCase.getCaseId());
        existingCase.setCourtRoom(updatedCase.getCourtRoom());
        existingCase.setSessionStartTime(updatedCase.getSessionStartTime());
        existingCase.setProbationStatus(updatedCase.getProbationStatus());
        existingCase.setPreviouslyKnownTerminationDate(updatedCase.getPreviouslyKnownTerminationDate());
        existingCase.setSuspendedSentenceOrder(updatedCase.getSuspendedSentenceOrder());
        existingCase.setBreach(updatedCase.getBreach());
        existingCase.setDefendantName(updatedCase.getDefendantName());
        existingCase.setDefendantAddress(updatedCase.getDefendantAddress());
        existingCase.setDefendantDob(updatedCase.getDefendantDob());
        existingCase.setDefendantSex(updatedCase.getDefendantSex());
        existingCase.setCrn(updatedCase.getCrn());
        existingCase.setCro(updatedCase.getCro());
        existingCase.setPnc(updatedCase.getPnc());
        existingCase.setListNo(updatedCase.getListNo());
        existingCase.setNationality1(updatedCase.getNationality1());
        existingCase.setNationality2(updatedCase.getNationality2());

        updateOffences(existingCase, updatedCase);
        updateOffenderMatches(existingCase, updatedCase);

        log.info("Court case updated for case no {}", updatedCase.getCaseNo());
        return courtCaseRepository.save(existingCase);
    }

    private void updateOffenderMatches(CourtCaseEntity existingCase, CourtCaseEntity updatedCase) {
        if (existingCase.getGroupedOffenderMatches() == null) return;

        existingCase.getGroupedOffenderMatches()
                .stream().flatMap(group -> group.getOffenderMatches() != null ? group.getOffenderMatches().stream() : Stream.empty())
                .forEach(match -> {
                    boolean crnMatches = updatedCase.getCrn().equals(match.getCrn());
                    match.setConfirmed(crnMatches);
                    match.setRejected(!crnMatches);

                    if (crnMatches && updatedCase.getPnc() != null && !updatedCase.getPnc().equals(match.getPnc())) {
                        log.warn(String.format("Unexpected PNC mismatch when updating offender match - matchId: '%s', crn: '%s', matchPnc: %s, updatePnc: %s",
                                match.getId(), existingCase.getCrn(), match.getPnc(), existingCase.getPnc()));
                    }
                    if (crnMatches && updatedCase.getCro() != null && !updatedCase.getCro().equals(match.getCro())) {
                        log.warn(String.format("Unexpected CRO mismatch when updating offender match - matchId: '%s', crn: '%s', matchCro: %s, updateCro: %s",
                                match.getId(), existingCase.getCrn(), match.getCro(), existingCase.getCro()));
                    }
                });
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
