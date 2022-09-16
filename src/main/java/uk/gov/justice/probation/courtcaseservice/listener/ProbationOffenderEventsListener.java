package uk.gov.justice.probation.courtcaseservice.listener;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jms.annotation.JmsListener;
import org.springframework.stereotype.Component;
import uk.gov.justice.probation.courtcaseservice.jpa.repository.OffenderRepository;
import uk.gov.justice.probation.courtcaseservice.service.OffenderService;
import uk.gov.justice.probation.courtcaseservice.service.model.event.ProbationOffenderEvent;

@Slf4j
@Component
public class ProbationOffenderEventsListener {

    private final OffenderService offenderService;
    private final ObjectMapper objectMapper;

    @Autowired
    public ProbationOffenderEventsListener(OffenderService offenderService, ObjectMapper objectMapper) {
        this.offenderService = offenderService;
        this.objectMapper = objectMapper;
    }

    @JmsListener(destination = "picprobationoffendereventsqueue", containerFactory = "hmppsQueueContainerFactoryProxy")
    public void processMessage(String rawMessage) {
        ProbationOffenderEvent probationOffenderEvent = getProbationOffenderEvent(rawMessage);
        if (probationOffenderEvent != null && !probationOffenderEvent.getCrn().isBlank()) {
            offenderService.updateOffenderProbationStatus(probationOffenderEvent.getCrn());
        }
    }

    private ProbationOffenderEvent getProbationOffenderEvent(String rawMessage) {
        try {
            var eventMessage = objectMapper.readValue(rawMessage, EventMessage.class);
            return objectMapper.readValue(eventMessage.getMessage(), ProbationOffenderEvent.class);
        } catch (JsonProcessingException e) {
            log.error("Failed to read event message {}", e.getMessage());
            return null;
        }
    }
}
