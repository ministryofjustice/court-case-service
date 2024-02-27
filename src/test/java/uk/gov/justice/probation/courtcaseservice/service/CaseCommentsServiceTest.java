package uk.gov.justice.probation.courtcaseservice.service;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import uk.gov.justice.probation.courtcaseservice.jpa.entity.CaseCommentEntity;
import uk.gov.justice.probation.courtcaseservice.jpa.entity.CourtCaseEntity;
import uk.gov.justice.probation.courtcaseservice.jpa.entity.EntityHelper;
import uk.gov.justice.probation.courtcaseservice.jpa.repository.CaseCommentsRepository;
import uk.gov.justice.probation.courtcaseservice.jpa.repository.CourtCaseRepository;
import uk.gov.justice.probation.courtcaseservice.service.exceptions.EntityNotFoundException;

import java.util.ArrayList;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.verifyNoMoreInteractions;
import static uk.gov.justice.probation.courtcaseservice.service.TelemetryEventType.CASE_COMMENT_ADDED;
import static uk.gov.justice.probation.courtcaseservice.service.TelemetryEventType.CASE_COMMENT_DELETED;

@ExtendWith(MockitoExtension.class)
class CaseCommentsServiceTest {

    @Mock
    private CourtCaseRepository courtCaseRepository;
    @Mock
    private CaseCommentsRepository caseCommentsRepository;
    @Mock
    private TelemetryService telemetryService;

    @InjectMocks
    private CaseCommentsService caseCommentsService;

    private static String testCaseId = "test-case-id";
    private static String testDefendantId = "test-defendant-id";
    private static String createdByUuid = "created-by-uuid";
    CaseCommentEntity caseComment = CaseCommentEntity.builder().caseId(testCaseId).author("test author").defendantId(testDefendantId).createdByUuid(createdByUuid).build();
    CourtCaseEntity courtCaseEntity = CourtCaseEntity.builder().caseId(testCaseId).hearings(new ArrayList<>()).build();

    @Test
    void givenValidCaseComment_shouldCreateComment() {
        courtCaseEntity.addHearing(EntityHelper.aHearingEntityWithHearingId(testCaseId, EntityHelper.HEARING_ID, testDefendantId));
        given(courtCaseRepository.findFirstByCaseIdOrderByIdDesc(testCaseId))
            .willReturn(Optional.of(courtCaseEntity));
        given(caseCommentsRepository.save(caseComment)).willReturn(caseComment);
        caseCommentsService.createCaseComment(caseComment);
        verify(courtCaseRepository).findFirstByCaseIdOrderByIdDesc(testCaseId);
        verify(caseCommentsRepository).save(caseComment);
        verify(telemetryService).trackCourtCaseCommentEvent(CASE_COMMENT_ADDED, caseComment);
    }

    @Test
    void givenExistingCommentDraft_shouldUpdateExistingCommentAndMarkDraftIsFalse() {
        var existingComment = caseComment.withId(1L).withDefendantId(EntityHelper.DEFENDANT_ID).withCaseId(EntityHelper.CASE_ID).withDraft(true);

        var courtCase = EntityHelper.aHearingEntity().getCourtCase();
        given(courtCaseRepository.findFirstByCaseIdOrderByIdDesc(EntityHelper.CASE_ID)).willReturn(Optional.of(courtCase));

        given(caseCommentsRepository.findByCaseIdAndDefendantIdAndCreatedByUuidAndDraftIsTrue(EntityHelper.CASE_ID, EntityHelper.DEFENDANT_ID, createdByUuid))
            .willReturn(Optional.of(existingComment));

        var expectedComment = existingComment.withComment("updated and finalised comment to save").withDraft(false);
        given(caseCommentsRepository.save(expectedComment)).willReturn(caseComment);
        caseCommentsService.createCaseComment(expectedComment);
        verify(caseCommentsRepository).findByCaseIdAndDefendantIdAndCreatedByUuidAndDraftIsTrue(EntityHelper.CASE_ID, EntityHelper.DEFENDANT_ID, createdByUuid);
        verify(caseCommentsRepository).save(existingComment.withDraft(false).withComment("updated and finalised comment to save"));
        verify(telemetryService).trackCourtCaseCommentEvent(CASE_COMMENT_ADDED, caseComment);
    }

    @Test
    void givenNonExistingCaseId_shouldThrowEntityNotFound() {

        given(courtCaseRepository.findFirstByCaseIdOrderByIdDesc(testCaseId))
            .willReturn(Optional.empty());
        Exception e = assertThrows(EntityNotFoundException.class, () -> caseCommentsService.createCaseComment(caseComment));
        assertThat(e.getMessage()).isEqualTo("Court case test-case-id / defendantId test-defendant-id not found");
        verify(courtCaseRepository).findFirstByCaseIdOrderByIdDesc(testCaseId);
        verifyNoInteractions(caseCommentsRepository);
    }

