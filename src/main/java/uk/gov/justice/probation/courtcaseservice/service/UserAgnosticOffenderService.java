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

    private OffenderEntity getOffender(String crn) {
        return offenderRepository.findByCrn(crn)
                .orElse(null);
    }

    public OffenderEntity updateOffenderProbationStatus(String crn) {
        ProbationStatusDetail probationStatusDetail = getProbationStatusWithoutRestrictions(crn).block();
        if (probationStatusDetail == null) {
            log.error("Probation status details not available for {}", crn);
            return null;
        }
        OffenderEntity offender = getOffender(crn);
        if (offender != null) {
            updateProbationStatusDetails(probationStatusDetail, offender);
            var updatedOffenderEntity =  offenderRepository.save(offender);
            telemetryService.trackOffenderProbationStatusUpdateEvent(updatedOffenderEntity);
            return updatedOffenderEntity;
        }
        log.warn("Offender not found for  {}", crn);
        return null;
    }

    private void updateProbationStatusDetails(ProbationStatusDetail probationStatusDetail, OffenderEntity offender) {
        offender.setProbationStatus(OffenderProbationStatus.of(probationStatusDetail.getStatus()));
        offender.setPreviouslyKnownTerminationDate(probationStatusDetail.getPreviouslyKnownTerminationDate());
        offender.setBreach(probationStatusDetail.getInBreach());
        offender.setAwaitingPsr(probationStatusDetail.getAwaitingPsr());
        offender.setPreSentenceActivity(probationStatusDetail.isPreSentenceActivity());
    }

}
