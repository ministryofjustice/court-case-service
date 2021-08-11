package uk.gov.justice.probation.courtcaseservice.controller.model;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonInclude.Include;
import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import uk.gov.justice.probation.courtcaseservice.service.model.UnpaidWork;

import java.time.LocalDate;
import java.util.List;

@ApiModel(description = "Sentence Response")
@Data
@AllArgsConstructor
@Builder
@NoArgsConstructor(access = AccessLevel.PRIVATE, force = true)
@JsonInclude(Include.NON_NULL)
public class SentenceResponse {

        private final Long sentenceId;
        private final String sentenceDescription;
        private final String mainOffenceDescription;
        private final LocalDate sentenceDate;
        private final LocalDate actualReleaseDate;
        private final Integer length;
        private final String lengthUnits;

        @ApiModelProperty(value = "List of Attendances")
        private final List<AttendanceResponse> attendances;

        @ApiModelProperty(value = "UPW")
        private final UnpaidWork unpaidWork;

        @ApiModelProperty(value = "Links relating to this sentence")
        private final SentenceLinks links;

        private final CustodyDetail custody;
}
