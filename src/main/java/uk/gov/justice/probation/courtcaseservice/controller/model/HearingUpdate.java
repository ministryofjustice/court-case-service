package uk.gov.justice.probation.courtcaseservice.controller.model;

import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import uk.gov.justice.probation.courtcaseservice.jpa.entity.HearingDefendantEntity;
import uk.gov.justice.probation.courtcaseservice.jpa.entity.HearingEntity;
import uk.gov.justice.probation.courtcaseservice.jpa.entity.OffenceEntity;

import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;

@Data
@Builder
@NoArgsConstructor(access = AccessLevel.PRIVATE, force = true)
@AllArgsConstructor
class HearingUpdate {
    private LocalDateTime created;
    private List<HearingDay> hearingDays;
    private List<String> defendantIds;
    private List<OffenceEntity> offences;

    public static HearingUpdate of(HearingEntity hearingEntity) {
        return HearingUpdate.builder().created(hearingEntity.getFirstCreated())
            .hearingDays(hearingEntity.getHearingDays().stream().map(HearingDay::of).collect(Collectors.toList()))
            .defendantIds(hearingEntity.getHearingDefendants().stream().map(HearingDefendantEntity::getDefendantId).collect(Collectors.toList()))
            .offences(hearingEntity.getHearingDefendants().stream().map(HearingDefendantEntity::getOffences).flatMap(List::stream).collect(Collectors.toList()))
            .build();
    }
}