package uk.gov.justice.probation.courtcaseservice.restclient.communityapi.model;

import java.time.LocalDate;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import io.swagger.annotations.ApiModelProperty;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import uk.gov.justice.probation.courtcaseservice.service.model.KeyValue;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
@JsonIgnoreProperties(ignoreUnknown = true)
public class CommunityApiLicenceCondition {

    private String licenceConditionNotes;
    private LocalDate startDate;
    private String commencementNotes;
    private String terminationNotes;
    private KeyValue licenceConditionTypeMainCat;
    private KeyValue licenceConditionTypeSubCat;

    @ApiModelProperty(value = "Is the licence condition currently active")
    private Boolean active;

}
