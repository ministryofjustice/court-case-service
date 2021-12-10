package uk.gov.justice.probation.courtcaseservice.controller.model;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonInclude.Include;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import uk.gov.justice.probation.courtcaseservice.service.model.KeyValue;

import java.time.LocalDate;

@Schema(description = "Sentence Response")
@Data
@AllArgsConstructor
@Builder
@NoArgsConstructor(access = AccessLevel.PRIVATE, force = true)
@JsonInclude(Include.NON_NULL)
public class CustodyDetail {

    private final KeyValue custodialType;
    private final LocalDate licenceExpiryDate;
    private final LocalDate pssEndDate;
}
