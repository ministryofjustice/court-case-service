package uk.gov.justice.probation.courtcaseservice.service.model;

import com.fasterxml.jackson.annotation.JsonInclude;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import uk.gov.justice.probation.courtcaseservice.jpa.entity.DefendantProbationStatus;

import java.time.LocalDate;
import java.util.List;

@Schema(description = "The Offender detail")
@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
@JsonInclude(JsonInclude.Include.NON_NULL)
public class OffenderDetail {
    private OtherIds otherIds;
    private String title;
    private String forename;
    private List<String> middleNames;
    private String surname;
    private LocalDate dateOfBirth;
    private DefendantProbationStatus probationStatus;
}
