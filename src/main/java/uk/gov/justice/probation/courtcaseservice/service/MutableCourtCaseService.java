package uk.gov.justice.probation.courtcaseservice.service;

import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import uk.gov.justice.probation.courtcaseservice.controller.exceptions.ConflictingInputException;
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
import java.util.stream.Stream;

@Slf4j
@AllArgsConstructor
public class MutableCourtCaseService implements CourtCaseService {

    private final CourtRepository courtRepository;
    private final CourtCaseRepository courtCaseRepository;
    private final TelemetryService telemetryService;

    @Override
    public CourtCaseEntity getCaseByCaseNumber(String courtCode, String caseNo) throws EntityNotFoundException {
        checkCourtExists(courtCode);
        log.info("Court case requested for court {} for case {}", courtCode, caseNo);
        return courtCaseRepository.findTopByCourtCodeAndCaseNoOrderByCreatedDesc(courtCode, caseNo)
            .orElseThrow(() -> new EntityNotFoundException(String.format("Case %s not found for court %s", caseNo, courtCode)));
    }

    @Override
    public CourtCaseEntity createOrUpdateCase(String courtCode, String caseNo, CourtCaseEntity updatedCase)
        throws EntityNotFoundException, InputMismatchException {
        validateEntity(courtCode, caseNo, updatedCase);

        return courtCaseRepository.findTopByCourtCodeAndCaseNoOrderByCreatedDesc(courtCode, caseNo)
                .map(existingCase -> updateAndSaveCase(existingCase, updatedCase))
                .orElseGet(() -> createCase(updatedCase));
    }

    @Override
    public List<CourtCaseEntity> filterCasesByCourtAndDate(String courtCode, LocalDate date) {
        CourtEntity court = courtRepository.findByCourtCode(courtCode)
            .orElseThrow(() -> new EntityNotFoundException("Court %s not found", courtCode));

        LocalDateTime start = LocalDateTime.of(date, LocalTime.MIDNIGHT);
        return courtCaseRepository.findByCourtCodeAndSessionStartTimeBetween(court.getCourtCode(), start, start.plusDays(1));
    }
    void validateEntity(String courtCode, String caseNo, CourtCaseEntity updatedCase) {
        checkCourtExists(updatedCase.getCourtCode());
        checkEntityCaseNoAndCourtAgree(courtCode, caseNo, updatedCase);
    }

    private void checkEntityCaseNoAndCourtAgree(String courtCode, String caseNo, CourtCaseEntity updatedCase) {
        if (!caseNo.equals(updatedCase.getCaseNo()) || !courtCode.equals(updatedCase.getCourtCode())) {
            throw new ConflictingInputException(String.format("Case No %s and Court Code %s do not match with values from body %s and %s",
                    caseNo, courtCode, updatedCase.getCaseNo(), updatedCase.getCourtCode()));
        }
    }

    private void checkCourtExists(String courtCode) throws EntityNotFoundException {
        courtRepository.findByCourtCode(courtCode)
                .orElseThrow(() -> new EntityNotFoundException(String.format("Court %s not found", courtCode)));
    }

    private CourtCaseEntity createCase(CourtCaseEntity courtCaseEntity) {
        log.info("Court case being created for case number {}", courtCaseEntity.getCaseNo());
        telemetryService.trackCourtCaseEvent(TelemetryEventType.COURT_CASE_CREATED, courtCaseEntity);
        if(courtCaseEntity.getCrn() != null){
            telemetryService.trackCourtCaseEvent(TelemetryEventType.DEFENDANT_LINKED, courtCaseEntity);
        }
        return courtCaseRepository.save(courtCaseEntity);
    }

    private CourtCaseEntity updateAndSaveCase(CourtCaseEntity existingCase, CourtCaseEntity updatedCase) {
        var originalCrn = existingCase.getCrn();
        if(existingCase.getCrn() != null && updatedCase.getCrn() == null){
            telemetryService.trackCourtCaseEvent(TelemetryEventType.DEFENDANT_UNLINKED, existingCase);
        }

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

        updateOffenderMatches(existingCase, updatedCase);

        log.info("Court case updated for case no {}", updatedCase.getCaseNo());
        telemetryService.trackCourtCaseEvent(TelemetryEventType.COURT_CASE_UPDATED, updatedCase);
        if(originalCrn == null && updatedCase.getCrn() != null){
            telemetryService.trackCourtCaseEvent(TelemetryEventType.DEFENDANT_LINKED, updatedCase);
        }
        return courtCaseRepository.save(existingCase);
    }

    protected void updateOffenderMatches(CourtCaseEntity existingCase, CourtCaseEntity updatedCase) {
        if (existingCase.getGroupedOffenderMatches() == null) return;

        existingCase.getGroupedOffenderMatches()
                .stream().flatMap(group -> group.getOffenderMatches() != null ? group.getOffenderMatches().stream() : Stream.empty())
                .forEach(match -> {
                    boolean crnMatches = match.getCrn().equals(updatedCase.getCrn());
                    match.setConfirmed(crnMatches);
                    match.setRejected(!crnMatches);
                    if (crnMatches){
                        telemetryService.trackMatchEvent(TelemetryEventType.MATCH_CONFIRMED, match);
                    } else {
                        telemetryService.trackMatchEvent(TelemetryEventType.MATCH_REJECTED, match);
                    }

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
}
