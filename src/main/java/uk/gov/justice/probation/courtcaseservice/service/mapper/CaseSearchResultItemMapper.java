package uk.gov.justice.probation.courtcaseservice.service.mapper;

import kotlin.Pair;
import lombok.AccessLevel;
import lombok.NoArgsConstructor;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import uk.gov.justice.probation.courtcaseservice.jpa.entity.CourtCaseEntity;
import uk.gov.justice.probation.courtcaseservice.jpa.entity.CourtEntity;
import uk.gov.justice.probation.courtcaseservice.jpa.entity.HearingDayEntity;
import uk.gov.justice.probation.courtcaseservice.jpa.entity.HearingDefendantEntity;
import uk.gov.justice.probation.courtcaseservice.jpa.entity.OffenceEntity;
import uk.gov.justice.probation.courtcaseservice.service.model.CaseSearchResultItem;

import java.time.Clock;
import java.time.LocalDateTime;
import java.util.Comparator;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@Component
@NoArgsConstructor(access = AccessLevel.PRIVATE, force = true)
public class CaseSearchResultItemMapper {
    @Autowired
    private final Clock clock;

    public CaseSearchResultItemMapper(final Clock clock) {
        this.clock = clock;
    }


    public CaseSearchResultItem from(CourtCaseEntity courtCaseEntity, final String crn) {

        // filter out hearing defendants that does not match teh CRN - as the case may have multiple defendants with different CRNs
        var hearingDefendants = courtCaseEntity.getHearings().stream()
            .flatMap(hearingEntity -> hearingEntity.getHearingDefendants().stream()
                .filter(hearingDefendantEntity -> Optional.ofNullable(hearingDefendantEntity.getDefendant().getOffender())
                    .map(offenderEntity -> StringUtils.equalsIgnoreCase(offenderEntity.getCrn(), crn)).orElse(false)))
            .collect(Collectors.toList());

        var defendant = hearingDefendants.get(0).getDefendant();
        var offenceTitles = hearingDefendants.stream()
            .flatMap(hearingDefendantEntity -> hearingDefendantEntity.getOffences().stream())
            .map(OffenceEntity::getTitle).collect(Collectors.toSet());

        final var hearingDays = hearingDefendants.stream().flatMap(hearingDefendantEntity -> hearingDefendantEntity.getHearing().getHearingDays().stream()).collect(Collectors.toList());
        hearingDays.sort(Comparator.comparing(HearingDayEntity::getDay));

        var lastAndNextHearings = getLastAndNextHearings(hearingDefendants);
        var lastHearing = lastAndNextHearings.getFirst();
        var nextHearing = lastAndNextHearings.getSecond();

        return CaseSearchResultItem.builder()
            .crn(crn)
            .defendantName(defendant.getDefendantName())
            .offenceTitles(offenceTitles.stream().toList())
            .probationStatus(defendant.getProbationStatusForDisplay())
            .lastHearingDate(lastHearing.map(HearingDayEntity::getDay).orElse(null))
            .lastHearingCourt(lastHearing.map(HearingDayEntity::getCourt).map(CourtEntity::getName).orElse(null))
            .nextHearingDate(nextHearing.map(HearingDayEntity::getDay).orElse(null))
            .nextHearingCourt(nextHearing.map(HearingDayEntity::getCourt).map(CourtEntity::getName).orElse(null))
            .build();
    }

    private Pair<Optional<HearingDayEntity>, Optional<HearingDayEntity>> getLastAndNextHearings(List<HearingDefendantEntity> hearingDefendants) {
        final var hearingDays = hearingDefendants.stream().flatMap(hearingDefendantEntity -> hearingDefendantEntity.getHearing().getHearingDays().stream()).collect(Collectors.toList());
        hearingDays.sort(Comparator.comparing(HearingDayEntity::getDay));

        Optional<HearingDayEntity> lastHearing = Optional.empty();
        Optional<HearingDayEntity> nextHearing = Optional.empty();
        var now = LocalDateTime.now(clock);

        for(int i = 0; i < hearingDays.size() && nextHearing.isEmpty(); i++) {

            HearingDayEntity hearingDayEntity = hearingDays.get(i);
            if(LocalDateTime.of(hearingDayEntity.getDay(), hearingDayEntity.getTime()).isAfter(now)) {
                nextHearing = Optional.of(hearingDayEntity);
                break;
            }
            lastHearing = Optional.of(hearingDayEntity);
        }
        return new Pair(lastHearing, nextHearing);
    }

}
