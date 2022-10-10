package uk.gov.justice.probation.courtcaseservice.service;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Captor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import reactor.core.publisher.Mono;
import uk.gov.justice.probation.courtcaseservice.jpa.entity.OffenderEntity;
import uk.gov.justice.probation.courtcaseservice.jpa.entity.OffenderProbationStatus;
import uk.gov.justice.probation.courtcaseservice.jpa.repository.OffenderRepository;
import uk.gov.justice.probation.courtcaseservice.restclient.OffenderRestClient;
import uk.gov.justice.probation.courtcaseservice.restclient.OffenderRestClientFactory;
import uk.gov.justice.probation.courtcaseservice.service.model.ProbationStatusDetail;

import java.time.LocalDate;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.*;

@DisplayName("OffenderService tests")
@ExtendWith(MockitoExtension.class)
public class UserAgnosticOffenderServiceTest {

    private static final String CRN = "X123";


    @Mock
    private OffenderRestClientFactory offenderRestClientFactory;

    @Mock
    private OffenderRepository offenderRepository;

    @Mock
    private OffenderRestClient userAgnosticOffenderRestClient;
    @Mock
    private TelemetryService telemetryService;

    private UserAgnosticOffenderService service;

    @Captor
    private ArgumentCaptor<OffenderEntity> offenderEntityArgumentCaptor;

    @BeforeEach
    void beforeEach() {
        when(offenderRestClientFactory.buildUserAgnosticOffenderRestClient()).thenReturn(userAgnosticOffenderRestClient);
        service = new UserAgnosticOffenderService(offenderRestClientFactory, offenderRepository, telemetryService);
    }

    @Test
    void whenGetProbationStatusWithoutRestrictions_thenReturnDetailWithCorrectProbationStatus() {
        final var probationStatusDetail = ProbationStatusDetail.builder()
                .status(OffenderProbationStatus.CURRENT.getName())
                .build();

        when(userAgnosticOffenderRestClient.getProbationStatusByCrn(CRN)).thenReturn(Mono.just(probationStatusDetail));

        var detail = service.getProbationStatusWithoutRestrictions(CRN).block();

        assertThat(detail.getStatus()).isEqualTo(OffenderProbationStatus.CURRENT.getName());
    }

    @Test
    void shouldUpdateOffenderProbationStatus_givenOffenderFoundForTheCrn() {

        var date = LocalDate.now();

        final var probationStatusDetail = ProbationStatusDetail.builder()
                .status(OffenderProbationStatus.CURRENT.getName())
                .inBreach(true)
                .awaitingPsr(true)
                .previouslyKnownTerminationDate(date)
                .preSentenceActivity(true)
                .build();
        final var offenderEntity = OffenderEntity.builder()
                .crn(CRN)
                .probationStatus(OffenderProbationStatus.NOT_SENTENCED)
                .build();

        when(userAgnosticOffenderRestClient.getProbationStatusByCrn(CRN)).thenReturn(Mono.just(probationStatusDetail));
        when(offenderRepository.findByCrn(CRN)).thenReturn(Optional.ofNullable(offenderEntity));
        when(offenderRepository.save(any(OffenderEntity.class))).thenReturn(offenderEntity);


        service.updateOffenderProbationStatus(CRN);

        verify(userAgnosticOffenderRestClient).getProbationStatusByCrn(CRN);
        verify(offenderRepository).findByCrn(CRN);
        verify(offenderRepository).save(offenderEntityArgumentCaptor.capture());
        var entityToUpdate = offenderEntityArgumentCaptor.getValue();

        assertThat(entityToUpdate.getProbationStatus()).isEqualTo(OffenderProbationStatus.CURRENT);
        assertThat(entityToUpdate.getAwaitingPsr()).isEqualTo(true);
        assertThat(entityToUpdate.getPreviouslyKnownTerminationDate()).isEqualTo(date);
        assertThat(entityToUpdate.getCrn()).isEqualTo(CRN);
        assertThat(entityToUpdate.isPreSentenceActivity()).isEqualTo(true);
        assertThat(entityToUpdate.isBreach()).isEqualTo(true);
    }

