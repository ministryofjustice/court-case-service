package uk.gov.justice.probation.courtcaseservice.restclient.communityapi.mapper.interventions;

import java.util.List;
import java.util.stream.Collectors;

import org.springframework.stereotype.Component;
import uk.gov.justice.probation.courtcaseservice.service.model.Breach;
import uk.gov.justice.probation.courtcaseservice.restclient.communityapi.model.CommunityApiNsi;
import uk.gov.justice.probation.courtcaseservice.restclient.communityapi.model.CommunityApiNsiResponse;

@Component
public class BreachMapper {
    public Breach breachFrom(CommunityApiNsi nsi) {
        return Breach.builder()
            .id(Long.toString(nsi.getNsiId()))
            .description(nsi.getSubType().getDescription())
            .status(nsi.getStatus().getDescription())
            .started(nsi.getActualStartDate())
            .build();
    }

    public List<Breach> breachesFrom(CommunityApiNsiResponse nsis) {
        return nsis.getNsis().stream().map(nsi -> breachFrom(nsi)).collect(Collectors.toList());
    }
}
