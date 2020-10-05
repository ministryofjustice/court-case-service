package uk.gov.justice.probation.courtcaseservice.service.model;

import java.time.LocalDate;
import java.util.List;
import com.fasterxml.jackson.annotation.JsonInclude;
import io.swagger.annotations.ApiModel;
import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@ApiModel("Registration")
@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor(access = AccessLevel.PRIVATE, force = true)
@JsonInclude(JsonInclude.Include.NON_NULL)
public class Registration {
    private final String type;
    private final LocalDate startDate;
    private final LocalDate nextReviewDate;
    private final LocalDate endDate;
    private final List<String> notes;
    private final boolean active;
}
