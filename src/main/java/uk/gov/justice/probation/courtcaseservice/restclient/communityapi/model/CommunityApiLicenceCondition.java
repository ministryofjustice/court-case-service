package uk.gov.justice.probation.courtcaseservice.restclient.communityapi.model;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import uk.gov.justice.probation.courtcaseservice.service.model.KeyValue;

import java.time.LocalDate;

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

    @Schema(description = "Is the licence condition currently active")
    private Boolean active;

}
