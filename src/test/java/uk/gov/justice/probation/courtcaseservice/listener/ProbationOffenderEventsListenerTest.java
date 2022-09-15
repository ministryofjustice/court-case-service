package uk.gov.justice.probation.courtcaseservice.listener;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import uk.gov.justice.probation.courtcaseservice.service.OffenderService;
import uk.gov.justice.probation.courtcaseservice.service.model.event.ProbationOffenderEvent;

import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
public class ProbationOffenderEventsListenerTest {

    @InjectMocks
    private ProbationOffenderEventsListener probationOffenderEventsListener;

    @Mock
    private ObjectMapper objectMapper;

    @Mock
    private OffenderService offenderService;

    private String offenderEventMessage;


    @BeforeEach
    public void setUp() {
        offenderEventMessage = "{\n" +
                "   \"offenderId\":1,\n" +
                "   \"crn\":\"crn\",\n" +
                "   \"nomsNumber\":\"noms\"\n" +
                "}".stripIndent();

    }

    @Test
    public void should_updateOffenderProbationStatus_whenProbationOffenderEventMessageReceived() throws JsonProcessingException {
        when(objectMapper.readValue(anyString(), eq(EventMessage.class))).thenReturn(EventMessage.builder().message(offenderEventMessage).build());
        when(objectMapper.readValue(anyString(), eq(ProbationOffenderEvent.class))).thenReturn(ProbationOffenderEvent.builder().crn("crn").build());

        probationOffenderEventsListener.processMessage(offenderEventMessage);

        verify(offenderService).updateOffenderProbationStatus("crn");
    }

    @Test
    public void shouldNot_updateOffenderProbationStatus_whenOffenderEventMessageHasNoCrn() throws JsonProcessingException {
        when(objectMapper.readValue(anyString(), eq(EventMessage.class))).thenReturn(EventMessage.builder().message(offenderEventMessage).build());
        when(objectMapper.readValue(anyString(), eq(ProbationOffenderEvent.class))).thenReturn(ProbationOffenderEvent.builder().crn("").build());

        probationOffenderEventsListener.processMessage(offenderEventMessage);

        verify(offenderService, times(0)).updateOffenderProbationStatus("crn");
    }
}
