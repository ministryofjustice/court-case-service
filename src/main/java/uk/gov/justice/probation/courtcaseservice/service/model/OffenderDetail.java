package uk.gov.justice.probation.courtcaseservice.service.model;

import io.swagger.annotations.ApiModel;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import uk.gov.justice.probation.courtcaseservice.controller.model.ProbationStatus;

import java.time.LocalDate;
import java.util.List;

@ApiModel("The Offender detail")
@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class OffenderDetail {
    private OtherIds otherIds;
    private String title;
    private String forename;
    private List<String> middleNames;
    private String surname;
    private LocalDate dateOfBirth;
    private ProbationStatus probationStatus;
}
