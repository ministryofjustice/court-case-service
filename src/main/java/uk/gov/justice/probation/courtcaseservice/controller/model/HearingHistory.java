package uk.gov.justice.probation.courtcaseservice.controller.model;

import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import uk.gov.justice.probation.courtcaseservice.jpa.entity.HearingEntity;

import java.util.List;
import java.util.stream.Collectors;

@Data
@Builder
@NoArgsConstructor(access = AccessLevel.PRIVATE, force = true)
@AllArgsConstructor
class HearingHistory {
    private String hearingId;
    private List<HearingUpdate> hearingUpdates;

    public static HearingHistory of(String hearingId, List<HearingEntity> hearingEntities) {
        return HearingHistory.builder()
            .hearingId(hearingId)
            .hearingUpdates(hearingEntities.stream().map(HearingUpdate::of).collect(Collectors.toList()))
            .build();
    }
}