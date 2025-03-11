package uk.gov.justice.probation.courtcaseservice.client.model.listeners

import com.fasterxml.jackson.annotation.JsonProperty
import java.time.LocalDateTime

data class SQSMessage(

  @JsonProperty("Type")
  val type: String,

  @JsonProperty("Message")
  val message: String,

  @JsonProperty("MessageId")
  val messageId: String? = null,

  @JsonProperty("Timestamp")
  val timeStamp: LocalDateTime? = null,
)
