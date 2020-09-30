package uk.gov.justice.probation.courtcaseservice.service;

import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import uk.gov.justice.probation.courtcaseservice.jpa.entity.CourtCaseEntity;
import uk.gov.justice.probation.courtcaseservice.jpa.repository.CourtCaseRepository;
import uk.gov.justice.probation.courtcaseservice.service.exceptions.EntityNotFoundException;

import java.time.LocalDate;
import java.util.InputMismatchException;
import java.util.List;
import java.util.Map;
import java.util.stream.Stream;

@AllArgsConstructor
@Slf4j
public class ImmutableCourtCaseService implements CourtCaseService {
    private final MutableCourtCaseService courtCaseService;
    private final CourtCaseRepository courtCaseRepository;
    private final TelemetryService telemetryService;

    @Override
    public CourtCaseEntity getCaseByCaseNumber(String courtCode, String caseNo) throws EntityNotFoundException {
        return courtCaseService.getCaseByCaseNumber(courtCode, caseNo);
    }

    @Override
    public CourtCaseEntity createOrUpdateCase(String courtCode, String caseNo, CourtCaseEntity updatedCase) throws EntityNotFoundException, InputMismatchException {
        courtCaseService.validateEntity(courtCode, caseNo, updatedCase);
        courtCaseRepository.findByCourtCodeAndCaseNo(courtCode, caseNo)
                .ifPresentOrElse(
                        (existingCase) -> {
                            trackUpdateEvents(updatedCase, existingCase);
                            updateOffenderMatches(existingCase, updatedCase);
                        },
                        () -> trackCreateEvents(updatedCase));
        return courtCaseRepository.save(updatedCase);
    }

    @Override
    public List<CourtCaseEntity> filterCasesByCourtAndDate(String courtCode, LocalDate date) {
        return courtCaseService.filterCasesByCourtAndDate(courtCode, date);
    }

    @Override
    public void delete(String courtCode, String caseNo) {
        courtCaseService.delete(courtCode, caseNo);
    }

    @Override
    public void deleteAbsentCases(String courtCode, Map<LocalDate, List<String>> existingCasesByDate) {
        courtCaseService.deleteAbsentCases(courtCode, existingCasesByDate);
    }

    private void trackCreateEvents(CourtCaseEntity updatedCase) {
        telemetryService.trackCourtCaseEvent(TelemetryEventType.COURT_CASE_CREATED, updatedCase);
        if (updatedCase.getCrn() != null)
            telemetryService.trackCourtCaseEvent(TelemetryEventType.DEFENDANT_LINKED, updatedCase);
    }

    private void trackUpdateEvents(CourtCaseEntity updatedCase, CourtCaseEntity existingCase) {
        telemetryService.trackCourtCaseEvent(TelemetryEventType.COURT_CASE_UPDATED, updatedCase);
        if (existingCase.getCrn() == null && updatedCase.getCrn() != null)
            telemetryService.trackCourtCaseEvent(TelemetryEventType.DEFENDANT_LINKED, updatedCase);
        else if (existingCase.getCrn() != null & updatedCase.getCrn() == null)
            telemetryService.trackCourtCaseEvent(TelemetryEventType.DEFENDANT_UNLINKED, existingCase);
    }

    private void updateOffenderMatches(CourtCaseEntity existingCase, CourtCaseEntity updatedCase) {
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
