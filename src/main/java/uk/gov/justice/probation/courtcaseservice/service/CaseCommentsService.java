package uk.gov.justice.probation.courtcaseservice.service;

import org.hibernate.Hibernate;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import uk.gov.justice.probation.courtcaseservice.jpa.entity.CaseCommentEntity;
import uk.gov.justice.probation.courtcaseservice.jpa.entity.CourtCaseEntity;
import uk.gov.justice.probation.courtcaseservice.jpa.repository.CaseCommentsRepository;
import uk.gov.justice.probation.courtcaseservice.jpa.repository.CourtCaseRepository;
import uk.gov.justice.probation.courtcaseservice.service.exceptions.EntityNotFoundException;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

import static uk.gov.justice.probation.courtcaseservice.service.TelemetryEventType.CASE_COMMENT_ADDED;

@Service
public class CaseCommentsService {

    private static final Logger log = LoggerFactory.getLogger(CaseCommentsService.class);

    private static final String COMMENTS_ERROR_MESSAGE_FORMAT_STRING = "Comment %d not found for caseId %s, defendantId %s and user %s or user does not have permissions to modify";

    private final CourtCaseRepository courtCaseRepository;
    private final CaseCommentsRepository caseCommentsRepository;
    private final TelemetryService telemetryService;

    public CaseCommentsService(CourtCaseRepository courtCaseRepository, CaseCommentsRepository caseCommentsRepository, TelemetryService telemetryService) {
        this.courtCaseRepository = courtCaseRepository;
        this.caseCommentsRepository = caseCommentsRepository;
        this.telemetryService = telemetryService;
    }

    @Transactional
    public CaseCommentEntity createCaseComment(CaseCommentEntity caseComment) {
        String caseId = caseComment.getCaseId();
        var defendantId = caseComment.getDefendantId();
        var courtCase =  courtCaseRepository.findFirstByCaseIdAndDeletedFalseOrderByIdDesc(caseId);
        Hibernate.initialize(courtCase.map(CourtCaseEntity::getCaseDefendants));
        return courtCase.filter(courtCaseEntity -> courtCaseEntity.hasDefendant(defendantId))
            .map(courtCaseEntity -> {
                var commentToSave = getDraftComment(caseId, defendantId, caseComment.getCreatedByUuid())
                    .map(caseCommentEntity -> {
                        caseCommentEntity.update(caseComment.withDraft(false));
                        return caseCommentEntity;
                    })
                    .orElse(caseComment.withDraft(false));
                return caseCommentsRepository.save(commentToSave);
            })
            .map(savedCaseComment -> {
                telemetryService.trackCourtCaseCommentEvent(CASE_COMMENT_ADDED, savedCaseComment);
                return savedCaseComment;
            })
            .orElseThrow(() -> new EntityNotFoundException("Court case %s / defendantId %s not found", caseId, defendantId));
    }

    public Optional<CaseCommentEntity> getDraftComment(String caseId, String defendantId, String userUuid){
        List<CaseCommentEntity> draftComments = caseCommentsRepository.findAllByCaseIdAndDefendantIdAndCreatedByUuidAndDraftIsTrueOrderByCreatedDesc(caseId, defendantId, userUuid);
        if (draftComments.size() > 1) {
            // Delete older draft comments
            draftComments.stream().skip(1).forEach(draftComment -> {
                draftComment.setDeleted(true);
                caseCommentsRepository.delete(draftComment);
            });
        }
        return draftComments.stream().findFirst();
    }

