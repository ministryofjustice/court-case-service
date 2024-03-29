package uk.gov.justice.probation.courtcaseservice.service.mapper;

import kotlin.Pair;
import org.apache.commons.lang3.StringUtils;
import org.springframework.stereotype.Component;
import uk.gov.justice.probation.courtcaseservice.jpa.entity.CourtCaseEntity;
import uk.gov.justice.probation.courtcaseservice.jpa.entity.CourtEntity;
import uk.gov.justice.probation.courtcaseservice.jpa.entity.DefendantEntity;
import uk.gov.justice.probation.courtcaseservice.jpa.entity.HearingDayEntity;
import uk.gov.justice.probation.courtcaseservice.jpa.entity.HearingDefendantEntity;
import uk.gov.justice.probation.courtcaseservice.jpa.entity.HearingEntity;
import uk.gov.justice.probation.courtcaseservice.jpa.entity.OffenceEntity;
import uk.gov.justice.probation.courtcaseservice.service.model.CaseSearchResultItem;

import java.time.Clock;
import java.time.LocalDateTime;
import java.util.Collection;
import java.util.Comparator;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@Component
public class CaseSearchResultItemMapper {

    private final Clock clock;

    public CaseSearchResultItemMapper(final Clock clock) {
        this.clock = clock;
    }

    public CaseSearchResultItem from(CourtCaseEntity courtCaseEntity, final DefendantEntity defendant) {

        // filter out hearing defendants that does not match the CRN as the case may have multiple defendants with different CRNs
        var hearingDefendants = courtCaseEntity.getHearings().stream().map(HearingEntity::getHearingDefendants)
            .flatMap(Collection::stream)
            .filter(hearingDefendantEntity -> StringUtils.equalsIgnoreCase(hearingDefendantEntity.getDefendantId(), defendant.getDefendantId()))
            .collect(Collectors.toList());

        var offenceTitles = hearingDefendants.stream()
            .flatMap(hearingDefendantEntity -> hearingDefendantEntity.getOffences().stream())
            .map(OffenceEntity::getTitle).collect(Collectors.toSet());

        var lastAndNextHearings = getLastAndNextHearings(hearingDefendants);
        var lastHearing = lastAndNextHearings.getFirst();
        var nextHearing = lastAndNextHearings.getSecond();

        CaseSearchResultItem.CaseSearchResultItemBuilder caseSearchResultItemBuilder = CaseSearchResultItem.builder()
            .hearingId(hearingDefendants.get(0).getHearing().getHearingId())
            .defendantId(defendant.getDefendantId())
            .defendantName(defendant.getDefendantName())
            .offenceTitles(offenceTitles.stream().toList())
            .probationStatus(defendant.getProbationStatusForDisplay())
            .lastHearingDate(lastHearing.map(HearingDayEntity::getDay).orElse(null))
            .lastHearingCourt(lastHearing.map(HearingDayEntity::getCourt).map(CourtEntity::getName).orElse(null))
            .nextHearingDate(nextHearing.map(HearingDayEntity::getDay).orElse(null))
            .nextHearingCourt(nextHearing.map(HearingDayEntity::getCourt).map(CourtEntity::getName).orElse(null));

        Optional.ofNullable(defendant.getOffender()).ifPresent(offenderEntity -> caseSearchResultItemBuilder.crn(offenderEntity.getCrn())
            .breach(offenderEntity.isBreach())
            .awaitingPsr(offenderEntity.getAwaitingPsr()));

        return caseSearchResultItemBuilder
            .build();
    }

    private Pair<Optional<HearingDayEntity>, Optional<HearingDayEntity>> getLastAndNextHearings(List<HearingDefendantEntity> hearingDefendants) {
        final var hearingDays = hearingDefendants.stream().flatMap(hearingDefendantEntity -> hearingDefendantEntity.getHearing().getHearingDays().stream()).collect(Collectors.toList());
        hearingDays.sort(Comparator.comparing(HearingDayEntity::getDay));

        Optional<HearingDayEntity> lastHearing = Optional.empty();
        Optional<HearingDayEntity> nextHearing = Optional.empty();
        var now = LocalDateTime.now(clock);
        // TODO : investigate below as nextHearing is always empty!
        for(int i = 0; i < hearingDays.size() && nextHearing.isEmpty(); i++) {

            HearingDayEntity hearingDayEntity = hearingDays.get(i);
            if(LocalDateTime.of(hearingDayEntity.getDay(), hearingDayEntity.getTime()).isAfter(now)) {
                nextHearing = Optional.of(hearingDayEntity);
                break;
            }
            lastHearing = Optional.of(hearingDayEntity);
        }
        return new Pair<>(lastHearing, nextHearing);
    }

}
