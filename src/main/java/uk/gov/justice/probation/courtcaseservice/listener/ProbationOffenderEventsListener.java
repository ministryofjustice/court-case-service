package uk.gov.justice.probation.courtcaseservice.listener;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import io.awspring.cloud.sqs.annotation.SqsListener;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import uk.gov.justice.probation.courtcaseservice.service.UserAgnosticOffenderService;
import uk.gov.justice.probation.courtcaseservice.service.model.event.ProbationOffenderEvent;

@Slf4j
@Component
public class ProbationOffenderEventsListener {

    private final UserAgnosticOffenderService offenderService;
    private final ObjectMapper objectMapper;

    @Autowired
    public ProbationOffenderEventsListener(UserAgnosticOffenderService offenderService, ObjectMapper objectMapper) {
        this.offenderService = offenderService;
        this.objectMapper = objectMapper;
    }


    @SqsListener(value = "picprobationoffendereventsqueue", factory = "hmppsQueueContainerFactoryProxy")
    public void processMessage(String rawMessage) throws JsonProcessingException {
        ProbationOffenderEvent probationOffenderEvent = getProbationOffenderEvent(rawMessage);
        if (probationOffenderEvent != null && !probationOffenderEvent.getCrn().isBlank()) {
            offenderService.updateOffenderProbationStatus(probationOffenderEvent.getCrn());
        }
    }

    private ProbationOffenderEvent getProbationOffenderEvent(String rawMessage) throws JsonProcessingException {
            var eventMessage = objectMapper.readValue(rawMessage, EventMessage.class);
            return objectMapper.readValue(eventMessage.getMessage(), ProbationOffenderEvent.class);
    }
}
