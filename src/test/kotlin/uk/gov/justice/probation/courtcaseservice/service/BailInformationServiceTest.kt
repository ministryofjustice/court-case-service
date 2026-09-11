package uk.gov.justice.probation.courtcaseservice.service

import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.extension.ExtendWith
import org.mockito.Mock
import org.mockito.junit.jupiter.MockitoExtension
import org.mockito.kotlin.argumentCaptor
import org.mockito.kotlin.eq
import org.mockito.kotlin.given
import org.mockito.kotlin.verify
import uk.gov.justice.probation.courtcaseservice.client.SharePointClient
import uk.gov.justice.probation.courtcaseservice.jpa.repository.BailInformationServiceRepository
import java.nio.charset.StandardCharsets
import java.time.LocalDate
import java.time.LocalTime

@ExtendWith(MockitoExtension::class)
internal class BailInformationServiceTest {

  @Mock
  lateinit var repository: BailInformationServiceRepository

  @Mock
  lateinit var sharePointClient: SharePointClient

  private lateinit var service: BailInformationService

  @BeforeEach
  fun setUp() {
    service = BailInformationService(repository, sharePointClient)
  }

  @Test
  fun `exportReport should build CSV in memory and upload it to SharePoint`() {
    val row = BailInformationReportRow(
      hearingId = "H-100",
      hearingEventType = "Bail Application",
      hearingType = "Bail application",
      hearingDay = LocalDate.of(2026, 9, 2),
      hearingTime = LocalTime.of(10, 15, 0),
      offenceSummary = "Offence, \"quoted\" and line\nbreak",
      offenceTitle = "Theft",
      offenceSequence = 1,
      offenceAct = "s1",
      offenceCode = "ABC123",
      pleaId = "P-01",
      verdictId = "V-09",
      defendantName = "Smith, John",
      defendantType = "Person",
      defendantNameJson = "{\"name\":\"John Smith\"}",
      defendantAddress = "1 Test Street",
      crn = "A123456",
      pnc = "PNC123",
      cro = "CRO123",
      dateOfBirth = LocalDate.of(1990, 1, 15),
      sex = "M",
      nationality1 = "British",
      nationality2 = "Irish",
      urn = "URN-001",
      courtName = "Manchester Crown Court",
      courtCode = "B01",
    )

    given(repository.fetchBailInformationReport()).willReturn(listOf(row))

    service.exportReport()

    val contentCaptor = argumentCaptor<ByteArray>()
    verify(sharePointClient).uploadFile(eq("bail_information_report_${LocalDate.now()}.csv"), contentCaptor.capture())

    val csv = String(contentCaptor.firstValue, StandardCharsets.UTF_8)
    assertThat(csv).startsWith("Hearing ID,Hearing Event Type,Hearing Type")
    assertThat(csv).contains("\"Offence, \"\"quoted\"\" and line\nbreak\"")
    assertThat(csv).contains("\"Smith, John\"")
    assertThat(csv).contains("Bail application")
    assertThat(csv).contains("Manchester Crown Court")
  }
}
