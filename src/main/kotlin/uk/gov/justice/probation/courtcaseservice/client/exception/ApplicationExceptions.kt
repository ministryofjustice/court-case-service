package uk.gov.justice.probation.courtcaseservice.client.exception

import org.springframework.http.HttpMethod

class ExternalApiEntityNotFoundException(
  msg: String? = "",
  val method: HttpMethod,
  val url: String,
  val client: ExternalService,
) : RuntimeException(msg)

class ExternalApiAuthorisationException(
  msg: String? = "",
  val method: HttpMethod,
  val url: String,
  val client: ExternalService,
) : RuntimeException(msg)

class ExternalApiForbiddenException(
  msg: String? = "",
  val method: HttpMethod,
  val url: String,
  val client: ExternalService,
) : RuntimeException(msg)

class ExternalApiInvalidRequestException(
  msg: String? = "",
  val method: HttpMethod,
  val url: String,
  val client: ExternalService,
) : RuntimeException(msg)

class ExternalApiUnknownException(
  msg: String? = "",
  val method: HttpMethod,
  val url: String,
  val client: ExternalService,
) : RuntimeException(msg)

class ExternalApiConflictException(
  msg: String? = "",
  val method: HttpMethod,
  val url: String,
  val client: ExternalService,
) : RuntimeException(msg)
