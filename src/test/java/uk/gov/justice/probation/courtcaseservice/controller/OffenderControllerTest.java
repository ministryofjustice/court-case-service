package uk.gov.justice.probation.courtcaseservice.controller;

import java.util.Optional;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.HttpEntity;
import org.springframework.http.ResponseEntity;
import reactor.core.publisher.Mono;
import uk.gov.justice.probation.courtcaseservice.controller.model.RequirementsResponse;
import uk.gov.justice.probation.courtcaseservice.service.DocumentService;
import uk.gov.justice.probation.courtcaseservice.service.OffenderService;
import uk.gov.justice.probation.courtcaseservice.service.model.ProbationRecord;
import uk.gov.justice.probation.courtcaseservice.service.model.Requirement;

import java.util.Collections;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoMoreInteractions;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
public class OffenderControllerTest {
    public static final String CRN = "CRN";
    public static final String CONVICTION_ID = "CONVICTION_ID";

    @Mock
    private DocumentService documentService;
    @Mock
    private OffenderService service;
    @Mock
    private ProbationRecord expectedProbationRecord;
    @Mock
    private Requirement expectedRequirement;

    @InjectMocks
    private OffenderController controller;

    @DisplayName("Ensures that the controller calls the service and returns the same offender probation record")
    @Test
    public void whenGetProbationRecord_thenReturnIt() {

        final boolean applyFilter = true;
        when(service.getProbationRecord(CRN, applyFilter)).thenReturn(expectedProbationRecord);

        ProbationRecord probationRecordResponse = controller.getProbationRecord(CRN, applyFilter);

        assertThat(probationRecordResponse).isNotNull();
        assertThat(probationRecordResponse).isEqualTo(expectedProbationRecord);
        verify(service).getProbationRecord(CRN, applyFilter);
        verifyNoMoreInteractions(service);
    }

    @DisplayName("Ensures that the controller calls the service and returns the same list of requirements")
    @Test
    public void whenGetRequirements_thenReturnIt() {

        when(service.getConvictionRequirements(CRN, CONVICTION_ID)).thenReturn(Collections.singletonList(expectedRequirement));

        RequirementsResponse requirementResponse = controller.getRequirements(CRN, CONVICTION_ID);

        assertThat(requirementResponse).isNotNull();
        assertThat(requirementResponse.getRequirements()).hasSize(1);
        assertThat(requirementResponse.getRequirements().get(0)).isEqualTo(expectedRequirement);
        verify(service).getConvictionRequirements(CRN, CONVICTION_ID);
    }

    @DisplayName("Ensures that the controller calls the document service and returns the same document")
    @Test
    public void whenGetDocument_thenReturnIt() {

        final ResponseEntity expectedResponse = mock(ResponseEntity.class);
        when(documentService.getDocument(CRN, CONVICTION_ID)).thenReturn(expectedResponse);

        HttpEntity responseEntity = controller.getOffenderDocumentByCrn(CRN, CONVICTION_ID);

        assertThat(responseEntity).isSameAs(expectedResponse);
        verify(documentService).getDocument(CRN, CONVICTION_ID);
    }

}
