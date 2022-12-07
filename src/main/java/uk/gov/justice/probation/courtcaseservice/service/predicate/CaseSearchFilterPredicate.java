package uk.gov.justice.probation.courtcaseservice.service.predicate;

import uk.gov.justice.probation.courtcaseservice.controller.model.CaseSearchFilter;
import uk.gov.justice.probation.courtcaseservice.jpa.entity.HearingDayEntity;
import uk.gov.justice.probation.courtcaseservice.jpa.entity.HearingDefendantEntity;
import uk.gov.justice.probation.courtcaseservice.jpa.entity.HearingEntity;

import javax.validation.constraints.NotNull;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import java.util.function.BiPredicate;
import java.util.function.Predicate;
import java.util.stream.Collectors;

import static org.apache.commons.lang3.ObjectUtils.isNotEmpty;

public class CaseSearchFilterPredicate {

    public static List<HearingEntity> applyFilters(List<HearingEntity> searchResults, CaseSearchFilter searchFilter) {
        return applyFilters(searchResults, getRecentlyAddedPredicate(searchFilter), getStatusPredicate(searchFilter), getCourtRoomPredicate(searchFilter), getCourtSessionPredicate(searchFilter));
    }

    private static List<HearingEntity> applyFilters(List<HearingEntity> searchResults,
                                                    Predicate<HearingEntity> recentlyAddedPredicate,
                                                    BiPredicate<HearingEntity, HearingDefendantEntity> statusPredicate,
                                                    BiPredicate<HearingEntity, HearingDayEntity> courtRoomPredicate,
                                                    BiPredicate<HearingEntity, HearingDayEntity> courtSessionPredicate) {

        //filter by recently added, status, room, session
        if (recentlyAddedPredicate != null && statusPredicate != null && courtRoomPredicate != null && courtSessionPredicate != null) {
            return searchResults.stream()
                    .filter(recentlyAddedPredicate)
                    .filter(hearingEntity -> hearingEntity.getHearingDefendants().stream()
                            .anyMatch(hearingDefendantEntity -> statusPredicate.test(hearingEntity, hearingDefendantEntity)))
                    .filter(hearingEntity -> hearingEntity.getHearingDays().stream()
                            .anyMatch(hearingDayEntity -> courtRoomPredicate.test(hearingEntity, hearingDayEntity)))
                    .filter(hearingEntity -> hearingEntity.getHearingDays().stream()
                            .anyMatch(hearingDayEntity -> courtSessionPredicate.test(hearingEntity, hearingDayEntity)))
                    .toList();
        }


        //filter by recently added, status
        if (recentlyAddedPredicate != null && statusPredicate != null) {
            return searchResults.stream()
                    .filter(recentlyAddedPredicate)
                    .filter(hearingEntity -> hearingEntity.getHearingDefendants().stream()
                            .anyMatch(hearingDefendantEntity -> statusPredicate.test(hearingEntity, hearingDefendantEntity)))
                    .toList();
        }

        //filter by recently added,room
        if (recentlyAddedPredicate != null && courtRoomPredicate != null) {
            return searchResults.stream()
                    .filter(recentlyAddedPredicate)
                    .filter(hearingEntity -> hearingEntity.getHearingDays().stream()
                            .anyMatch(hearingDayEntity -> courtRoomPredicate.test(hearingEntity, hearingDayEntity)))
                    .toList();
        }

        //filter by recently added,session
        if (recentlyAddedPredicate != null && courtSessionPredicate != null) {
            return searchResults.stream()
                    .filter(recentlyAddedPredicate)
                    .filter(hearingEntity -> hearingEntity.getHearingDays().stream()
                            .anyMatch(hearingDayEntity -> courtSessionPredicate.test(hearingEntity, hearingDayEntity)))
                    .toList();
        }

        //filter by status, room, session
        if (statusPredicate != null && courtRoomPredicate != null && courtSessionPredicate != null) {
            return searchResults.stream()
                    .filter(hearingEntity -> hearingEntity.getHearingDefendants().stream()
                            .anyMatch(hearingDefendantEntity -> statusPredicate.test(hearingEntity, hearingDefendantEntity)))
                    .filter(hearingEntity -> hearingEntity.getHearingDays().stream()
                            .anyMatch(hearingDayEntity -> courtRoomPredicate.test(hearingEntity, hearingDayEntity)))
                    .filter(hearingEntity -> hearingEntity.getHearingDays().stream()
                            .anyMatch(hearingDayEntity -> courtSessionPredicate.test(hearingEntity, hearingDayEntity)))
                    .toList();
        }


        //filter by status, room
        if (statusPredicate != null && courtRoomPredicate != null) {
            return searchResults.stream()
                    .filter(hearingEntity -> hearingEntity.getHearingDefendants().stream()
                            .anyMatch(hearingDefendantEntity -> statusPredicate.test(hearingEntity, hearingDefendantEntity)))
                    .filter(hearingEntity -> hearingEntity.getHearingDays().stream()
                            .anyMatch(hearingDayEntity -> courtRoomPredicate.test(hearingEntity, hearingDayEntity)))
                    .toList();
        }

        //filter by status, session
        if (statusPredicate != null && courtSessionPredicate != null) {
            return searchResults.stream()
                    .filter(hearingEntity -> hearingEntity.getHearingDefendants().stream()
                            .anyMatch(hearingDefendantEntity -> statusPredicate.test(hearingEntity, hearingDefendantEntity)))
                    .filter(hearingEntity -> hearingEntity.getHearingDays().stream()
                            .anyMatch(hearingDayEntity -> courtSessionPredicate.test(hearingEntity, hearingDayEntity)))
                    .toList();
        }

        // filter by room and session
        if (courtRoomPredicate != null && courtSessionPredicate != null) {
            return searchResults.stream()
                    .filter(hearingEntity -> hearingEntity.getHearingDays().stream()
                            .anyMatch(hearingDayEntity -> courtRoomPredicate.test(hearingEntity, hearingDayEntity)))
                    .filter(hearingEntity -> hearingEntity.getHearingDays().stream()
                            .anyMatch(hearingDayEntity -> courtSessionPredicate.test(hearingEntity, hearingDayEntity)))
                    .toList();
        }

        //filter by recently added
        if (recentlyAddedPredicate != null) {
            return searchResults.stream()
                    .filter(recentlyAddedPredicate).collect(Collectors.toList());
        }

        //filter by status
        if (statusPredicate != null) {
            return searchResults.stream()
                    .filter(hearingEntity -> hearingEntity.getHearingDefendants().stream()
                            .anyMatch(hearingDefendantEntity -> statusPredicate.test(hearingEntity, hearingDefendantEntity))
                    ).toList();

        }

        // filter by courtroom
        if (courtRoomPredicate != null) {
            return searchResults.stream()
                    .filter(hearingEntity -> hearingEntity.getHearingDays().stream()
                            .anyMatch(hearingDayEntity -> courtRoomPredicate.test(hearingEntity, hearingDayEntity)))
                    .toList();

        }
        //filter by courtSession
        if (courtSessionPredicate != null) {
            return searchResults.stream()
                    .filter(hearingEntity -> hearingEntity.getHearingDays().stream()
                            .anyMatch(hearingDayEntity -> courtSessionPredicate.test(hearingEntity, hearingDayEntity)))
                    .toList();
        }

        //no filter
        return searchResults;
    }


