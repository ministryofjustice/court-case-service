package uk.gov.justice.probation.courtcaseservice.service;

import org.springframework.stereotype.Service;
import uk.gov.justice.probation.courtcaseservice.jpa.entity.CaseCommentEntity;
import uk.gov.justice.probation.courtcaseservice.jpa.repository.CaseCommentsRepository;
import uk.gov.justice.probation.courtcaseservice.jpa.repository.CourtCaseRepository;
import uk.gov.justice.probation.courtcaseservice.service.exceptions.EntityNotFoundException;

import java.util.UUID;

@Service
public class CaseCommentsService {

    private final CourtCaseRepository courtCaseRepository;
    private final CaseCommentsRepository caseCommentsRepository;

    public CaseCommentsService(CourtCaseRepository courtCaseRepository, CaseCommentsRepository caseCommentsRepository) {
        this.courtCaseRepository = courtCaseRepository;
        this.caseCommentsRepository = caseCommentsRepository;
    }

    public CaseCommentEntity createCaseComment(String caseId, CaseCommentEntity caseCommentEntity) {

        CaseCommentEntity entity = caseCommentEntity.withCommentId(UUID.randomUUID().toString());
        return courtCaseRepository.findFirstByCaseIdOrderByIdDesc(caseId).map(
            courtCaseEntity -> caseCommentsRepository.save(entity))
            .orElseThrow(() -> new EntityNotFoundException("Court case %s not found", caseId));
    }
}
