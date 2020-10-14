package uk.gov.justice.probation.courtcaseservice.restclient.communityapi.mapper;

import java.util.Collections;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;
import uk.gov.justice.probation.courtcaseservice.restclient.communityapi.model.CommunityApiCourtAppearanceResponse;
import uk.gov.justice.probation.courtcaseservice.restclient.communityapi.model.CommunityApiCourtAppearancesResponse;
import uk.gov.justice.probation.courtcaseservice.service.model.CourtAppearance;


public class CourtAppearanceMapper {

    public static List<CourtAppearance> appearancesFrom(final CommunityApiCourtAppearancesResponse courtAppearancesResponse) {

        return Optional.ofNullable(courtAppearancesResponse.getCourtAppearances()).orElse(Collections.emptyList())
                            .stream()
                            .map(CourtAppearanceMapper::buildCourtAppearance)
                            .collect(Collectors.toList());
    }

    static CourtAppearance buildCourtAppearance(final CommunityApiCourtAppearanceResponse appearance) {

        return CourtAppearance.builder()
            .courtCode(appearance.getCourtCode())
            .type(appearance.getAppearanceType())
            .date(appearance.getAppearanceDate())
            .courtName(appearance.getCourtName())
            .build();
    }

}
