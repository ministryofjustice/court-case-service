package uk.gov.justice.probation.courtcaseservice.restclient;

import static com.github.tomakehurst.wiremock.core.WireMockConfiguration.wireMockConfig;
import static org.assertj.core.api.Assertions.assertThat;
import static uk.gov.justice.probation.courtcaseservice.restclient.communityapi.mapper.OffenderMapperTest.EXPECTED_RQMNT_1;

import com.github.tomakehurst.wiremock.junit.WireMockRule;
import java.time.LocalDate;
import java.util.List;
import java.util.Optional;
import org.junit.Rule;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.junit4.SpringRunner;
import org.springframework.web.reactive.function.client.WebClientResponseException;
import uk.gov.justice.probation.courtcaseservice.restclient.exception.OffenderNotFoundException;
import uk.gov.justice.probation.courtcaseservice.service.model.Requirement;
import uk.gov.justice.probation.courtcaseservice.service.model.document.GroupedDocuments;

@RunWith(SpringRunner.class)
@SpringBootTest
@ActiveProfiles("test")
public class OffenderRestClientIntTest {

    private static final String CRN = "X320741";
    private static final String CONVICTION_ID = "2500297061";
    public static final String SERVER_ERROR_CRN = "X320742";

    @Autowired
    private OffenderRestClient offenderRestClient;

    @Rule
    public WireMockRule wireMockRule = new WireMockRule(wireMockConfig()
            .port(8090)
            .usingFilesUnderClasspath("mocks"));

    @Test
    public void whenGetOffenderByCrnCalled_thenMakeRestCallToCommunityApi() {
        var optionalOffender = offenderRestClient.getProbationRecordByCrn(CRN).blockOptional();

        assertThat(optionalOffender).isNotEmpty();
        var offender = optionalOffender.get();
        assertThat(offender.getCrn()).isEqualTo(CRN);

        assertThat(offender.getOffenderManagers().get(0).getForenames()).isEqualTo("Temperance");
        assertThat(offender.getOffenderManagers().get(0).getSurname()).isEqualTo("Brennan");
        assertThat(offender.getOffenderManagers().get(0).getAllocatedDate()).isEqualTo(LocalDate.of(2019,9,30));
    }

    @Test(expected = OffenderNotFoundException.class)
    public void givenOffenderDoesNotExist_whenGetOffenderByCrnCalled_ReturnEmpty() {
        offenderRestClient.getProbationRecordByCrn("CRNXXX").blockOptional();
    }

    @Test(expected = WebClientResponseException.class)
    public void givenServiceThrowsError_whenGetOffenderByCrnCalled_thenFailFastAndThrowException() {
        offenderRestClient.getProbationRecordByCrn(SERVER_ERROR_CRN).block();
    }

    @Test
    public void whenGetConvictionsByCrnCalled_thenMakeRestCallToCommunityApi() {
        var optionalConvictions = offenderRestClient.getConvictionsByCrn(CRN).blockOptional();

        assertThat(optionalConvictions).isNotEmpty();

        assertThat(optionalConvictions.get()).hasSize(3);
    }

    @Test(expected = OffenderNotFoundException.class)
    public void givenOffenderDoesNotExist_whenGetConvictionsByCrnCalled_ReturnEmpty() {
        offenderRestClient.getConvictionsByCrn("CRNXXX").block();
    }

    @Test(expected = WebClientResponseException.class)
    public void givenServiceThrowsError_whenGetConvictionsByCrnCalled_thenFailFastAndThrowException() {
        offenderRestClient.getConvictionsByCrn(SERVER_ERROR_CRN).block();
    }

    @Test
    public void whenGetConvictionRequirementsCalled_thenMakeRestCallToCommunityApi() {
       var optionalRequirements = offenderRestClient.getConvictionRequirements(CRN, CONVICTION_ID).blockOptional();

        assertThat(optionalRequirements).isNotEmpty();

        final List<Requirement> rqmnts = optionalRequirements.get();
        assertThat(rqmnts).hasSize(2);

        final Requirement rqmt1 = rqmnts.stream()
            .filter(requirement -> requirement.getRequirementId().equals(2500083652L))
            .findFirst().orElse(null);

        assertThat(EXPECTED_RQMNT_1).isEqualToComparingFieldByField(rqmt1);
    }

    @Test
    public void givenKnownCrnUnknownConvictionId_whenGetConvictionRequirementsCalled_thenReturnEmptyRequirements() {
        var optionalRequirements = offenderRestClient.getConvictionRequirements(CRN, "2500297999").blockOptional();

        final List<Requirement> reqs = optionalRequirements.get();

        assertThat(reqs).isEmpty();
    }

    @Test
    public void whenGetConvictionDocumentsCalled_thenMakeRestCallToCommunityApi() {
        Optional<GroupedDocuments> documentsResponse = offenderRestClient.getDocumentsByCrn(CRN).blockOptional();

        final GroupedDocuments groupedDocuments = documentsResponse.get();

        assertThat(groupedDocuments.getConvictions()).hasSize(2);
        assertThat(groupedDocuments.getDocuments()).hasSize(7);
    }

    @Test
    public void givenKnownCrnNoDocuments_whenGetConvictionDocumentsCalled_thenMakeRestCallToCommunityApi() {
        GroupedDocuments documentsResponse = offenderRestClient.getDocumentsByCrn("CRN800").blockOptional().get();

        assertThat(documentsResponse.getDocuments()).isEmpty();
        assertThat(documentsResponse.getConvictions()).isEmpty();
    }

    @Test(expected = OffenderNotFoundException.class)
    public void givenOffenderDoesNotExist_whenGetConvictionsDocumentsByCrnCalled_ThrowException() {
        offenderRestClient.getDocumentsByCrn("CRNXXX").block();
    }

    @Test
    public void whenGetBreaches_thenMakeRestCallToCommunityApi() {
        var optionalBreaches = offenderRestClient.getBreaches(CRN, CONVICTION_ID).blockOptional();
        assertThat(optionalBreaches).isNotEmpty();

        var breaches = optionalBreaches.get();
        assertThat(breaches.size()).isEqualTo(1);

        var breach = breaches.get(0);
        assertThat(breach.getStatus()).isEqualTo("Breach Initiated");
        assertThat(breach.getDescription()).isEqualTo("Community Order");
    }}
