package uk.gov.justice.probation.courtcaseservice.restclient.communityapi.mapper.interventions;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;
import uk.gov.justice.probation.courtcaseservice.restclient.communityapi.model.CommunityApiNsi;
import uk.gov.justice.probation.courtcaseservice.restclient.communityapi.model.CommunityApiNsiResponse;
import uk.gov.justice.probation.courtcaseservice.restclient.communityapi.model.CommunityApiNsiStatus;
import uk.gov.justice.probation.courtcaseservice.restclient.communityapi.model.CommunityApiNsiType;
import uk.gov.justice.probation.courtcaseservice.service.model.Breach;

public class BreachMapper {
    public static Breach breachFrom(CommunityApiNsi nsi) {
        return Breach.builder()
            .breachId(nsi.getNsiId())
            .description(Optional.ofNullable(nsi.getSubType()).map(CommunityApiNsiType::getDescription).orElse(null))
            .status(Optional.ofNullable(nsi.getStatus()).map(CommunityApiNsiStatus::getDescription).orElse(null))
            .started(nsi.getActualStartDate())
            .statusDate(Optional.ofNullable(nsi.getStatusDateTime()).map(LocalDateTime::toLocalDate).orElse(null))
            .build();
    }

    public static List<Breach> breachesFrom(CommunityApiNsiResponse nsis) {
        return nsis.getNsis().stream().map(BreachMapper::breachFrom).collect(Collectors.toList());
    }
}