    @Test
    void shouldUpdateOffenderProbationStatus_AndEmitTelemetryEvent() {

        var date = LocalDate.now();

        final var probationStatusDetail = ProbationStatusDetail.builder()
                .status(OffenderProbationStatus.CURRENT.getName())
                .inBreach(true)
                .awaitingPsr(true)
                .previouslyKnownTerminationDate(date)
                .preSentenceActivity(true)
                .build();
        final var offenderEntity = OffenderEntity.builder()
                .crn(CRN)
                .probationStatus(OffenderProbationStatus.NOT_SENTENCED)
                .build();

        final var updatedOffender = OffenderEntity.builder()
                .crn(CRN)
                .awaitingPsr(true)
                .previouslyKnownTerminationDate(date)
                .preSentenceActivity(true)
                .breach(true)
                .probationStatus(OffenderProbationStatus.CURRENT)
                .build();

        when(userAgnosticOffenderRestClient.getProbationStatusByCrn(CRN)).thenReturn(Mono.just(probationStatusDetail));
        when(offenderRepository.findByCrn(CRN)).thenReturn(Optional.ofNullable(offenderEntity));
        when(offenderRepository.save(any(OffenderEntity.class))).thenReturn(updatedOffender);

        service.updateOffenderProbationStatus(CRN);
        verify(telemetryService).trackOffenderProbationStatusUpdateEvent(offenderEntityArgumentCaptor.capture());
        var entityToUpdate = offenderEntityArgumentCaptor.getValue();

        assertThat(entityToUpdate.getProbationStatus()).isEqualTo(OffenderProbationStatus.CURRENT);
        assertThat(entityToUpdate.getAwaitingPsr()).isEqualTo(true);
        assertThat(entityToUpdate.getPreviouslyKnownTerminationDate()).isEqualTo(date);
        assertThat(entityToUpdate.getCrn()).isEqualTo(CRN);
        assertThat(entityToUpdate.isPreSentenceActivity()).isEqualTo(true);
        assertThat(entityToUpdate.isBreach()).isEqualTo(true);
    }

    @Test
    void shouldNotUpdateOffenderProbationStatus_givenOffenderNotFoundForTheCrn() {
        when(offenderRepository.findByCrn(CRN)).thenReturn(Optional.empty());

        service.updateOffenderProbationStatus(CRN);

        verify(offenderRepository).findByCrn(CRN);
        verify(userAgnosticOffenderRestClient,times(0)).getProbationStatusByCrn(CRN);
        verify(offenderRepository,times(0)).save(offenderEntityArgumentCaptor.capture());
    }

    @Test
    void shouldNotUpdateOffenderProbationStatusFields_givenProbationStatusHasNullValues() {

        var date = LocalDate.now();

        final var probationStatusDetail = ProbationStatusDetail.builder()
                .status(OffenderProbationStatus.CURRENT.getName())
                .inBreach(null)
                .awaitingPsr(null)
                .previouslyKnownTerminationDate(date)
                .preSentenceActivity(true)
                .build();
        final var offenderEntity = OffenderEntity.builder()
                .crn(CRN)
                .probationStatus(null)
                .breach(false)
                .awaitingPsr(false)
                .build();

        when(userAgnosticOffenderRestClient.getProbationStatusByCrn(CRN)).thenReturn(Mono.just(probationStatusDetail));
        when(offenderRepository.findByCrn(CRN)).thenReturn(Optional.ofNullable(offenderEntity));
        when(offenderRepository.save(any(OffenderEntity.class))).thenReturn(offenderEntity);

        service.updateOffenderProbationStatus(CRN);

        verify(userAgnosticOffenderRestClient).getProbationStatusByCrn(CRN);
        verify(offenderRepository).findByCrn(CRN);
        verify(offenderRepository).save(offenderEntityArgumentCaptor.capture());
        var entityToUpdate = offenderEntityArgumentCaptor.getValue();

        //no update due to null values
        assertThat(entityToUpdate.getAwaitingPsr()).isEqualTo(false);
        assertThat(entityToUpdate.isBreach()).isEqualTo(false);

        //new values from the probation status detail
        assertThat(entityToUpdate.getProbationStatus()).isEqualTo(OffenderProbationStatus.CURRENT);
        assertThat(entityToUpdate.getPreviouslyKnownTerminationDate()).isEqualTo(date);
        assertThat(entityToUpdate.getCrn()).isEqualTo(CRN);
        assertThat(entityToUpdate.isPreSentenceActivity()).isEqualTo(true);
    }

