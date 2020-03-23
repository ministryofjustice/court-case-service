package uk.gov.justice.probation.courtcaseservice.service;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import uk.gov.justice.probation.courtcaseservice.controller.model.AttendancesResponse;
import uk.gov.justice.probation.courtcaseservice.restclient.ConvictionRestClient;
import uk.gov.justice.probation.courtcaseservice.restclient.exception.OffenderNotFoundException;

@Service
@Slf4j
public class ConvictionService {

    private final ConvictionRestClient restClient;

    @Autowired
    public ConvictionService(final ConvictionRestClient client) {
        this.restClient = client;
    }

    public AttendancesResponse getAttendances(final String crn, final Long convictionId) {
        return restClient.getAttendancesByCrnAndConvictionId(crn, convictionId)
            .blockOptional()
            .orElseThrow(() -> new OffenderNotFoundException(crn));
    }

}