    public CaseCommentEntity createUpdateCaseCommentDraft(CaseCommentEntity caseComment) {

        var caseId = caseComment.getCaseId();
        var defendantId = caseComment.getDefendantId();
        return courtCaseRepository.findFirstByCaseIdAndDeletedFalseOrderByIdDesc(caseId)
            .map(courtCaseEntity -> {
                var commentToSave = caseCommentsRepository
                    .findByCaseIdAndDefendantIdAndCreatedByUuidAndDraftIsTrue(caseId, defendantId, caseComment.getCreatedByUuid())
                    .map(caseCommentEntity -> {
                        caseCommentEntity.update(caseComment.withDraft(true));
                        return caseCommentEntity;
                    })
                    .orElse(caseComment.withDraft(true));
                return caseCommentsRepository.save(commentToSave);
            })
            .orElseThrow(() -> new EntityNotFoundException("Cannot find draft with case id %s / defendantId %s / user", caseId));
    }

    public void deleteCaseCommentDraft(String caseId, String defendantId, String userUuid) {
        caseCommentsRepository.findByCaseIdAndDefendantIdAndCreatedByUuidAndDraftIsTrue(caseId, defendantId, userUuid)
                .ifPresentOrElse(caseCommentEntity -> {
                    caseCommentEntity.setDeleted(true);
                    caseCommentsRepository.delete(caseCommentEntity);
                }, () -> {
                throw new EntityNotFoundException("Cannot find draft case comment for case id %s and user id %s", caseId, userUuid);
            });
    }

    public CaseCommentEntity updateCaseComment(CaseCommentEntity caseCommentUpdate, Long commentId) {

       return caseCommentsRepository.findByIdAndCaseIdAndDefendantIdAndCreatedByUuid(commentId, caseCommentUpdate.getCaseId(), caseCommentUpdate.getDefendantId(), caseCommentUpdate.getCreatedByUuid())
           .map(caseCommentEntity -> {
               caseCommentEntity.update(caseCommentUpdate);
               return caseCommentsRepository.save(caseCommentEntity);
           })
           .orElseThrow(() -> new EntityNotFoundException(COMMENTS_ERROR_MESSAGE_FORMAT_STRING, commentId, caseCommentUpdate.getCaseId(), caseCommentUpdate.getDefendantId(), caseCommentUpdate.getDefendantId()));
    }

    public void deleteCaseComment(String caseId, String defendantId, Long commentId, String userUuid) {
        caseCommentsRepository.findByIdAndCaseIdAndDefendantIdAndCreatedByUuid(commentId, caseId, defendantId, userUuid)
            .ifPresentOrElse( caseCommentEntity -> {
            caseCommentEntity.setDeleted(true);
            caseCommentsRepository.save(caseCommentEntity);
            telemetryService.trackCourtCaseCommentEvent(TelemetryEventType.CASE_COMMENT_DELETED, caseCommentEntity);
        }, () -> {
                throw new EntityNotFoundException(COMMENTS_ERROR_MESSAGE_FORMAT_STRING, commentId, caseId, defendantId, userUuid);
        });
    }

    public List<CaseCommentEntity> getCaseCommentsForDefendant(String defendantId) {
        return caseCommentsRepository.findByDefendantId(defendantId);
    }

    public List<CaseCommentEntity> getCaseCommentsForDefendantBetween(String defendantId, LocalDate fromDate, LocalDate toDate) {
        return caseCommentsRepository.findByDefendantIdAndDeletedFalseAndDraftFalseAndLegacyFalseAndCreatedBetween(defendantId, fromDate.atStartOfDay(), toDate.atStartOfDay());
    }

    public List<CaseCommentEntity> getCaseCommentsForDefendantFrom(String defendantId, LocalDate fromDate) {
        return caseCommentsRepository.findByDefendantIdAndDeletedFalseAndDraftFalseAndLegacyFalseAndCreatedAfter(defendantId, fromDate.atStartOfDay());
    }

    public List<CaseCommentEntity> getCaseCommentsForDefendantTo(String defendantId, LocalDate toDate) {
        return caseCommentsRepository.findByDefendantIdAndDeletedFalseAndDraftFalseAndLegacyFalseAndCreatedBefore(defendantId, toDate.atStartOfDay());
    }
}