    @NotNull
    private static BiPredicate<HearingEntity, HearingDefendantEntity> getStatusPredicate(CaseSearchFilter searchFilter) {
        if (isNotEmpty(searchFilter.getProbationStatus())) {
            return (hearingEntity, hearingDefendantEntity) -> searchFilter.getProbationStatus().contains(hearingDefendantEntity.getDefendant().getProbationStatusForDisplay().getName()) &&
                    hearingDefendantEntity.getHearing().getHearingId().equals(hearingEntity.getHearingId());
        }

        return null;
    }

    @NotNull
    private static BiPredicate<HearingEntity, HearingDayEntity> getCourtRoomPredicate(CaseSearchFilter searchFilter) {
        if (isNotEmpty(searchFilter.getCourtRoom())) {
            return (hearingEntity, hearingDayEntity) -> searchFilter.getCourtRoom().contains(hearingDayEntity.getCourtRoom()) &&
                    hearingDayEntity.getHearing().getHearingId().equals(hearingEntity.getHearingId());
        }
        return null;
    }

    @NotNull
    private static BiPredicate<HearingEntity, HearingDayEntity> getCourtSessionPredicate(CaseSearchFilter searchFilter) {
        if (isNotEmpty(searchFilter.getSession())) {
            return (hearingEntity, hearingDayEntity) -> searchFilter.getSession().contains(hearingDayEntity.getSession().name()) &&
                    hearingDayEntity.getHearing().getHearingId().equals(hearingEntity.getHearingId());
        }

        return null;
    }

    @NotNull
    private static Predicate<HearingEntity> getRecentlyAddedPredicate(CaseSearchFilter searchFilter) {
        if (searchFilter.isRecentlyAdded()) {
            return (hearingEntity -> LocalDate.now().isEqual(Optional.ofNullable(hearingEntity.getFirstCreated()).orElse(LocalDateTime.now()).toLocalDate()));
        }
        return null;
    }
}
