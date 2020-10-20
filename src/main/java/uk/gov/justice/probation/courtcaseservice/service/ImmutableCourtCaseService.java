package uk.gov.justice.probation.courtcaseservice.service;

import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import uk.gov.justice.probation.courtcaseservice.controller.exceptions.ConflictingInputException;
import uk.gov.justice.probation.courtcaseservice.jpa.entity.CourtCaseEntity;
import uk.gov.justice.probation.courtcaseservice.jpa.entity.CourtEntity;
import uk.gov.justice.probation.courtcaseservice.jpa.entity.GroupedOffenderMatchesEntity;
import uk.gov.justice.probation.courtcaseservice.jpa.entity.OffenderMatchEntity;
import uk.gov.justice.probation.courtcaseservice.jpa.repository.CourtCaseRepository;
import uk.gov.justice.probation.courtcaseservice.jpa.repository.CourtRepository;
import uk.gov.justice.probation.courtcaseservice.jpa.repository.GroupedOffenderMatchRepository;
import uk.gov.justice.probation.courtcaseservice.service.exceptions.EntityNotFoundException;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.InputMismatchException;
import java.util.List;

@Service
@Slf4j
@AllArgsConstructor
public class ImmutableCourtCaseService implements CourtCaseService {

    private final CourtRepository courtRepository;
    private final CourtCaseRepository courtCaseRepository;
    private final TelemetryService telemetryService;
    private final GroupedOffenderMatchRepository matchRepository;

    @Override
    public CourtCaseEntity createCase(String courtCode, String caseNo, CourtCaseEntity updatedCase) throws EntityNotFoundException, InputMismatchException {
        validateEntity(courtCode, caseNo, updatedCase);
        courtCaseRepository.findByCourtCodeAndCaseNo(courtCode, caseNo)
                .ifPresentOrElse(
                        (existingCase) -> {
                            updateOffenderMatches(existingCase, updatedCase);
                            trackUpdateEvents(updatedCase, existingCase);
                        },
                        () -> trackCreateEvents(updatedCase));

        return courtCaseRepository.save(updatedCase);
    }

    private void trackCreateEvents(CourtCaseEntity createdCase) {
        telemetryService.trackCourtCaseEvent(TelemetryEventType.COURT_CASE_CREATED, createdCase);
        if (createdCase.getCrn() != null)
            telemetryService.trackCourtCaseEvent(TelemetryEventType.DEFENDANT_LINKED, createdCase);
    }

    private void trackUpdateEvents(CourtCaseEntity updatedCase, CourtCaseEntity existingCase) {
        telemetryService.trackCourtCaseEvent(TelemetryEventType.COURT_CASE_UPDATED, updatedCase);
        if (existingCase.getCrn() == null && updatedCase.getCrn() != null)
            telemetryService.trackCourtCaseEvent(TelemetryEventType.DEFENDANT_LINKED, updatedCase);
        else if (existingCase.getCrn() != null & updatedCase.getCrn() == null)
            telemetryService.trackCourtCaseEvent(TelemetryEventType.DEFENDANT_UNLINKED, existingCase);
    }

    @Override
    public CourtCaseEntity getCaseByCaseNumber(String courtCode, String caseNo) throws EntityNotFoundException {
        checkCourtExists(courtCode);
        log.info("Court case requested for court {} for case {}", courtCode, caseNo);
        return courtCaseRepository.findByCourtCodeAndCaseNo(courtCode, caseNo)
            .orElseThrow(() -> new EntityNotFoundException(String.format("Case %s not found for court %s", caseNo, courtCode)));
    }

    @Override
    public List<CourtCaseEntity> filterCasesByCourtAndDate(String courtCode, LocalDate date, LocalDateTime createdAfter) {
        CourtEntity court = courtRepository.findByCourtCode(courtCode)
            .orElseThrow(() -> new EntityNotFoundException("Court %s not found", courtCode));

        LocalDateTime start = LocalDateTime.of(date, LocalTime.MIDNIGHT);
        return courtCaseRepository.findByCourtCodeAndSessionStartTime(court.getCourtCode(), start, start.plusDays(1), createdAfter);
    }

    private void validateEntity(String courtCode, String caseNo, CourtCaseEntity updatedCase) {
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

    private void updateOffenderMatches(CourtCaseEntity existingCase, CourtCaseEntity updatedCase) {
        final var groupedMatches = matchRepository.findByCourtCodeAndCaseNo(updatedCase.getCourtCode(), updatedCase.getCaseNo());
        groupedMatches
                .map(GroupedOffenderMatchesEntity::getOffenderMatches)
                .ifPresent(matches -> matches
                        .forEach(match -> confirmAndRejectMatches(existingCase, updatedCase, match)));
        groupedMatches.ifPresent(matchRepository::save);
    }

    private void confirmAndRejectMatches(CourtCaseEntity existingCase, CourtCaseEntity updatedCase, OffenderMatchEntity match) {
        boolean crnMatches = match.getCrn().equals(updatedCase.getCrn());
        match.setConfirmed(crnMatches);
        match.setRejected(!crnMatches);
        if (crnMatches){
            telemetryService.trackMatchEvent(TelemetryEventType.MATCH_CONFIRMED, match, updatedCase);
        } else {
            telemetryService.trackMatchEvent(TelemetryEventType.MATCH_REJECTED, match, updatedCase);
        }

        if (crnMatches && updatedCase.getPnc() != null && !updatedCase.getPnc().equals(match.getPnc())) {
            log.warn(String.format("Unexpected PNC mismatch when updating offender match - matchId: '%s', crn: '%s', matchPnc: %s, updatePnc: %s",
                    match.getId(), existingCase.getCrn(), match.getPnc(), existingCase.getPnc()));
        }
        if (crnMatches && updatedCase.getCro() != null && !updatedCase.getCro().equals(match.getCro())) {
            log.warn(String.format("Unexpected CRO mismatch when updating offender match - matchId: '%s', crn: '%s', matchCro: %s, updateCro: %s",
                    match.getId(), existingCase.getCrn(), match.getCro(), existingCase.getCro()));
        }
    }
}
