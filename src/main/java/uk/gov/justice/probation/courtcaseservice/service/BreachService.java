package uk.gov.justice.probation.courtcaseservice.service;

import lombok.AllArgsConstructor;
import org.springframework.stereotype.Service;
import uk.gov.justice.probation.courtcaseservice.controller.model.BreachResponse;
import uk.gov.justice.probation.courtcaseservice.restclient.NsiRestClient;
import uk.gov.justice.probation.courtcaseservice.restclient.communityapi.mapper.NsiMapper;
import uk.gov.justice.probation.courtcaseservice.service.exceptions.BreachNotFoundException;

@Service
@AllArgsConstructor
public class BreachService {

    private final NsiRestClient nsiRestClient;
    private final NsiMapper breachMapper;

    public BreachResponse getBreach(String crn, Long convictionId, Long breachId) {
        return nsiRestClient.getNsiById(crn, convictionId, breachId)
                .blockOptional()
                .map(breachMapper::breachOf)
                .orElseThrow(() -> new BreachNotFoundException(String.format("Breach with id %s does not exist", breachId)));
    }
}
