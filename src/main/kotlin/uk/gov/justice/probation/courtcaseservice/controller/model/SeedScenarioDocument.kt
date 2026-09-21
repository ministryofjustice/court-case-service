package uk.gov.justice.probation.courtcaseservice.controller.model

import com.fasterxml.jackson.annotation.JsonIgnoreProperties
import java.time.LocalDate
import java.time.LocalTime

@JsonIgnoreProperties(ignoreUnknown = true)
data class SeedScenarioDocument(
  val clean: Boolean = false,
  val cases: List<ScenarioCase> = emptyList(),
)

@JsonIgnoreProperties(ignoreUnknown = true)
data class ScenarioCase(
  val caseNo: String? = null,
  val urn: String? = null,
  val sourceType: String? = "COMMON_PLATFORM",
  val caseMarkers: List<String> = emptyList(),
  val comments: List<ScenarioCaseComment> = emptyList(),
  val defendants: List<ScenarioDefendant> = emptyList(),
  val hearings: List<ScenarioHearing> = emptyList(),
)

@JsonIgnoreProperties(ignoreUnknown = true)
data class ScenarioCaseComment(
  val author: String? = null,
  val comment: String = "",
  val draft: Boolean = false,
  val legacy: Boolean = false,
)

@JsonIgnoreProperties(ignoreUnknown = true)
data class ScenarioDefendant(
  val defendantName: String? = null,
  val type: String? = "PERSON",
  val sex: String? = "MALE",
  val name: ScenarioName? = null,
  val address: ScenarioAddress? = null,
  val phoneNumber: ScenarioPhoneNumber? = null,
  val dateOfBirth: LocalDate? = null,
  val cro: String? = null,
  val crn: String? = null,
  val pnc: String? = null,
  val offender: ScenarioOffender? = null,
  val nationality1: String? = null,
  val nationality2: String? = null,
)

@JsonIgnoreProperties(ignoreUnknown = true)
data class ScenarioOffender(
  val crn: String? = null,
  val pnc: String? = null,
  val cro: String? = null,
  val probationStatus: String? = null,
  val awaitingPsr: Boolean? = null,
  val breach: Boolean = false,
  val preSentenceActivity: Boolean = false,
  val suspendedSentenceOrder: Boolean = false,
  val previouslyKnownTerminationDate: LocalDate? = null,
)

@JsonIgnoreProperties(ignoreUnknown = true)
data class ScenarioName(
  val title: String? = null,
  val forename1: String? = null,
  val forename2: String? = null,
  val forename3: String? = null,
  val surname: String? = null,
)

@JsonIgnoreProperties(ignoreUnknown = true)
data class ScenarioAddress(
  val line1: String? = null,
  val line2: String? = null,
  val line3: String? = null,
  val line4: String? = null,
  val line5: String? = null,
  val postcode: String? = null,
)

@JsonIgnoreProperties(ignoreUnknown = true)
data class ScenarioPhoneNumber(
  val home: String? = null,
  val mobile: String? = null,
  val work: String? = null,
)

@JsonIgnoreProperties(ignoreUnknown = true)
data class ScenarioHearing(
  val hearingType: String? = "sentence",
  val listNo: String? = "1",
  val hearingEventType: String? = null,
  val hearingDays: List<ScenarioHearingDay> = emptyList(),
  val defendants: List<ScenarioHearingDefendant> = emptyList(),
)

@JsonIgnoreProperties(ignoreUnknown = true)
data class ScenarioHearingDay(
  val day: LocalDate? = null,
  val time: LocalTime? = null,
  val courtCode: String? = "B10JQ",
  val courtRoom: String? = "1",
)

@JsonIgnoreProperties(ignoreUnknown = true)
data class ScenarioHearingDefendant(
  val prepStatus: String? = null,
  val outcomeNotRequired: Boolean? = null,
  val notes: List<ScenarioHearingNote> = emptyList(),
  val offences: List<ScenarioOffence> = emptyList(),
)

@JsonIgnoreProperties(ignoreUnknown = true)
data class ScenarioHearingNote(
  val author: String? = null,
  val note: String = "",
  val draft: Boolean = false,
  val legacy: Boolean = false,
)

@JsonIgnoreProperties(ignoreUnknown = true)
data class ScenarioOffence(
  val title: String = "",
  val summary: String = "",
  val act: String? = null,
  val sequence: Int = 1,
  val listNo: Int = 1,
  val offenceCode: String? = null,
  val plea: ScenarioPlea? = null,
  val verdict: ScenarioVerdict? = null,
  val judicialResults: List<ScenarioJudicialResult> = emptyList(),
)

@JsonIgnoreProperties(ignoreUnknown = true)
data class ScenarioPlea(
  val value: String? = null,
  val date: LocalDate? = null,
)

@JsonIgnoreProperties(ignoreUnknown = true)
data class ScenarioVerdict(
  val typeDescription: String? = null,
  val date: LocalDate? = null,
)

@JsonIgnoreProperties(ignoreUnknown = true)
data class ScenarioJudicialResult(
  val label: String? = null,
  val resultText: String? = null,
  val isConvictedResult: Boolean = false,
)
