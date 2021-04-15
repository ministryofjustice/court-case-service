package uk.gov.justice.probation.courtcaseservice.restclient.communityapi.mapper;

import java.util.Collection;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;
import uk.gov.justice.probation.courtcaseservice.restclient.communityapi.model.CommunityApiCommunityOrPrisonOffenderManager;
import uk.gov.justice.probation.courtcaseservice.restclient.communityapi.model.CommunityApiCommunityOrPrisonOffenderManagerResponse;
import uk.gov.justice.probation.courtcaseservice.restclient.communityapi.model.CommunityApiProbationArea;
import uk.gov.justice.probation.courtcaseservice.restclient.communityapi.model.CommunityApiStaff;
import uk.gov.justice.probation.courtcaseservice.restclient.communityapi.model.CommunityApiTeam;
import uk.gov.justice.probation.courtcaseservice.service.model.KeyValue;
import uk.gov.justice.probation.courtcaseservice.service.model.OffenderManager;
import uk.gov.justice.probation.courtcaseservice.service.model.Staff;
import uk.gov.justice.probation.courtcaseservice.service.model.Team;

import static java.util.function.Predicate.not;

public class OffenderManagerMapper {

    public static List<OffenderManager> offenderManagersFrom(CommunityApiCommunityOrPrisonOffenderManagerResponse response) {
        return Optional.ofNullable(response.getOffenderManagers())
            .stream()
            .flatMap(Collection::stream)
            .filter(not(CommunityApiCommunityOrPrisonOffenderManager::getIsPrisonOffenderManager))
            .map(OffenderManagerMapper::buildOffenderManager)
            .collect(Collectors.toList());
    }

    static OffenderManager buildOffenderManager(CommunityApiCommunityOrPrisonOffenderManager offenderManager) {
        return OffenderManager.builder()
            .staff(staffOf(offenderManager.getStaff()))
            .provider(Optional.ofNullable(offenderManager.getProbationArea()).map(CommunityApiProbationArea::getDescription).orElse(null))
            .team(teamOf(offenderManager.getTeam()))
            .allocatedDate(offenderManager.getFromDate())
            .build();
    }

    static Staff staffOf(final CommunityApiStaff staff) {
        return Optional.ofNullable(staff)
            .map(s -> Staff.builder()
                .forenames(s.getForenames())
                .surname(s.getSurname())
                .email(s.getEmail())
                .telephone(s.getPhoneNumber())
                .build())
            .orElse(null);
    }

    static Team teamOf(final CommunityApiTeam team) {
        return Optional.ofNullable(team)
            .map(tm -> Team.builder()
                .description(tm.getDescription())
                .district(tm.getDistrict().getDescription())
                .localDeliveryUnit(Optional.ofNullable(tm.getLocalDeliveryUnit()).map(KeyValue::getDescription).orElse(null))
                .telephone(tm.getTelephone())
                .build())
            .orElse(null);
    }
}
