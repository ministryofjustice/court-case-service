package uk.gov.justice.probation.courtcaseservice.controller.model;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonInclude.Include;
import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.*;
import uk.gov.justice.probation.courtcaseservice.service.model.UnpaidWork;

import java.util.List;

@ApiModel(description = "Sentence Response")
@Data
@AllArgsConstructor
@Builder
@NoArgsConstructor(access = AccessLevel.PRIVATE, force = true)
@JsonInclude(Include.NON_NULL)
public class SentenceResponse {

        @ApiModelProperty(value = "List of Attendances")
        private final List<AttendanceResponse> attendances;

        @ApiModelProperty(value = "UPW")
        private final UnpaidWork unpaidWork;

        @ApiModelProperty(value = "Sentence with current order header info")
        private final CurrentOrderHeaderResponse currentOrderHeaderDetail;

}
