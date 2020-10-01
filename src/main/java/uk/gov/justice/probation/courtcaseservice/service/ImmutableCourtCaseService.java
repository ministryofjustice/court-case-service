package uk.gov.justice.probation.courtcaseservice.service;

import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import uk.gov.justice.probation.courtcaseservice.jpa.entity.CourtCaseEntity;
import uk.gov.justice.probation.courtcaseservice.jpa.entity.ImmutableOffenceEntity;
import uk.gov.justice.probation.courtcaseservice.jpa.repository.CourtCaseRepository;
import uk.gov.justice.probation.courtcaseservice.jpa.repository.CourtRepository;
import uk.gov.justice.probation.courtcaseservice.service.exceptions.EntityNotFoundException;

import java.util.Collection;
import java.util.Collections;
import java.util.Comparator;
import java.util.InputMismatchException;
import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

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
        courtCaseRepository.findTopByCourtCodeAndCaseNoOrderByVersion(courtCode, caseNo)
                .ifPresentOrElse(
                        (existingCase) -> updateCase(updatedCase, existingCase),
                        () -> createCase(updatedCase));
        List<ImmutableOffenceEntity> updatedOffences = applyImmutableOffenceSequencing(updatedCase.getOffences());

        final var caseWithSequencedOffences = updatedCase.withOffences(updatedOffences);
        return courtCaseRepository.save(caseWithSequencedOffences);
    }

    private List<ImmutableOffenceEntity> applyImmutableOffenceSequencing(Collection<ImmutableOffenceEntity> offences) {
        if(offences == null) {
            return Collections.emptyList();
        }
        final var sortedOffences = offences.stream()
                .sorted(Comparator.comparingInt(value -> value.getSequenceNumber() == null ? Integer.MAX_VALUE : value.getSequenceNumber()))
                .map(offenceEntity -> ImmutableOffenceEntity.builder()
                        .act(offenceEntity.getAct())
                        .courtCase(offenceEntity.getCourtCase())
                        .offenceTitle(offenceEntity.getOffenceTitle())
                        .offenceSummary(offenceEntity.getOffenceSummary())
                        .build())
                .collect(Collectors.toList());

        return IntStream.range(0, sortedOffences.size())
                .mapToObj(index -> sortedOffences.get(index).withSequenceNumber(index + 1))
                .collect(Collectors.toList());
    }

    private void createCase(CourtCaseEntity updatedCase) {
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
