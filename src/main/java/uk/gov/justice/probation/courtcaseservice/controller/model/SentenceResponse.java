package uk.gov.justice.probation.courtcaseservice.controller.model;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonInclude.Include;
import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import uk.gov.justice.probation.courtcaseservice.service.model.UnpaidWork;

import java.util.List;

@ApiModel(description = "Sentence Response")
@Data
@Builder
@AllArgsConstructor
@JsonInclude(Include.NON_NULL)
public class SentenceResponse {

        @ApiModelProperty(value = "List of Attendances")
        private final List<AttendanceResponse> attendances;

        @ApiModelProperty(value = "UPW")
        private final UnpaidWork unpaidWork;

        @ApiModelProperty(value = "Sentence with current order header info")
        private final CurrentOrderHeaderResponse currentOrderHeaderDetail;


}
