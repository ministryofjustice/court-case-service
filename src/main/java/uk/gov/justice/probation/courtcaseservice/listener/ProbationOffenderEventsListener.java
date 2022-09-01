package uk.gov.justice.probation.courtcaseservice.listener;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jms.annotation.JmsListener;
import org.springframework.stereotype.Component;
import uk.gov.justice.probation.courtcaseservice.jpa.entity.OffenderEntity;
import uk.gov.justice.probation.courtcaseservice.jpa.entity.OffenderProbationStatus;
import uk.gov.justice.probation.courtcaseservice.jpa.repository.OffenderRepository;
import uk.gov.justice.probation.courtcaseservice.service.OffenderService;
import uk.gov.justice.probation.courtcaseservice.service.exceptions.EntityNotFoundException;
import uk.gov.justice.probation.courtcaseservice.service.model.ProbationStatusDetail;
import uk.gov.justice.probation.courtcaseservice.service.model.event.ProbationOffenderEvent;

@Slf4j
@Component
public class ProbationOffenderEventsListener {

    private final OffenderRepository offenderRepository;
    private final OffenderService offenderService;
    private final ObjectMapper objectMapper;

    @Autowired
    public ProbationOffenderEventsListener(OffenderRepository offenderRepository, OffenderService offenderService, ObjectMapper objectMapper) {
        this.offenderRepository = offenderRepository;
        this.offenderService = offenderService;
        this.objectMapper = objectMapper;
    }

    @JmsListener(destination = "picprobationoffendereventsqueue", containerFactory = "hmppsQueueContainerFactoryProxy")
    private void processMessage(String rawMessage) {
        //TODO
        // 1) extract the message
        // 2) check crn present
        // 3) If yes update the offender probation status

    }

    private ProbationOffenderEvent getProbationOffenderEvent(String message) {
        try {
            return objectMapper.readValue(message, ProbationOffenderEvent.class);
        } catch (JsonProcessingException e) {
            log.error("Failed to read event message %s", e);
            return null;
        }
    }

    private OffenderEntity getOffender(String crn) {
        return offenderRepository.findByCrn(crn)
                .orElseThrow(() -> new EntityNotFoundException("Offender with crn %s does not exist", crn));
    }

    //TODO better move this to offender service
    private OffenderEntity updateOffenderProbationStatus(String crn) {
        ProbationStatusDetail probationStatusDetail = offenderService.getProbationStatus(crn).block();
        if (probationStatusDetail == null) {
            log.error("Probation status details not available for {}", crn);
            return null;

        }
        OffenderEntity offender = getOffender(crn);
        offender.setProbationStatus(OffenderProbationStatus.of(probationStatusDetail.getStatus()));
        offender.setPreviouslyKnownTerminationDate(probationStatusDetail.getPreviouslyKnownTerminationDate());
        offender.setBreach(probationStatusDetail.getInBreach());
        offender.setAwaitingPsr(probationStatusDetail.getAwaitingPsr());
        offender.setPreSentenceActivity(probationStatusDetail.isPreSentenceActivity());
        return offenderRepository.save(offender);

    }
}
