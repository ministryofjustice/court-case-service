package uk.gov.justice.probation.courtcaseservice.service;

import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import uk.gov.justice.probation.courtcaseservice.jpa.entity.CourtCaseEntity;
import uk.gov.justice.probation.courtcaseservice.jpa.repository.CourtCaseRepository;
import uk.gov.justice.probation.courtcaseservice.jpa.repository.CourtRepository;
import uk.gov.justice.probation.courtcaseservice.service.exceptions.EntityNotFoundException;

import java.util.InputMismatchException;

@Service
@Slf4j
public class ImmutableCourtCaseService extends MutableCourtCaseService {

    private CourtRepository courtRepository;
    private CourtCaseRepository courtCaseRepository;
    private TelemetryService telemetryService;

    public ImmutableCourtCaseService(CourtRepository courtRepository, CourtCaseRepository courtCaseRepository, TelemetryService telemetryService) {
        super(courtRepository, courtCaseRepository, telemetryService);
        this.courtRepository = courtRepository;
        this.courtCaseRepository = courtCaseRepository;
        this.telemetryService = telemetryService;
    }

    @Override
    public CourtCaseEntity createOrUpdateCase(String courtCode, String caseNo, CourtCaseEntity updatedCase) throws EntityNotFoundException, InputMismatchException {
        validateEntity(courtCode, caseNo, updatedCase);
        courtCaseRepository.findByCourtCodeAndCaseNo(courtCode, caseNo)
                .ifPresentOrElse(
                        (existingCase) -> updateCase(updatedCase, existingCase),
                        () -> createCase(updatedCase));
        return courtCaseRepository.save(updatedCase);
    }

    private void createCase(CourtCaseEntity updatedCase) {
        applyOffenceSequencing(updatedCase.getOffences());
        trackCreateEvents(updatedCase);
    }

    private void updateCase(CourtCaseEntity updatedCase, CourtCaseEntity existingCase) {
        updateOffenderMatches(existingCase, updatedCase);
        trackUpdateEvents(updatedCase, existingCase);
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
}
