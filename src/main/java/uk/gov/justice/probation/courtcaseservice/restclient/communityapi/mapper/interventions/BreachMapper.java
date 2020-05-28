package uk.gov.justice.probation.courtcaseservice.restclient.communityapi.mapper.interventions;

import org.springframework.stereotype.Component;
import uk.gov.justice.probation.courtcaseservice.restclient.communityapi.model.CommunityApiNsi;
import uk.gov.justice.probation.courtcaseservice.restclient.communityapi.model.CommunityApiNsiResponse;
import uk.gov.justice.probation.courtcaseservice.service.model.Breach;

import java.util.List;
import java.util.stream.Collectors;

@Component
public class BreachMapper {
    public Breach breachFrom(CommunityApiNsi nsi) {
        return Breach.builder()
            .breachId(nsi.getNsiId())
            .description(nsi.getSubType().getDescription())
            .status(nsi.getStatus().getDescription())
            .started(nsi.getActualStartDate())
            .build();
    }

    public List<Breach> breachesFrom(CommunityApiNsiResponse nsis) {
        return nsis.getNsis().stream().map(nsi -> breachFrom(nsi)).collect(Collectors.toList());
    }
}
