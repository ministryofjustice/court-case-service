package uk.gov.justice.probation.courtcaseservice.restclient;

import java.time.LocalDate;
import java.time.Month;
import java.util.List;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.context.junit4.SpringRunner;
import org.springframework.web.reactive.function.client.WebClientResponseException;
import uk.gov.justice.probation.courtcaseservice.BaseIntTest;
import uk.gov.justice.probation.courtcaseservice.controller.model.ProbationStatus;
import uk.gov.justice.probation.courtcaseservice.restclient.exception.ConvictionNotFoundException;
import uk.gov.justice.probation.courtcaseservice.restclient.exception.OffenderNotFoundException;
import uk.gov.justice.probation.courtcaseservice.service.model.PssRequirement;
import uk.gov.justice.probation.courtcaseservice.service.model.Requirement;

import static org.assertj.core.api.Assertions.assertThat;
import static uk.gov.justice.probation.courtcaseservice.restclient.communityapi.mapper.RequirementMapperTest.EXPECTED_RQMNT_1;

@RunWith(SpringRunner.class)
public class OffenderRestClientIntTest extends BaseIntTest {

    private static final String CRN = "X320741";
    private static final String UNKNOWN_CRN = "CRNXXX";
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

        assertThat(offender.getOffenderManagers()).hasSize(1);
        var offenderManager = offender.getOffenderManagers().get(0);
        assertThat(offenderManager.getStaff().getForenames()).isEqualTo("Temperance");
        assertThat(offenderManager.getStaff().getSurname()).isEqualTo("Brennan");
        assertThat(offenderManager.getProvider()).isEqualTo("NPS North East");
        assertThat(offenderManager.getTeam().getDescription()).isEqualTo("OMIC OMU A");
        assertThat(offenderManager.getTeam().getTelephone()).isEqualTo("0151 222 3333");
        assertThat(offenderManager.getTeam().getLocalDeliveryUnit()).isEqualTo("LDU Description");
        assertThat(offenderManager.getTeam().getDistrict()).isEqualTo("OMiC POM Responsibility");
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
        offenderRestClient.getConvictionsByCrn(UNKNOWN_CRN).block();
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
    public void givenServiceThrowsError_whenGetConvictionRequirementsCalled_thenReturnEmptyList() {
        // This endpoint is used as a composite so we will return an empty list for a 500 error
        var optionalRequirements = offenderRestClient.getConvictionRequirements(CRN, "99999").block();

        assertThat(optionalRequirements).isEmpty();
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

    @Test
    public void whenGetOffenderMatchDetail_thenMakeRestCallToCommunityApi() {
        var optionalOffenderMatchDetail = offenderRestClient.getOffenderMatchDetailByCrn(CRN).blockOptional();
        assertThat(optionalOffenderMatchDetail).isNotEmpty();

        var offenderMatchDetail = optionalOffenderMatchDetail.get();
        assertThat(offenderMatchDetail.getMiddleNames()).containsExactlyInAnyOrder("Felix", "Hope");
    }

    @Test
    public void givenOffenderDoesNotExist_whenGetOffenderMatchDetail_thenReturnNull() {
        var optionalOffenderMatchDetail = offenderRestClient.getOffenderMatchDetailByCrn(UNKNOWN_CRN).blockOptional();
        assertThat(optionalOffenderMatchDetail.get().getForename()).isNull();
    }

    @Test(expected = WebClientResponseException.class)
    public void givenServiceThrowsError_whenGetOffenderMatchDetail_thenFailFastAndThrowException() {
        offenderRestClient.getOffenderMatchDetailByCrn(SERVER_ERROR_CRN).block();
    }

    @Test
    public void whenGetOffenderDetailByCrnCalled_thenMakeRestCallToCommunityApi() {
        var optionalOffender = offenderRestClient.getOffenderDetailByCrn(CRN).blockOptional();

        assertThat(optionalOffender).isNotEmpty();
        var offenderDetail = optionalOffender.get();
        assertThat(offenderDetail.getTitle()).isEqualTo("Mr.");
        assertThat(offenderDetail.getProbationStatus()).isSameAs(ProbationStatus.CURRENT);
        assertThat(offenderDetail.getDateOfBirth()).isEqualTo(LocalDate.of(2000, Month.JULY, 19));
        assertThat(offenderDetail.getForename()).isEqualTo("Aadland");
        assertThat(offenderDetail.getSurname()).isEqualTo("Bertrand");
        assertThat(offenderDetail.getOtherIds().getCrn()).isEqualTo("X320741");
        assertThat(offenderDetail.getMiddleNames()).containsExactlyInAnyOrder("Hope", "Felix");
    }

    @Test(expected = OffenderNotFoundException.class)
    public void givenOffenderDetailDoesNotExist_whenGetOffenderByCrnCalled_thenExpectException() {
        offenderRestClient.getOffenderDetailByCrn(UNKNOWN_CRN).blockOptional();
    }

    @Test(expected = WebClientResponseException.class)
    public void givenServiceThrowsError_whenGetOffenderDetailByCrnCalled_thenFailFastAndThrowException() {
        offenderRestClient.getOffenderDetailByCrn(SERVER_ERROR_CRN).block();
    }

    @Test
    public void whenGetConvictionPssRequirementsCalled_thenReturn() {
        var optionalRequirements = offenderRestClient.getConvictionPssRequirements(CRN, CONVICTION_ID).blockOptional();
        assertThat(optionalRequirements).isNotEmpty();

        final List<PssRequirement> pssRqmnts = optionalRequirements.get();
        assertThat(pssRqmnts).hasSize(4);
        assertThat(pssRqmnts).extracting("description")
            .contains("Specified Activity", "Travel Restriction", "Inactive description", "UK Travel Restriction");
    }

    @Test
    public void givenServiceThrowsError_whenGetConvictionPssRequirementsCalled_thenReturnEmptyList() {
        // This endpoint is used as a composite so we will return an empty list for a 500 error
        var optionalRequirements = offenderRestClient.getConvictionPssRequirements(CRN, "99999").block();

        assertThat(optionalRequirements).isEmpty();
    }

    @Test
    public void whenGetLicenceConditionsCalled_thenReturn() {
        var optionalRequirements = offenderRestClient.getConvictionLicenceConditions(CRN, CONVICTION_ID).blockOptional();
        assertThat(optionalRequirements).isNotEmpty();

        var licenceConditions = optionalRequirements.get();
        assertThat(licenceConditions).hasSize(3);
        assertThat(licenceConditions).extracting("description")
            .contains("Alcohol", "Curfew Arrangement", "Participate or co-op with Programme or Activities");
    }

    @Test
    public void givenServiceThrowsError_whenGetLicenceConditionsCalled_thenReturnEmptyList() {
        var optionalRequirements = offenderRestClient.getConvictionLicenceConditions(CRN, "99999").blockOptional();

        assertThat(optionalRequirements).isNotEmpty();
        assertThat(optionalRequirements.get()).isEmpty();
    }

    @Test
    public void whenGetRegistrationsCalled_thenReturn() {
        var optionalRegistrations = offenderRestClient.getOffenderRegistrations(CRN).blockOptional();
        assertThat(optionalRegistrations).isNotEmpty();

        var registrations = optionalRegistrations.get();
        assertThat(registrations).hasSize(4);
        assertThat(registrations).extracting("type")
            .contains("Suicide/Self Harm", "Domestic Abuse Perpetrator", "Medium RoSH", "Risk to Staff");
    }

    @Test(expected = OffenderNotFoundException.class)
    public void  givenOffenderDoesNotExist_whenGetRegistrationsCalled_thenExpectException() {
        offenderRestClient.getOffenderRegistrations(UNKNOWN_CRN).blockOptional();
    }

    @Test
    public void whenGetCourtAppearancesCalled_thenReturn() {
        var optionalCourtAppearances = offenderRestClient.getOffenderCourtAppearances(CRN, 2500295343L).blockOptional();
        assertThat(optionalCourtAppearances).isNotEmpty();

        var appearances = optionalCourtAppearances.get();
        assertThat(appearances).hasSize(3);
        assertThat(appearances).extracting("courtName")
            .contains("Aberdare Magistrates Court", "Aberdare Magistrates Court", "Bicester Magistrates Court");
    }

    @Test(expected = OffenderNotFoundException.class)
    public void  givenOffenderDoesNotExist_whenGetCourtAppearancesCalled_thenExpectException() {
        offenderRestClient.getOffenderCourtAppearances(UNKNOWN_CRN, Long.valueOf(CONVICTION_ID)).blockOptional();
    }
}
