package uk.gov.justice.probation.courtcaseservice.service

import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.extension.ExtendWith
import org.mockito.BDDMockito.given
import org.mockito.Mock
import org.mockito.junit.jupiter.MockitoExtension
import org.mockito.kotlin.verifyNoInteractions
import uk.gov.justice.probation.courtcaseservice.jpa.entity.EntityHelper.*
import uk.gov.justice.probation.courtcaseservice.jpa.repository.DefendantRepositoryFacade
import java.util.*

@ExtendWith(MockitoExtension::class)
class DefendantCaseCommentsServiceTest {

    @Mock
    lateinit var caseCommentsService: CaseCommentsService

    @Mock
    lateinit var defendantRepositoryFacade: DefendantRepositoryFacade

    @Mock
    lateinit var immutableCourtCaseService: ImmutableCourtCaseService

    private lateinit var defendantCaseCommentsService: DefendantCaseCommentsService

    @BeforeEach
    fun setup() {
        defendantCaseCommentsService = DefendantCaseCommentsService(caseCommentsService, defendantRepositoryFacade, immutableCourtCaseService)
    }

    @Test
    fun `given defendants found with case comments should return sars comments response`() {
        given(defendantRepositoryFacade.findDefendantsByCrn("CRN123")).willReturn(listOf(aDefendantEntity("1234", "CRN123")))
        given(caseCommentsService.getCaseCommentsForDefendant("1234")).willReturn(listOf(aCaseCommentEntity()))
        given(immutableCourtCaseService.findByCaseId("5678")).willReturn(Optional.empty())

        val caseCommentsForDefendant = defendantCaseCommentsService.getCaseCommentsForDefendant("CRN123")
        assertThat(caseCommentsForDefendant[0].comment).isEqualTo("Some comment")
        assertThat(caseCommentsForDefendant[0].author).isEqualTo("Some author")
        assertThat(caseCommentsForDefendant[0].created).isEqualTo("2024-05-22T12:00")
        assertThat(caseCommentsForDefendant[0].createdBy).isEqualTo("Test User")
        assertThat(caseCommentsForDefendant[0].lastUpdated).isEqualTo("2024-05-22T12:30")
        assertThat(caseCommentsForDefendant[0].lastUpdatedBy).isEqualTo("Test User")
        assertThat(caseCommentsForDefendant[0].caseNumber).isEqualTo("")
    }

    @Test
    fun `given defendants found with case comments and LIBRA court case entry should return sars comments response`() {
        given(defendantRepositoryFacade.findDefendantsByCrn("CRN123")).willReturn(listOf(aDefendantEntity("1234", "CRN123")))
        given(caseCommentsService.getCaseCommentsForDefendant("1234")).willReturn(listOf(aCaseCommentEntity()))
        given(immutableCourtCaseService.findByCaseId("5678")).willReturn(Optional.of(aCourtCaseEntity()))

        val caseCommentsForDefendant = defendantCaseCommentsService.getCaseCommentsForDefendant("CRN123")
        assertThat(caseCommentsForDefendant[0].comment).isEqualTo("Some comment")
        assertThat(caseCommentsForDefendant[0].author).isEqualTo("Some author")
        assertThat(caseCommentsForDefendant[0].created).isEqualTo("2024-05-22T12:00")
        assertThat(caseCommentsForDefendant[0].createdBy).isEqualTo("Test User")
        assertThat(caseCommentsForDefendant[0].lastUpdated).isEqualTo("2024-05-22T12:30")
        assertThat(caseCommentsForDefendant[0].lastUpdatedBy).isEqualTo("Test User")
        assertThat(caseCommentsForDefendant[0].caseNumber).isEqualTo("222333")
    }

    @Test
    fun `given crn does not exist then no comments are returned`() {
        given(defendantRepositoryFacade.findDefendantsByCrn("CRN123")).willReturn(Collections.emptyList())
        verifyNoInteractions(caseCommentsService)
        verifyNoInteractions(immutableCourtCaseService)
        val caseCommentsForDefendant = defendantCaseCommentsService.getCaseCommentsForDefendant("CRN123")
        assertThat(caseCommentsForDefendant).isEmpty()
    }
}