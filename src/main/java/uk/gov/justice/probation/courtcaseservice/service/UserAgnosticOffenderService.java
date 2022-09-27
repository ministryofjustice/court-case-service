package uk.gov.justice.probation.courtcaseservice.service;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Mono;
import uk.gov.justice.probation.courtcaseservice.jpa.entity.OffenderEntity;
import uk.gov.justice.probation.courtcaseservice.jpa.entity.OffenderProbationStatus;
import uk.gov.justice.probation.courtcaseservice.jpa.repository.OffenderRepository;
import uk.gov.justice.probation.courtcaseservice.restclient.OffenderRestClient;
import uk.gov.justice.probation.courtcaseservice.restclient.OffenderRestClientFactory;
import uk.gov.justice.probation.courtcaseservice.service.model.ProbationStatusDetail;

import java.util.Optional;

@Service
@Slf4j
public class UserAgnosticOffenderService {

    private final OffenderRestClient userAgnosticOffenderRestClient;

    private final OffenderRepository offenderRepository;

    private final TelemetryService telemetryService;

    @Autowired
    public UserAgnosticOffenderService(final OffenderRestClientFactory offenderRestClientFactory,
                                       final OffenderRepository offenderRepository, TelemetryService telemetryService) {
        this.userAgnosticOffenderRestClient = offenderRestClientFactory.buildUserAgnosticOffenderRestClient();
        this.offenderRepository = offenderRepository;
        this.telemetryService = telemetryService;
    }


    public Mono<ProbationStatusDetail> getProbationStatusWithoutRestrictions(String crn) {
        return userAgnosticOffenderRestClient.getProbationStatusByCrn(crn);
    }

    public Optional<OffenderEntity> updateOffenderProbationStatus(String crn) {
        ProbationStatusDetail probationStatusDetail = getProbationStatusWithoutRestrictions(crn).block();
        if (probationStatusDetail == null) {
            log.error("Probation status details not available for {}", crn);
            return Optional.empty();
        }
        return offenderRepository.findByCrn(crn)
                .map(offenderEntity -> updateProbationStatusDetails(probationStatusDetail, offenderEntity))
                .map(offenderRepository::save);
    }

    private OffenderEntity updateProbationStatusDetails(ProbationStatusDetail probationStatusDetail, OffenderEntity offender) {
        return offender.withProbationStatus(OffenderProbationStatus.of(probationStatusDetail.getStatus()))
                .withPreviouslyKnownTerminationDate(probationStatusDetail.getPreviouslyKnownTerminationDate())
                .withBreach(probationStatusDetail.getInBreach())
                .withAwaitingPsr(probationStatusDetail.getAwaitingPsr())
                .withPreSentenceActivity(probationStatusDetail.isPreSentenceActivity());
    }

}