    @Test
    void givenCaseIdAndDefendantIdAndCommentId_shouldMarkCommentAsDeleted() {
        var commentId = 1234L;
        given(caseCommentsRepository.findByIdAndCaseIdAndDefendantIdAndCreatedByUuid(commentId, testCaseId, testDefendantId, createdByUuid)).willReturn(Optional.of(caseComment.withId(commentId)));
        given(caseCommentsRepository.save(any(CaseCommentEntity.class))).willReturn(caseComment);

        caseCommentsService.deleteCaseComment(testCaseId, testDefendantId, commentId, createdByUuid);

        var expected = caseComment.withId(commentId);
        expected.setDeleted(true);
        verify(caseCommentsRepository).findByIdAndCaseIdAndDefendantIdAndCreatedByUuid(commentId, testCaseId, testDefendantId, createdByUuid);
        verify(caseCommentsRepository).save(expected);
        verify(telemetryService).trackCourtCaseCommentEvent(CASE_COMMENT_DELETED, expected);
    }

    @Test
    void givenCaseIdAndCommentId_AndCaseIdDoesNotExist_shouldThrowNotFoundException() {
        var commentId = 1234L;
        var invalidCaseId = "invalid-case-id";
        given(caseCommentsRepository.findByIdAndCaseIdAndDefendantIdAndCreatedByUuid(commentId, invalidCaseId, testDefendantId, createdByUuid)).willReturn(Optional.empty());

        assertThrows(EntityNotFoundException.class, () -> caseCommentsService.deleteCaseComment(invalidCaseId, testDefendantId, commentId, createdByUuid),
            "Comment 1234 not found for case invalid-case-id");
        verify(caseCommentsRepository).findById(commentId);
        verifyNoMoreInteractions(caseCommentsRepository);
    }

    @Test
    void givenCaseIdCommentIdAndUserUuid_AndUserUuidDoesNotMatchCommentUserUuid_shouldThrowForbiddenException() {
        var commentId = 1234L;
        String invalidCreatedByUuid = "invalid-user-uuid";
        given(caseCommentsRepository.findByIdAndCaseIdAndDefendantIdAndCreatedByUuid(commentId, testCaseId, testDefendantId, invalidCreatedByUuid)).willReturn(Optional.empty());
        assertThrows(EntityNotFoundException.class, () -> caseCommentsService.deleteCaseComment(testCaseId, testDefendantId, commentId, invalidCreatedByUuid),
            "User invalid-user-uuid does not have permissions to delete comment 1234");
        verify(caseCommentsRepository).findByIdAndCaseIdAndDefendantIdAndCreatedByUuid(commentId, testCaseId, testDefendantId, invalidCreatedByUuid);
        verifyNoMoreInteractions(caseCommentsRepository);
    }

    @Test
    void givenValidCaseCommentDraft_and_draft_do_not_exist_shouldCreateComment() {
        given(courtCaseRepository.findFirstByCaseIdOrderByIdDesc(testCaseId))
            .willReturn(Optional.of(courtCaseEntity));
        given(caseCommentsRepository.findByCaseIdAndDefendantIdAndCreatedByUuidAndDraftIsTrue(testCaseId, testDefendantId, createdByUuid))
            .willReturn(Optional.empty());
        given(caseCommentsRepository.save(caseComment.withDraft(true))).willReturn(caseComment);
        caseCommentsService.createUpdateCaseCommentDraft(caseComment);
        verify(courtCaseRepository).findFirstByCaseIdOrderByIdDesc(testCaseId);
        verify(caseCommentsRepository).findByCaseIdAndDefendantIdAndCreatedByUuidAndDraftIsTrue(testCaseId, testDefendantId, createdByUuid);
        verify(caseCommentsRepository).save(caseComment.withDraft(true));
    }

    @Test
    void givenValidCaseCommentDraft_and_draft_already_exist_shouldCreateComment() {
        given(courtCaseRepository.findFirstByCaseIdOrderByIdDesc(testCaseId))
            .willReturn(Optional.of(courtCaseEntity));
        var existingComment = CaseCommentEntity.builder().caseId(testCaseId).comment("comment one").id(1L).draft(true).build();
        given(caseCommentsRepository.findByCaseIdAndDefendantIdAndCreatedByUuidAndDraftIsTrue(testCaseId, testDefendantId, createdByUuid))
            .willReturn(Optional.of(existingComment));
        var expectedSavedComment = existingComment.withComment("updated comment");
        given(caseCommentsRepository.save(expectedSavedComment)).willReturn(expectedSavedComment);

        caseCommentsService.createUpdateCaseCommentDraft(caseComment.withComment("updated comment"));

        verify(courtCaseRepository).findFirstByCaseIdOrderByIdDesc(testCaseId);
        verify(caseCommentsRepository).findByCaseIdAndDefendantIdAndCreatedByUuidAndDraftIsTrue(testCaseId, testDefendantId, createdByUuid);
        verify(caseCommentsRepository).save(expectedSavedComment);
    }

