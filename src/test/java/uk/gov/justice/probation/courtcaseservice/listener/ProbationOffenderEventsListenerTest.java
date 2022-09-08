package uk.gov.justice.probation.courtcaseservice.listener;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Captor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import reactor.core.publisher.Mono;
import uk.gov.justice.probation.courtcaseservice.jpa.entity.OffenderEntity;
import uk.gov.justice.probation.courtcaseservice.jpa.entity.OffenderProbationStatus;
import uk.gov.justice.probation.courtcaseservice.jpa.repository.OffenderRepository;
import uk.gov.justice.probation.courtcaseservice.service.OffenderService;
import uk.gov.justice.probation.courtcaseservice.service.exceptions.EntityNotFoundException;
import uk.gov.justice.probation.courtcaseservice.service.model.ProbationStatusDetail;
import uk.gov.justice.probation.courtcaseservice.service.model.event.ProbationOffenderEvent;

import java.time.LocalDate;
import java.time.Month;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
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

    @Mock
    private OffenderRepository offenderRepository;

    @Captor
    private ArgumentCaptor<OffenderEntity> offenderEntityArgumentCaptor;

    private String offenderEventMessage;

    private ProbationStatusDetail probationStatusDetail;

    private OffenderEntity offenderEntityBeforeUpdate;

    private OffenderEntity offenderEntityAfterUpdate;


    @BeforeEach
    public void setUp() {
        offenderEventMessage = "{\n" +
                "   \"offenderId\":1,\n" +
                "   \"crn\":\"crn\",\n" +
                "   \"nomsNumber\":\"noms\"\n" +
                "}".stripIndent();

        probationStatusDetail = ProbationStatusDetail.builder()
                .awaitingPsr(true)
                .inBreach(true)
                .preSentenceActivity(true)
                .status("Current")
                .previouslyKnownTerminationDate(LocalDate.of(2025, Month.SEPTEMBER, 01))
                .build();

        offenderEntityBeforeUpdate = OffenderEntity.builder()
                .crn("crn")
                .awaitingPsr(false)
                .breach(false)
                .breach(false)
                .preSentenceActivity(false)
                .probationStatus(OffenderProbationStatus.NOT_SENTENCED)
                .build();

        offenderEntityAfterUpdate = OffenderEntity.builder()
                .crn("crn")
                .awaitingPsr(true)
                .breach(true)
                .preSentenceActivity(true)
                .probationStatus(OffenderProbationStatus.CURRENT)
                .previouslyKnownTerminationDate(LocalDate.of(2022, Month.SEPTEMBER, 1))
                .build();
    }

    @Test
    public void should_updateOffenderProbationStatus_whenProbationOffenderEventMessageReceived() throws JsonProcessingException {
        when(objectMapper.readValue(anyString(), eq(EventMessage.class))).thenReturn(EventMessage.builder().message(offenderEventMessage).build());
        when(objectMapper.readValue(anyString(), eq(ProbationOffenderEvent.class))).thenReturn(ProbationOffenderEvent.builder().crn("crn").build());

        when(offenderService.getProbationStatus(anyString())).thenReturn(Mono.just(probationStatusDetail));
        when(offenderRepository.findByCrn(anyString())).thenReturn(Optional.of(offenderEntityBeforeUpdate));

        when(offenderRepository.save(any())).thenReturn(offenderEntityAfterUpdate);

        probationOffenderEventsListener.processMessage(offenderEventMessage);

        verify(offenderService).getProbationStatus("crn");
        verify(offenderRepository).findByCrn("crn");

        verify(offenderRepository).save(offenderEntityArgumentCaptor.capture());

        var offenderEntityToUpdate = offenderEntityArgumentCaptor.getValue();

        assertThat(offenderEntityToUpdate.getProbationStatus()).isEqualTo(OffenderProbationStatus.CURRENT);
        assertThat(offenderEntityToUpdate.isBreach()).isEqualTo(true);
        assertThat(offenderEntityToUpdate.isPreSentenceActivity()).isEqualTo(true);
        assertThat(offenderEntityToUpdate.getAwaitingPsr()).isEqualTo(true);
        assertThat(offenderEntityToUpdate.getPreviouslyKnownTerminationDate()).isEqualTo(LocalDate.of(2025, Month.SEPTEMBER, 1));
    }

    @Test
    public void shouldNot_updateOffenderProbationStatus_whenOffenderEventMessageHasNoCrn() throws JsonProcessingException {
        when(objectMapper.readValue(anyString(), eq(EventMessage.class))).thenReturn(EventMessage.builder().message(offenderEventMessage).build());
        when(objectMapper.readValue(anyString(), eq(ProbationOffenderEvent.class))).thenReturn(ProbationOffenderEvent.builder().crn("").build());

        probationOffenderEventsListener.processMessage(offenderEventMessage);

        verify(offenderService, times(0)).getProbationStatus("crn");
        verify(offenderRepository, times(0)).findByCrn("crn");
        verify(offenderRepository, times(0)).save(offenderEntityArgumentCaptor.capture());
    }

    @Test
    void shouldNot_updateOffenderProbationStatus_whenNoOffenderExistForTheCrn() throws JsonProcessingException {
        when(objectMapper.readValue(anyString(), eq(EventMessage.class))).thenReturn(EventMessage.builder().message(offenderEventMessage).build());
        when(objectMapper.readValue(anyString(), eq(ProbationOffenderEvent.class))).thenReturn(ProbationOffenderEvent.builder().crn("crn").build());

        when(offenderService.getProbationStatus(anyString())).thenReturn(Mono.just(probationStatusDetail));
        when(offenderRepository.findByCrn(anyString())).thenReturn(Optional.empty());

        Exception e = assertThrows(EntityNotFoundException.class, () -> probationOffenderEventsListener.processMessage("something"));

        verify(offenderService).getProbationStatus("crn");
        verify(offenderRepository).findByCrn("crn");
        verify(offenderRepository, times(0)).save(offenderEntityArgumentCaptor.capture());

    }
}
