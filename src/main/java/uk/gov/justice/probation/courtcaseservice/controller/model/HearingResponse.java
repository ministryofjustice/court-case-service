package uk.gov.justice.probation.courtcaseservice.controller.model;

import java.time.LocalDateTime;
import com.fasterxml.jackson.annotation.JsonInclude;
import io.swagger.annotations.ApiModel;
import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import uk.gov.justice.probation.courtcaseservice.jpa.entity.CourtSession;

@ApiModel(description = "Hearing Information")
@Data
@AllArgsConstructor
@NoArgsConstructor(access = AccessLevel.PRIVATE, force = true)
@Builder
@JsonInclude(JsonInclude.Include.NON_NULL)
public class HearingResponse {

    private final String listNo;
    private final String courtCode;
    private final String courtRoom;
    private final LocalDateTime sessionStartTime;
    private final CourtSession session;

}
