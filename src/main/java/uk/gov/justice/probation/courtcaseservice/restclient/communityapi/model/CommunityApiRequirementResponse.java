package uk.gov.justice.probation.courtcaseservice.restclient.communityapi.model;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import lombok.AllArgsConstructor;
import lombok.Getter;

import java.time.LocalDate;
import lombok.NoArgsConstructor;
import uk.gov.justice.probation.courtcaseservice.service.model.KeyValue;

@Getter
@AllArgsConstructor
@NoArgsConstructor
@JsonIgnoreProperties(ignoreUnknown = true)
public class CommunityApiRequirementResponse {

    private Long requirementId;
    private String requirementNotes;

    private LocalDate commencementDate;
    private LocalDate startDate;
    private LocalDate terminationDate;
    private LocalDate expectedStartDate;
    private LocalDate expectedEndDate;

    private Boolean active;
    private KeyValue requirementTypeSubCategory;
    private KeyValue requirementTypeMainCategory;
    private KeyValue adRequirementTypeMainCategory;
    private KeyValue adRequirementTypeSubCategory;
    private KeyValue terminationReason;
    private Long length;
    private String lengthUnit;
}
