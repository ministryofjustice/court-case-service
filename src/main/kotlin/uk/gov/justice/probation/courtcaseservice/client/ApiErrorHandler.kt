package uk.gov.justice.probation.courtcaseservice.client

import org.springframework.http.HttpMethod
import org.springframework.http.HttpStatus
import org.springframework.web.reactive.function.client.ClientResponse
import reactor.core.publisher.Mono
import uk.gov.justice.probation.courtcaseservice.client.exception.ExternalApiAuthorisationException
import uk.gov.justice.probation.courtcaseservice.client.exception.ExternalApiConflictException
import uk.gov.justice.probation.courtcaseservice.client.exception.ExternalApiEntityNotFoundException
import uk.gov.justice.probation.courtcaseservice.client.exception.ExternalApiForbiddenException
import uk.gov.justice.probation.courtcaseservice.client.exception.ExternalApiInvalidRequestException
import uk.gov.justice.probation.courtcaseservice.client.exception.ExternalApiUnknownException
import uk.gov.justice.probation.courtcaseservice.client.exception.ExternalService

fun handle4xxError(
  clientResponse: ClientResponse,
  method: HttpMethod,
  url: String,
  client: ExternalService,
): Mono<out Throwable?>? = when (clientResponse.statusCode()) {
  HttpStatus.BAD_REQUEST -> {
    clientResponse.bodyToMono(String::class.java)
      .map { error -> ExternalApiInvalidRequestException(error, method, url, client) }
  }
  HttpStatus.UNAUTHORIZED -> {
    clientResponse.bodyToMono(String::class.java)
      .map { error -> ExternalApiAuthorisationException(error, method, url, client) }
  }
  HttpStatus.FORBIDDEN -> {
    clientResponse.bodyToMono(String::class.java)
      .map { error -> ExternalApiForbiddenException(error, method, url, client) }
  }
  HttpStatus.NOT_FOUND -> {
    clientResponse.bodyToMono(String::class.java)
      .map { error -> ExternalApiEntityNotFoundException(error, method, url, client) }
  }
  HttpStatus.CONFLICT -> {
    clientResponse.bodyToMono(String::class.java)
      .map { error -> ExternalApiConflictException(error, method, url, client) }
  }
  else -> handleError(clientResponse, method, url, client)
}

fun handle5xxError(
  message: String,
  method: HttpMethod,
  path: String,
  service: ExternalService,
): Mono<out Throwable?>? = throw ExternalApiUnknownException(
  message,
  method,
  path,
  service,
)

fun handleError(
  clientResponse: ClientResponse,
  method: HttpMethod,
  url: String,
  client: ExternalService,
): Mono<out Throwable?>? {
  val httpStatus = clientResponse.statusCode()
  return clientResponse.bodyToMono(String::class.java).map { error ->
    ExternalApiUnknownException(error, method, url, client)
  }.or(
    Mono.error(
      ExternalApiUnknownException(
        "Unexpected exception with no body and status $httpStatus",
        method,
        url,
        client,
      ),
    ),
  )
}
