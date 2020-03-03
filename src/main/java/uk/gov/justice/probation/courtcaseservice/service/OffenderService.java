package uk.gov.justice.probation.courtcaseservice.service;

import lombok.AllArgsConstructor;
import lombok.NoArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import uk.gov.justice.probation.courtcaseservice.restclient.OffenderRestClient;
import uk.gov.justice.probation.courtcaseservice.restclient.exception.OffenderNotFoundException;
import uk.gov.justice.probation.courtcaseservice.service.model.Offender;

@Service
@Slf4j
@NoArgsConstructor
@AllArgsConstructor
public class OffenderService {
    @Autowired
    private OffenderRestClient client;

    public Offender getOffender(String crn) {
        return client.getOffenderByCrn(crn)
                .blockOptional()
                .orElseThrow(() -> new OffenderNotFoundException(crn));
    }
}