    @Test
    void givenValidCaseIdWithDraftComment_whenDeleteDraftComment_shouldDelete() {
        given(courtCaseRepository.findFirstByCaseIdOrderByIdDesc(testCaseId))
            .willReturn(Optional.of(courtCaseEntity));
        var existingComment = CaseCommentEntity.builder().caseId(testCaseId).comment("comment one").id(123L).draft(true).build();
        given(caseCommentsRepository.findByCaseIdAndDefendantIdAndCreatedByUuidAndDraftIsTrue(testCaseId, testDefendantId, createdByUuid))
            .willReturn(Optional.of(existingComment));

        caseCommentsService.deleteCaseCommentDraft(testCaseId, testDefendantId, createdByUuid);

        verify(caseCommentsRepository).findByCaseIdAndDefendantIdAndCreatedByUuidAndDraftIsTrue(testCaseId, testDefendantId, createdByUuid);
        verify(caseCommentsRepository).delete(existingComment);
    }

    @Test
    void givenCaseIdDoNotExist_whenDeleteDraftComment_shouldThrowCaseNotFound() {
        given(caseCommentsRepository.findByCaseIdAndDefendantIdAndCreatedByUuidAndDraftIsTrue(testCaseId, testDefendantId, createdByUuid))
            .willReturn(Optional.empty());
        assertThrows(EntityNotFoundException.class, () -> caseCommentsService.deleteCaseCommentDraft(testCaseId, testDefendantId, createdByUuid),
            String.format("Cannot find case with case id %s", testCaseId));
        verifyNoMoreInteractions(caseCommentsRepository);
    }

    @Test
    void givenCaseIdExist_draftCommentDoNotExistForGivenUser_whenDeleteDraftComment_shouldThrowCaseCommentDraftNotFoundForUser() {
        given(caseCommentsRepository.findByCaseIdAndDefendantIdAndCreatedByUuidAndDraftIsTrue(testCaseId, testDefendantId, createdByUuid)).willReturn(Optional.empty());
        assertThrows(EntityNotFoundException.class, () -> caseCommentsService.deleteCaseCommentDraft(testCaseId, testDefendantId, createdByUuid),
            String.format("Cannot find draft case comment for case id %s and user id %s", testCaseId, createdByUuid));
        verify(caseCommentsRepository).findByCaseIdAndDefendantIdAndCreatedByUuidAndDraftIsTrue(testCaseId, testDefendantId, createdByUuid);
        verifyNoMoreInteractions(caseCommentsRepository);
    }

    @Test
    void givenExistingCaseIdAndDefendantIdAndCommentId_whenUpdateCaseComment_shouldUpdateTheComment() {
        var commentId = 1L;
        var existingComment = CaseCommentEntity.builder().caseId(testCaseId).defendantId(testDefendantId).createdByUuid(createdByUuid).comment("comment one").id(commentId).build();

        given(caseCommentsRepository.findByIdAndCaseIdAndDefendantIdAndCreatedByUuid(commentId, testCaseId, testDefendantId, createdByUuid))
            .willReturn(Optional.of(existingComment));

        var expectedSavedComment = existingComment.withComment("updated comment");
        given(caseCommentsRepository.save(expectedSavedComment)).willReturn(expectedSavedComment);

        caseCommentsService.updateCaseComment(CaseCommentEntity.builder().createdByUuid(createdByUuid).comment("updated comment").caseId(testCaseId).defendantId(testDefendantId).build(), commentId);

        verify(caseCommentsRepository).findByIdAndCaseIdAndDefendantIdAndCreatedByUuid(commentId, testCaseId, testDefendantId, createdByUuid);
        verify(caseCommentsRepository).save(expectedSavedComment);
    }

    @Test
    void givenCommentNotFound_whenUpdateCaseComment_shouldThrowEntityNotFound() {
        var commentId = 1L;
        var invalidUserUuid = "invalid used uuid";
        var comment = CaseCommentEntity.builder().createdByUuid(invalidUserUuid).comment("updated comment")
            .caseId(testCaseId).defendantId(testDefendantId).build();

        given(caseCommentsRepository.findByIdAndCaseIdAndDefendantIdAndCreatedByUuid(commentId, testCaseId, testDefendantId, invalidUserUuid))
            .willReturn(Optional.empty());
        Exception e = assertThrows(EntityNotFoundException.class, () -> caseCommentsService.updateCaseComment(comment, commentId));
        assertThat(e.getMessage()).isEqualTo("Comment 1 not found for the given user on case test-case-id");
        verifyNoMoreInteractions(caseCommentsRepository);
    }

}
