package uk.gov.justice.probation.courtcaseservice.client.model.listeners

import lombok.*


@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor(access = AccessLevel.PRIVATE, force = true)
data class DomainEvent(
  val eventType: String,
  val detailUrl: String,
)
