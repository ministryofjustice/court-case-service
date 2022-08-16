package uk.gov.justice.probation.courtcaseservice.service;

import org.springframework.stereotype.Service;
import uk.gov.justice.probation.courtcaseservice.controller.exceptions.ConflictingInputException;
import uk.gov.justice.probation.courtcaseservice.jpa.entity.CaseCommentEntity;
import uk.gov.justice.probation.courtcaseservice.jpa.repository.CaseCommentsRepository;
import uk.gov.justice.probation.courtcaseservice.jpa.repository.CourtCaseRepository;
import uk.gov.justice.probation.courtcaseservice.service.exceptions.EntityNotFoundException;

import static org.apache.commons.lang3.StringUtils.equalsIgnoreCase;

@Service
public class CaseCommentsService {

    private final CourtCaseRepository courtCaseRepository;
    private final CaseCommentsRepository caseCommentsRepository;

    public CaseCommentsService(CourtCaseRepository courtCaseRepository, CaseCommentsRepository caseCommentsRepository) {
        this.courtCaseRepository = courtCaseRepository;
        this.caseCommentsRepository = caseCommentsRepository;
    }

    public CaseCommentEntity createCaseComment(CaseCommentEntity caseCommentEntity) {

        var caseId = caseCommentEntity.getCaseId();
        return courtCaseRepository.findFirstByCaseIdOrderByIdDesc(caseId)
            .map(courtCaseEntity -> caseCommentsRepository.save(caseCommentEntity))
            .orElseThrow(() -> new EntityNotFoundException("Court case %s not found", caseId));
    }

    public void deleteCaseComment(String caseId, Long commentId) {
        caseCommentsRepository.findById(commentId).ifPresentOrElse( caseCommentEntity -> {
            if(!equalsIgnoreCase(caseCommentEntity.getCaseId(), caseId)) {
                throw new ConflictingInputException(String.format("Comment %d not found for case %s", commentId, caseId));
            }
            caseCommentEntity.setDeleted(true);
            caseCommentsRepository.save(caseCommentEntity);
        }, () -> {
            throw new EntityNotFoundException("Comment %d not found", commentId);
        });
    }
}
