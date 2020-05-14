package uk.gov.justice.probation.courtcaseservice.restclient;

import static com.github.tomakehurst.wiremock.core.WireMockConfiguration.wireMockConfig;
import static org.assertj.core.api.Assertions.assertThat;

import com.github.tomakehurst.wiremock.junit.WireMockRule;
import java.io.IOException;
import java.util.Optional;
import org.junit.Rule;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.core.io.Resource;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.junit4.SpringRunner;
import org.springframework.web.reactive.function.client.WebClientResponseException;
import uk.gov.justice.probation.courtcaseservice.restclient.exception.DocumentNotFoundException;
import uk.gov.justice.probation.courtcaseservice.restclient.exception.OffenderNotFoundException;
import uk.gov.justice.probation.courtcaseservice.service.model.document.GroupedDocuments;

@RunWith(SpringRunner.class)
@SpringBootTest
@ActiveProfiles("test")
public class DocumentRestClientIntTest {

    private static final String CRN = "X320741";
    private static final String SERVER_ERROR_CRN = "X320500";

    @Autowired
    private DocumentRestClient restClient;

    @Rule
    public WireMockRule wireMockRule = new WireMockRule(wireMockConfig()
            .port(8090)
            .usingFilesUnderClasspath("mocks"));

    @Test
    public void whenGetConvictionDocumentsCalled_thenMakeRestCallToCommunityApi() {
        Optional<GroupedDocuments> documentsResponse = restClient.getDocumentsByCrn(CRN).blockOptional();

        final GroupedDocuments groupedDocuments = documentsResponse.get();

        assertThat(groupedDocuments.getConvictions()).hasSize(2);
        assertThat(groupedDocuments.getDocuments()).hasSize(7);
    }

    @Test
    public void givenKnownCrnNoDocuments_whenGetConvictionDocumentsCalled_thenMakeRestCallToCommunityApi() {
        GroupedDocuments documentsResponse = restClient.getDocumentsByCrn("CRN800").blockOptional().get();

        assertThat(documentsResponse.getDocuments()).isEmpty();
        assertThat(documentsResponse.getConvictions()).isEmpty();
    }

    @Test(expected = OffenderNotFoundException.class)
    public void givenOffenderDoesNotExist_whenGetConvictionsDocumentsByCrnCalled_ThrowException() {
        restClient.getDocumentsByCrn("CRNXXX").block();
    }

    @Test
    public void whenGetDocument_thenReturnDocument() throws IOException {
        final ResponseEntity<Resource> responseEntity = restClient.getDocument(CRN, "abc-def").blockOptional().get();

        assertThat(responseEntity.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(responseEntity.getBody().contentLength()).isEqualTo(20992);
    }

    @Test(expected = DocumentNotFoundException.class)
    public void givenDocumentDoesNotExist_whenGetDocument_thenThrowException()  {
        restClient.getDocument(CRN, "xxx").blockOptional().get();
    }

    @Test(expected = WebClientResponseException.class)
    public void whenGetDocument_ServerError_thenFailFastAndThrowException() {
        final ResponseEntity<Resource> responseEntity = restClient.getDocument(SERVER_ERROR_CRN, "abc-def").blockOptional().get();

        assertThat(responseEntity.getStatusCode()).isEqualTo(HttpStatus.INTERNAL_SERVER_ERROR);
    }
}
