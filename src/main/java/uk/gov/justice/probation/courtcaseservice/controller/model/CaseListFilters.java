package uk.gov.justice.probation.courtcaseservice.controller.model;

import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import uk.gov.justice.probation.courtcaseservice.jpa.entity.CourtSession;
import uk.gov.justice.probation.courtcaseservice.jpa.entity.DefendantProbationStatus;

import java.util.Arrays;
import java.util.List;

@Data
@AllArgsConstructor
@Builder
@NoArgsConstructor(access = AccessLevel.PRIVATE, force = true)
public class CaseListFilters {
    private final List<String> probationStatus = Arrays.stream(DefendantProbationStatus.class.getEnumConstants()).map(Enum::name).toList();
    private final List<String> session = Arrays.stream(CourtSession.class.getEnumConstants()).map(Enum::name).toList();
    private final List<String> courtRoom;
    private final int possibleNdeliusRecords;
    private final int recentlyAdded;
    private final int size;
    private final int totalNoOfPages;
}
