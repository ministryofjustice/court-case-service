package uk.gov.justice.probation.courtcaseservice.restclient.communityapi.model;


import java.time.LocalDateTime;
import com.fasterxml.jackson.annotation.JsonInclude;
import io.swagger.annotations.ApiModel;
import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import uk.gov.justice.probation.courtcaseservice.service.model.KeyValue;

@ApiModel(description = "Court Appearances from Community API")
@Builder
@Getter
@AllArgsConstructor
@NoArgsConstructor(access = AccessLevel.PRIVATE, force = true)
@JsonInclude(JsonInclude.Include.NON_NULL)
public class CommunityApiCourtAppearanceResponse {

    private final Long courtAppearanceId;
    private final LocalDateTime appearanceDate;
    private final String courtCode;
    private final String courtName;
    private final KeyValue appearanceType;
    private final String crn;
}
