package uk.gov.justice.probation.courtcaseservice.service

import hex.genmodel.easy.EasyPredictModelWrapper
import org.junit.jupiter.api.Assertions.*
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.extension.ExtendWith
import org.mockito.InjectMocks
import org.mockito.Mock
import org.mockito.Mockito.*
import org.mockito.junit.jupiter.MockitoExtension
import uk.gov.justice.probation.courtcaseservice.client.ManageOffencesRestClient
import uk.gov.justice.probation.courtcaseservice.jpa.entity.CourtCaseEntity
import uk.gov.justice.probation.courtcaseservice.jpa.entity.EntityHelper
import uk.gov.justice.probation.courtcaseservice.jpa.entity.HearingDayEntity
import uk.gov.justice.probation.courtcaseservice.jpa.entity.HearingEntity
import java.time.LocalDate
import java.time.LocalTime


@ExtendWith(MockitoExtension::class)
internal class ShortTermCustodyPredictorServiceTest {

    @Mock
    lateinit var offencesRestClient: ManageOffencesRestClient

    @Mock
    lateinit var easyPredictModelWrapper: EasyPredictModelWrapper

    @InjectMocks
    lateinit var shortTermCustodyPredictorService: ShortTermCustodyPredictorService

    @Test
    fun should_NOT_call_offences_API_for_crown_court_hearings() {
        // Given
        val hearingDayEntity = HearingDayEntity.builder()
            .day(LocalDate.now())
            .time(LocalTime.now())
            .courtRoom(EntityHelper.COURT_ROOM)
            .courtCode("C22SW")
            .build()

        val hearingEntity = HearingEntity.builder()
            .courtCase(CourtCaseEntity.builder()
                .caseNo("caseNumber")
                .build())
            .hearingDefendants(listOf(EntityHelper.aHearingDefendantEntity()))
            .hearingDays(listOf(hearingDayEntity))
            .build()

        // When
        shortTermCustodyPredictorService.addPredictorScoresToHearing(hearingEntity)

        // Then
        verify(offencesRestClient, never()).getHomeOfficeOffenceCodeByCJSCode(anyString())
    }

    @Test
    fun should_call_offences_API_for_magistrate_court_hearings() {
        // Given
        val hearingDayEntity = HearingDayEntity.builder()
            .day(LocalDate.now())
            .time(LocalTime.now())
            .courtRoom(EntityHelper.COURT_ROOM)
            .courtCode("B01GU")
            .build()

        val hearingEntity = HearingEntity.builder()
            .courtCase(CourtCaseEntity.builder()
                .caseNo("caseNumber")
                .build())
            .hearingDefendants(listOf(EntityHelper.aHearingDefendantEntity()))
            .hearingDays(listOf(hearingDayEntity))
            .build()

        // When
        shortTermCustodyPredictorService.addPredictorScoresToHearing(hearingEntity)

        // Then
        verify(offencesRestClient).getHomeOfficeOffenceCodeByCJSCode(anyString())
    }


}