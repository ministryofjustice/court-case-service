package uk.gov.justice.probation.courtcaseservice.service;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import uk.gov.justice.probation.courtcaseservice.controller.exceptions.ConflictingInputException;
import uk.gov.justice.probation.courtcaseservice.jpa.entity.CaseCommentEntity;
import uk.gov.justice.probation.courtcaseservice.jpa.entity.CourtCaseEntity;
import uk.gov.justice.probation.courtcaseservice.jpa.repository.CaseCommentsRepository;
import uk.gov.justice.probation.courtcaseservice.jpa.repository.CourtCaseRepository;
import uk.gov.justice.probation.courtcaseservice.restclient.exception.ForbiddenException;
import uk.gov.justice.probation.courtcaseservice.service.exceptions.EntityNotFoundException;

import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;

@ExtendWith(MockitoExtension.class)
class CaseCommentsServiceTest {

    @Mock
    private CourtCaseRepository courtCaseRepository;
    @Mock
    private CaseCommentsRepository caseCommentsRepository;

    @InjectMocks
    private CaseCommentsService caseCommentsService;

    private static String testCaseId = "test-case-id";
    private static String createdByUuid = "created-by-uuid";
    CaseCommentEntity caseComment = CaseCommentEntity.builder().caseId(testCaseId).author("test author").createdByUuid("created-by-uuid").build();
    CourtCaseEntity courtCaseEntity = CourtCaseEntity.builder().caseId(testCaseId).build();

    @Test
    void givenValidCaseComment_shouldCreateComment() {
        given(courtCaseRepository.findFirstByCaseIdOrderByIdDesc(testCaseId))
            .willReturn(Optional.of(courtCaseEntity));
        given(caseCommentsRepository.save(caseComment)).willReturn(caseComment);
        caseCommentsService.createCaseComment(caseComment);
        verify(courtCaseRepository).findFirstByCaseIdOrderByIdDesc(testCaseId);
        verify(caseCommentsRepository).save(caseComment);
    }

    @Test
    void givenNonExistingCaseId_shouldThrowEntityNotFound() {

        given(courtCaseRepository.findFirstByCaseIdOrderByIdDesc(testCaseId))
            .willReturn(Optional.empty());
        Exception e = assertThrows(EntityNotFoundException.class, () -> caseCommentsService.createCaseComment(caseComment));
        assertThat(e.getMessage()).isEqualTo(String.format("Court case %s not found", testCaseId));
        verify(courtCaseRepository).findFirstByCaseIdOrderByIdDesc(testCaseId);
        verifyNoInteractions(caseCommentsRepository);
    }

    @Test
    void givenCaseIdAndCommentId_shouldMarkCommentAsDeleted() {
        var commentId = 1234L;
        given(caseCommentsRepository.findById(commentId)).willReturn(Optional.of(caseComment.withId(commentId)));
        given(caseCommentsRepository.save(any(CaseCommentEntity.class))).willReturn(caseComment);

        caseCommentsService.deleteCaseComment(testCaseId, commentId, createdByUuid);

        verify(caseCommentsRepository).findById(commentId);
        var expected = caseComment.withId(commentId);
        expected.setDeleted(true);
        verify(caseCommentsRepository).save(expected);
    }

    @Test
    void givenCaseIdAndCommentId_AndCaseIdDoesNotMatchCommentCaseId_shouldThrowComflictingInput() {
        var commentId = 1234L;
        given(caseCommentsRepository.findById(commentId)).willReturn(Optional.of(caseComment.withId(commentId)));

        var invalidCaseId = "invalid-case-id";
        assertThrows(ConflictingInputException.class, () -> caseCommentsService.deleteCaseComment(invalidCaseId, commentId, createdByUuid),
            "Comment 1234 not found for case invalid-case-id");
        verify(caseCommentsRepository).findById(commentId);
        verify(caseCommentsRepository, times(0)).save(any(CaseCommentEntity.class));
    }

    @Test
    void givenCaseIdCommentIdAndUserUuid_AndUserUuidDoesNotMatchCommentUserUuid_shouldThrowForbiddenException() {
        var commentId = 1234L;
        String invalidCreatedByUuid = "invalid-user-uuid";
        given(caseCommentsRepository.findById(commentId)).willReturn(Optional.of(caseComment.withId(commentId)));
        assertThrows(ForbiddenException.class, () -> caseCommentsService.deleteCaseComment(testCaseId, commentId, invalidCreatedByUuid),
            "User invalid-user-uuid does not have permissions to delete comment 1234");
        verify(caseCommentsRepository).findById(commentId);
        verify(caseCommentsRepository, times(0)).save(any(CaseCommentEntity.class));
    }

    @Test
    void givenNonExistingCommentId_shouldThrowEntityNotFound() {
        var commentId = 1234L;
        given(caseCommentsRepository.findById(commentId)).willReturn(Optional.empty());
        assertThrows(EntityNotFoundException.class, () -> caseCommentsService.deleteCaseComment(testCaseId, commentId, createdByUuid),
            "Comment 1234 not found");
        verify(caseCommentsRepository).findById(commentId);
        verify(caseCommentsRepository, times(0)).save(any(CaseCommentEntity.class));
    }
}