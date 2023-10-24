package uk.gov.justice.probation.courtcaseservice.client.model.listeners

import com.fasterxml.jackson.databind.PropertyNamingStrategies
import com.fasterxml.jackson.databind.annotation.JsonNaming
import lombok.*

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor(access = AccessLevel.PRIVATE, force = true)
@Suppress("PropertyName")
data class SQSMessage(val type: String, val message: String, val messageId: String? = null)
