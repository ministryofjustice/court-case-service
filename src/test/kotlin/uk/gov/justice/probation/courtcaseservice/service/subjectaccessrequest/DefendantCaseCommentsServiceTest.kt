package uk.gov.justice.probation.courtcaseservice.service.subjectaccessrequest

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
import uk.gov.justice.probation.courtcaseservice.service.CaseCommentsService
import uk.gov.justice.probation.courtcaseservice.service.ImmutableCourtCaseService
import java.time.LocalDate
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

    private val defendant = aDefendantEntity("1234", "CRN123")

    @BeforeEach
    fun setup() {
        defendantCaseCommentsService = DefendantCaseCommentsService(caseCommentsService, immutableCourtCaseService)
    }

    @Test
    fun `given defendants found with case comments should return sars comments response`() {
        given(defendantRepositoryFacade.findDefendantsByCrn("CRN123")).willReturn(listOf(defendant))
        given(caseCommentsService.getCaseCommentsForDefendant("1234")).willReturn(listOf(aCaseCommentEntity()))
        given(immutableCourtCaseService.findByCaseId("5678")).willReturn(Optional.empty())

        val caseCommentsForDefendant = defendantCaseCommentsService.getCaseCommentsForDefendant(
            defendant.hearingDefendants.first(),
            null,
            null
        )
        assertThat(caseCommentsForDefendant[0].comment).isEqualTo("Some comment")
        assertThat(caseCommentsForDefendant[0].author).isEqualTo("Author")
        assertThat(caseCommentsForDefendant[0].created).isEqualTo("2024-05-22T12:00")
        assertThat(caseCommentsForDefendant[0].lastUpdated).isEqualTo("2024-05-22T12:30")
        assertThat(caseCommentsForDefendant[0].lastUpdatedBy).isEqualTo("TestUser")
        assertThat(caseCommentsForDefendant[0].caseNumber).isEqualTo("")
    }

    @Test
    fun `given defendants found with case comments should return sars comments response between from and to dates`() {
        val fromDate = LocalDate.of(2004, 1, 1)
        val toDate = LocalDate.of(2004, 1, 2)

        given(defendantRepositoryFacade.findDefendantsByCrn("CRN123")).willReturn(listOf(defendant))
        given(caseCommentsService.getCaseCommentsForDefendantBetween("1234", fromDate, toDate)).willReturn(listOf(aCaseCommentEntity()))
        given(immutableCourtCaseService.findByCaseId("5678")).willReturn(Optional.empty())

        val caseCommentsForDefendant = defendantCaseCommentsService.getCaseCommentsForDefendant(
            defendant.hearingDefendants.first(),
            fromDate,
            toDate
        )
        assertThat(caseCommentsForDefendant[0].comment).isEqualTo("Some comment")
        assertThat(caseCommentsForDefendant[0].author).isEqualTo("Author")
        assertThat(caseCommentsForDefendant[0].created).isEqualTo("2024-05-22T12:00")
        assertThat(caseCommentsForDefendant[0].lastUpdated).isEqualTo("2024-05-22T12:30")
        assertThat(caseCommentsForDefendant[0].lastUpdatedBy).isEqualTo("TestUser")
        assertThat(caseCommentsForDefendant[0].caseNumber).isEqualTo("")
    }

    @Test
    fun `given defendants found with case comments should return sars comments response after from date`() {
        val fromDate = LocalDate.of(2004, 1, 1)

        given(defendantRepositoryFacade.findDefendantsByCrn("CRN123")).willReturn(listOf(aDefendantEntity("1234", "CRN123")))
        given(caseCommentsService.getCaseCommentsForDefendantFrom("1234", fromDate)).willReturn(listOf(aCaseCommentEntity()))
        given(immutableCourtCaseService.findByCaseId("5678")).willReturn(Optional.empty())

        val caseCommentsForDefendant = defendantCaseCommentsService.getCaseCommentsForDefendant(
            defendant.hearingDefendants.first(),
            fromDate,
            null
        )
        assertThat(caseCommentsForDefendant[0].comment).isEqualTo("Some comment")
        assertThat(caseCommentsForDefendant[0].author).isEqualTo("Author")
        assertThat(caseCommentsForDefendant[0].created).isEqualTo("2024-05-22T12:00")
        assertThat(caseCommentsForDefendant[0].lastUpdated).isEqualTo("2024-05-22T12:30")
        assertThat(caseCommentsForDefendant[0].lastUpdatedBy).isEqualTo("TestUser")
        assertThat(caseCommentsForDefendant[0].caseNumber).isEqualTo("")
    }

    @Test
    fun `given defendants found with case comments should return sars comments response before to date`() {
        val toDate = LocalDate.of(2004, 1, 1)

        given(defendantRepositoryFacade.findDefendantsByCrn("CRN123")).willReturn(listOf(aDefendantEntity("1234", "CRN123")))
        given(caseCommentsService.getCaseCommentsForDefendantTo("1234", toDate)).willReturn(listOf(aCaseCommentEntity()))
        given(immutableCourtCaseService.findByCaseId("5678")).willReturn(Optional.empty())

        val caseCommentsForDefendant = defendantCaseCommentsService.getCaseCommentsForDefendant(
            defendant.hearingDefendants.first(),
            null,
            toDate
        )
        assertThat(caseCommentsForDefendant[0].comment).isEqualTo("Some comment")
        assertThat(caseCommentsForDefendant[0].author).isEqualTo("Author")
        assertThat(caseCommentsForDefendant[0].created).isEqualTo("2024-05-22T12:00")
        assertThat(caseCommentsForDefendant[0].lastUpdated).isEqualTo("2024-05-22T12:30")
        assertThat(caseCommentsForDefendant[0].lastUpdatedBy).isEqualTo("TestUser")
        assertThat(caseCommentsForDefendant[0].caseNumber).isEqualTo("")
    }

    @Test
    fun `given defendants found with case comments and LIBRA court case entry should return sars comments response`() {
        given(defendantRepositoryFacade.findDefendantsByCrn("CRN123")).willReturn(listOf(aDefendantEntity("1234", "CRN123")))
        given(caseCommentsService.getCaseCommentsForDefendant("1234")).willReturn(listOf(aCaseCommentEntity()))
        given(immutableCourtCaseService.findByCaseId("5678")).willReturn(Optional.of(aCourtCaseEntity()))

        val caseCommentsForDefendant = defendantCaseCommentsService.getCaseCommentsForDefendant(
            defendant.hearingDefendants.first(),
            null,
            null
        )
        assertThat(caseCommentsForDefendant[0].comment).isEqualTo("Some comment")
        assertThat(caseCommentsForDefendant[0].author).isEqualTo("Author")
        assertThat(caseCommentsForDefendant[0].created).isEqualTo("2024-05-22T12:00")
        assertThat(caseCommentsForDefendant[0].lastUpdated).isEqualTo("2024-05-22T12:30")
        assertThat(caseCommentsForDefendant[0].lastUpdatedBy).isEqualTo("TestUser")
        assertThat(caseCommentsForDefendant[0].caseNumber).isEqualTo("222333")
    }

    @Test
    fun `given hearing defendant with no comments  then no comments are returned`() {
        given(caseCommentsService.getCaseCommentsForDefendant(defendant.defendantId)).willReturn(Collections.emptyList())
        verifyNoInteractions(caseCommentsService)
        verifyNoInteractions(immutableCourtCaseService)
        val caseCommentsForDefendant = defendantCaseCommentsService.getCaseCommentsForDefendant(
            defendant.hearingDefendants.first(),
            null,
            null
        )
        assertThat(caseCommentsForDefendant).isEmpty()
    }

}