    @Test
    void shouldNotUpdateOffenderProbationStatus_whenNoChangeInDetails_AndEmitCorrectTelemetryEvent() {

        var date = LocalDate.now();

        final var probationStatusDetail = ProbationStatusDetail.builder()
                .status(OffenderProbationStatus.CURRENT.getName())
                .inBreach(true)
                .awaitingPsr(true)
                .previouslyKnownTerminationDate(date)
                .preSentenceActivity(true)
                .build();
        final var offenderEntity = OffenderEntity.builder()
                .crn(CRN)
                .probationStatus(OffenderProbationStatus.CURRENT)
                .breach(true)
                .awaitingPsr(true)
                .previouslyKnownTerminationDate(date)
                .preSentenceActivity(true)
                .build();

        when(userAgnosticOffenderRestClient.getProbationStatusByCrn(CRN)).thenReturn(Mono.just(probationStatusDetail));
        when(offenderRepository.findByCrn(CRN)).thenReturn(Optional.ofNullable(offenderEntity));

        service.updateOffenderProbationStatus(CRN);
        verify(telemetryService).trackOffenderProbationStatusNotUpdateEvent(offenderEntityArgumentCaptor.capture());
        verify(offenderRepository,times(0)).save(offenderEntityArgumentCaptor.capture());
        var entityToUpdate = offenderEntityArgumentCaptor.getValue();

        assertThat(entityToUpdate.getProbationStatus()).isEqualTo(OffenderProbationStatus.CURRENT);
        assertThat(entityToUpdate.getAwaitingPsr()).isEqualTo(true);
        assertThat(entityToUpdate.getPreviouslyKnownTerminationDate()).isEqualTo(date);
        assertThat(entityToUpdate.getCrn()).isEqualTo(CRN);
        assertThat(entityToUpdate.isPreSentenceActivity()).isEqualTo(true);
        assertThat(entityToUpdate.isBreach()).isEqualTo(true);
    }

    @Test
    void shouldNotUpdateOffenderProbationStatus_whenProbationStatusDetailsIsNull_AndEmitCorrectTelemetryEvent() {

        var date = LocalDate.now();


        final var offenderEntity = OffenderEntity.builder()
                .crn(CRN)
                .probationStatus(OffenderProbationStatus.CURRENT)
                .breach(true)
                .awaitingPsr(true)
                .previouslyKnownTerminationDate(date)
                .preSentenceActivity(true)
                .build();

        when(userAgnosticOffenderRestClient.getProbationStatusByCrn(CRN)).thenReturn(Mono.empty());
        when(offenderRepository.findByCrn(CRN)).thenReturn(Optional.ofNullable(offenderEntity));

        service.updateOffenderProbationStatus(CRN);
        verify(telemetryService).trackOffenderProbationStatusNotUpdateEvent(offenderEntityArgumentCaptor.capture());
        verify(offenderRepository,times(0)).save(offenderEntityArgumentCaptor.capture());
        var entityToUpdate = offenderEntityArgumentCaptor.getValue();

        assertThat(entityToUpdate.getProbationStatus()).isEqualTo(OffenderProbationStatus.CURRENT);
        assertThat(entityToUpdate.getAwaitingPsr()).isEqualTo(true);
        assertThat(entityToUpdate.getPreviouslyKnownTerminationDate()).isEqualTo(date);
        assertThat(entityToUpdate.getCrn()).isEqualTo(CRN);
        assertThat(entityToUpdate.isPreSentenceActivity()).isEqualTo(true);
        assertThat(entityToUpdate.isBreach()).isEqualTo(true);
    }
}
