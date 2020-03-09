package uk.gov.justice.probation.courtcaseservice.service;

import lombok.AllArgsConstructor;
import lombok.NoArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Mono;
import uk.gov.justice.probation.courtcaseservice.restclient.OffenderRestClient;
import uk.gov.justice.probation.courtcaseservice.restclient.exception.OffenderNotFoundException;
import uk.gov.justice.probation.courtcaseservice.service.model.Conviction;
import uk.gov.justice.probation.courtcaseservice.service.model.Offender;

import java.util.List;

@Service
@Slf4j
@NoArgsConstructor
@AllArgsConstructor
public class OffenderService {
    @Autowired
    private OffenderRestClient client;

    public Offender getOffender(String crn) {
        return Mono.zip(client.getOffenderByCrn(crn), client.getConvictionsByCrn(crn), this::combineOffenderAndConvictions)
                .blockOptional()
                .orElseThrow(() -> new OffenderNotFoundException(crn));
    }

    private Offender combineOffenderAndConvictions(Offender offender, List<Conviction> convictions) {
            offender.setConvictions(convictions);
            return offender;
    }
}
