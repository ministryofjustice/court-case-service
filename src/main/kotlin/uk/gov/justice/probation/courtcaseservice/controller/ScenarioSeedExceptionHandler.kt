package uk.gov.justice.probation.courtcaseservice.controller

import org.springframework.http.HttpStatus
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.ExceptionHandler
import org.springframework.web.bind.annotation.RestControllerAdvice
import org.springframework.http.converter.HttpMessageNotReadableException
import uk.gov.justice.probation.courtcaseservice.controller.model.SeedResponse

@RestControllerAdvice(assignableTypes = [SeedController::class])
class ScenarioSeedExceptionHandler {

  @ExceptionHandler(HttpMessageNotReadableException::class)
  fun handleNotReadable(exception: HttpMessageNotReadableException): ResponseEntity<SeedResponse> =
    ResponseEntity.status(HttpStatus.BAD_REQUEST).body(
      SeedResponse(
        message = "Invalid scenario payload.",
        details = exception.mostSpecificCause.message ?: "Request body could not be read.",
      ),
    )
}
