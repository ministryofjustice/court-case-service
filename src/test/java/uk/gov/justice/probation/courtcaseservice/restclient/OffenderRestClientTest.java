package uk.gov.justice.probation.courtcaseservice.restclient;

import com.google.common.io.Resources;
import okhttp3.mockwebserver.MockResponse;
import okhttp3.mockwebserver.MockWebServer;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.springframework.web.reactive.function.client.WebClient;
import uk.gov.justice.probation.courtcaseservice.restclient.communityapi.mapper.OffenderMapper;
import uk.gov.justice.probation.courtcaseservice.service.model.Offender;

import java.io.IOException;
import java.nio.charset.Charset;
import java.time.LocalDate;

import static org.assertj.core.api.Assertions.assertThat;

public class OffenderRestClientTest {

    private static final String CRN = "X320741";
    private static MockWebServer mockBackEnd;
    private OffenderRestClient offenderRestClient;

    @Before
    public void setUp() throws IOException {
        mockBackEnd = new MockWebServer();
        mockBackEnd.start();
        String baseUrl = String.format("http://localhost:%s", mockBackEnd.getPort());
        offenderRestClient = new OffenderRestClient(baseUrl + "test/%s/url", new OffenderMapper(), WebClient.builder().build());
    }

    @After
    public void tearDown() throws IOException {
        mockBackEnd.shutdown();
    }

    @Test
    public void whenGetOffenderByCrnCalled_thenMakeRestCallToCommunityApi() throws IOException {
        MockResponse mockResponse = new MockResponse()
                .setBody(getFileAsString("mocks/__files/GET_offender_all_X320741.json"))
                .addHeader("Content-Type", "application/json");
        mockBackEnd.enqueue(mockResponse);

        Offender offender = offenderRestClient.getOffenderByCrn(CRN);
        assertThat(offender.getCrn()).isEqualTo(CRN);

        assertThat(offender.getOffenderManager().getForenames()).isEqualTo("Temperance");
        assertThat(offender.getOffenderManager().getSurname()).isEqualTo("Brennan");
        assertThat(offender.getOffenderManager().getAllocatedDate()).isEqualTo(LocalDate.of(2019,9,30));

        // TODO: Test convictions values returned
    }


    @SuppressWarnings("UnstableApiUsage")
    private String getFileAsString(String resourcePath) throws IOException {
        return Resources.toString(Resources.getResource(resourcePath), Charset.defaultCharset());
    }
}