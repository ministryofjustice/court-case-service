package uk.gov.justice.probation.courtcaseservice.controller

import io.swagger.v3.oas.annotations.Hidden
import io.swagger.v3.oas.annotations.Operation
import io.swagger.v3.oas.annotations.tags.Tag
import org.springframework.http.MediaType.APPLICATION_JSON_VALUE
import org.springframework.web.bind.annotation.PutMapping
import org.springframework.web.bind.annotation.RestController
import uk.gov.justice.probation.courtcaseservice.service.BailInformationService

@Tag(name = "Bail Information Service API")
@RestController
class BailInformationServiceController(private val bailInformationService: BailInformationService) {

  @Operation(description = "Triggers export of data to the Bail Information Service SharePoint site")
  @Hidden
  @PutMapping(value = ["/bail-information-service-export"], produces = [APPLICATION_JSON_VALUE])
  fun exportBailInformationServiceData() {
    bailInformationService.exportReport()
  }
}
