package uk.gov.justice.probation.courtcaseservice.restclient;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.context.junit4.SpringRunner;
import org.springframework.web.reactive.function.client.WebClientResponseException;
import uk.gov.justice.probation.courtcaseservice.BaseIntTest;
import uk.gov.justice.probation.courtcaseservice.restclient.exception.ConvictionNotFoundException;
import uk.gov.justice.probation.courtcaseservice.restclient.exception.OffenderNotFoundException;
import uk.gov.justice.probation.courtcaseservice.service.model.Requirement;

import java.time.LocalDate;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static uk.gov.justice.probation.courtcaseservice.restclient.communityapi.mapper.OffenderMapperTest.EXPECTED_RQMNT_1;

@RunWith(SpringRunner.class)
public class OffenderRestClientIntTest extends BaseIntTest {

    private static final String CRN = "X320741";
    private static final String CONVICTION_ID = "2500297061";
    public static final String SERVER_ERROR_CRN = "X320742";

    @Autowired
    private OffenderRestClient offenderRestClient;

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
    public void whenGetBreaches_thenMakeRestCallToCommunityApi() {
        var optionalBreaches = offenderRestClient.getBreaches(CRN, CONVICTION_ID).blockOptional();
        assertThat(optionalBreaches).isNotEmpty();

        var breaches = optionalBreaches.get();
        assertThat(breaches.size()).isEqualTo(1);

        var breach = breaches.get(0);
        assertThat(breach.getStatus()).isEqualTo("Breach Initiated");
        assertThat(breach.getDescription()).isEqualTo("Community Order");
    }

    @Test(expected = ConvictionNotFoundException.class)
    public void whenGetBreaches_thenMakeRestCallToCommunityApi_404NoCRN() {
        offenderRestClient.getBreaches("xxx", CONVICTION_ID).block();
    }

    @Test(expected = ConvictionNotFoundException.class)
    public void whenGetBreaches_thenMakeRestCallToCommunityApi_404NoConvictionId() {
        offenderRestClient.getBreaches(CRN, "123").block();
    }

    @Test(expected = WebClientResponseException.class)
    public void whenGetBreaches_thenMakeRestCallToCommunityApi_500ServerError() {
        offenderRestClient.getBreaches(SERVER_ERROR_CRN, CONVICTION_ID).block();
    }
}
