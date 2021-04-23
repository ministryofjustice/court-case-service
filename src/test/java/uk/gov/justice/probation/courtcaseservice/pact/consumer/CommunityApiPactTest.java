package uk.gov.justice.probation.courtcaseservice.pact.consumer;

import java.io.IOException;
import java.util.Map;
import au.com.dius.pact.consumer.MockServer;
import au.com.dius.pact.consumer.dsl.PactDslJsonBody;
import au.com.dius.pact.consumer.dsl.PactDslWithProvider;
import au.com.dius.pact.consumer.junit5.PactConsumerTestExt;
import au.com.dius.pact.consumer.junit5.PactTestFor;
import au.com.dius.pact.core.model.RequestResponsePact;
import au.com.dius.pact.core.model.annotations.Pact;
import au.com.dius.pact.core.model.annotations.PactDirectory;
import org.apache.http.client.fluent.Request;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.http.MediaType;

import static org.assertj.core.api.Assertions.assertThat;

@ExtendWith(PactConsumerTestExt.class)
@PactTestFor(providerName = "community-api")
@PactDirectory(value = "build/pacts")
class CommunityApiPactTest {

    @Pact(provider="community-api", consumer="court-case-service")
    public RequestResponsePact getNsis(PactDslWithProvider builder) {

        PactDslJsonBody keyValueType = new PactDslJsonBody()
            .stringType("code", "description");

        PactDslJsonBody body = new PactDslJsonBody()
            .integerType("nsiId", 2500003903L)
            .date("actualStartDate","yyyy-MM-dd")
            .datetime("statusDateTime")
            .object("nsiType", keyValueType)
            .object("nsiSubType", keyValueType)
            .object("nsiStatus", keyValueType)
            ;

        return builder
            .given("an NSI exists for CRN X320741 and conviction id 2500018597")
            .uponReceiving("a request for a NSIs by CRN and conviction ID")
            .path("/secure/offenders/crn/X320741/convictions/2500018597/nsis")
            .query("nsiCodes=BRE")
            .method("GET")
            .willRespondWith()
            .headers(Map.of("Content-Type", MediaType.APPLICATION_JSON_VALUE))
            .body(body)
            .status(200)
            .toPact();
    }

    @PactTestFor(pactMethod = "getNsis")
    @Test
    void getNsis(MockServer mockServer) throws IOException {
        var httpResponse = Request
            .Get(mockServer.getUrl() + "/secure/offenders/crn/X320741/convictions/2500018597/nsis?nsiCodes=BRE")
            .execute()
            .returnResponse();

        assertThat(httpResponse.getStatusLine().getStatusCode()).isEqualTo(200);
    }
}
