package uk.gov.justice.probation.courtcaseservice.service.model;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.data.domain.Sort.Direction;

import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import java.util.Optional;

@Schema(description = "Case search request")
@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor(access = AccessLevel.PRIVATE, force = true)
public class CaseSearchRequest {
    @Min(1)
    private Integer page;

    @Min(1)
    private Integer size;

    @NotBlank
    private String term;

    @NotNull
    private CaseSearchType type;

    @Schema(description = "Optional court code to filter results")
    private String courtCode;

    @Schema(description = "Optional field to sort by")
    private CaseSearchSortFields sortBy;

    @Schema(description = "Optional sort order")
    private Direction order;

    public Integer getPage() {
        return Optional.ofNullable(page).orElse(1);
    }

    public Integer getSize() {
        return Optional.ofNullable(size).orElse(10);
    }

    public Direction getOrder() {
        return Optional.ofNullable(order).orElse(Direction.ASC);
    }

    public String getCourtCode() { return Optional.ofNullable(courtCode).orElse(""); }
}
