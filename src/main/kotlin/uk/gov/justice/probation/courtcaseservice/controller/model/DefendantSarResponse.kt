package uk.gov.justice.probation.courtcaseservice.controller.model

import uk.gov.justice.probation.courtcaseservice.jpa.entity.DefendantType
import java.time.LocalDate

data class DefendantSarResponse(
  val crn: String?,
  val defendantName: String,
  val defendantType: DefendantType,
  val address: AddressSarResponse,
  val pnc: String?,
  val cro: String?,
  val dateOfBirth: LocalDate,
  val sex: String,
  val nationality1: String?,
  val nationality2: String?,
  val manualUpdate: Boolean,
  val offenderConfirmed: Boolean,
  val phoneNumber: PhoneNumberSarResponse?,
)
