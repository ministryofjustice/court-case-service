package uk.gov.justice.probation.courtcaseservice.service.model;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import javax.validation.constraints.Min;
import javax.validation.constraints.NotBlank;
import javax.validation.constraints.NotNull;
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

    public Integer getPage() {
        return Optional.ofNullable(page).orElse(1);
    }

    public Integer getSize() {
        return Optional.ofNullable(size).orElse(10);
    }
}
