package uk.gov.justice.probation.courtcaseservice.service;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import uk.gov.justice.probation.courtcaseservice.controller.exceptions.ConflictingInputException;
import uk.gov.justice.probation.courtcaseservice.jpa.entity.CaseCommentEntity;
import uk.gov.justice.probation.courtcaseservice.jpa.repository.CaseCommentsRepository;
import uk.gov.justice.probation.courtcaseservice.jpa.repository.CourtCaseRepository;
import uk.gov.justice.probation.courtcaseservice.restclient.exception.ForbiddenException;
import uk.gov.justice.probation.courtcaseservice.service.exceptions.EntityNotFoundException;

import static org.apache.commons.lang3.StringUtils.equalsIgnoreCase;
import static uk.gov.justice.probation.courtcaseservice.service.TelemetryEventType.CASE_COMMENT_ADDED;

@Service
public class CaseCommentsService {

    private static final Logger log = LoggerFactory.getLogger(CaseCommentsService.class);

    private final CourtCaseRepository courtCaseRepository;
    private final CaseCommentsRepository caseCommentsRepository;
    private final TelemetryService telemetryService;

    public CaseCommentsService(CourtCaseRepository courtCaseRepository, CaseCommentsRepository caseCommentsRepository,
                               TelemetryService telemetryService) {
        this.courtCaseRepository = courtCaseRepository;
        this.caseCommentsRepository = caseCommentsRepository;
        this.telemetryService = telemetryService;
    }

    public CaseCommentEntity createCaseComment(CaseCommentEntity caseCommentEntity) {

        var caseId = caseCommentEntity.getCaseId();
        return courtCaseRepository.findFirstByCaseIdOrderByIdDesc(caseId)
            .map(courtCaseEntity -> caseCommentsRepository.save(caseCommentEntity))
            .map(savedCaseComment -> {
                telemetryService.trackCourtCaseCommentEvent(CASE_COMMENT_ADDED, savedCaseComment);
                return savedCaseComment;
            })
            .orElseThrow(() -> new EntityNotFoundException("Court case %s not found", caseId));
    }

    public void deleteCaseComment(String caseId, Long commentId, String userUuid) {
        caseCommentsRepository.findById(commentId).ifPresentOrElse( caseCommentEntity -> {
            if(!equalsIgnoreCase(caseCommentEntity.getCaseId(), caseId)) {
                throw new ConflictingInputException(String.format("Comment %d not found for case %s", commentId, caseId));
            }
            if(!equalsIgnoreCase(caseCommentEntity.getCreatedByUuid(), userUuid)) {
                log.warn("User {} illegal attempt to delete comment {}", userUuid, commentId);
                throw new ForbiddenException(String.format("User %s does not have permissions to delete comment %s", userUuid, commentId));
            }
            caseCommentEntity.setDeleted(true);
            caseCommentsRepository.save(caseCommentEntity);
        }, () -> {
            throw new EntityNotFoundException("Comment %d not found", commentId);
        });
    }
}
