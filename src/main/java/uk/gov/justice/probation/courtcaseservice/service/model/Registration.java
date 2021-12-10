package uk.gov.justice.probation.courtcaseservice.service.model;

import com.fasterxml.jackson.annotation.JsonInclude;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDate;
import java.util.List;

@Schema(description = "Registration")
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
