package uk.gov.justice.probation.courtcaseservice.service.model;

import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor(access = AccessLevel.PRIVATE, force = true)
public class Page<T> {
    private Integer pageNumber;
    private Integer resultsPerPage;
    private Integer totalResults;
    private List<T> pageItems;
}
