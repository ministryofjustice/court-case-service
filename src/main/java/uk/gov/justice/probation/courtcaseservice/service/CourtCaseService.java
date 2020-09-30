package uk.gov.justice.probation.courtcaseservice.service;

import org.springframework.transaction.annotation.Transactional;
import uk.gov.justice.probation.courtcaseservice.jpa.entity.CourtCaseEntity;
import uk.gov.justice.probation.courtcaseservice.service.exceptions.EntityNotFoundException;

import java.time.LocalDate;
import java.util.InputMismatchException;
import java.util.List;
import java.util.Map;

public interface CourtCaseService {
    CourtCaseEntity getCaseByCaseNumber(String courtCode, String caseNo) throws EntityNotFoundException;

    CourtCaseEntity createOrUpdateCase(String courtCode, String caseNo, CourtCaseEntity updatedCase)
        throws EntityNotFoundException, InputMismatchException;

    List<CourtCaseEntity> filterCasesByCourtAndDate(String courtCode, LocalDate date);

    void delete(String courtCode, String caseNo);

    @Transactional
    void deleteAbsentCases(String courtCode, Map<LocalDate, List<String>> existingCasesByDate);
}
