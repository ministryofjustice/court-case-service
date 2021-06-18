package uk.gov.justice.probation.courtcaseservice.restclient;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.reactive.function.client.WebClientResponseException;
import uk.gov.justice.probation.courtcaseservice.BaseIntTest;
import uk.gov.justice.probation.courtcaseservice.controller.model.ProbationStatus;
import uk.gov.justice.probation.courtcaseservice.restclient.exception.ConvictionNotFoundException;
import uk.gov.justice.probation.courtcaseservice.restclient.exception.ForbiddenException;
import uk.gov.justice.probation.courtcaseservice.restclient.exception.OffenderNotFoundException;

import java.time.LocalDate;
import java.time.Month;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static uk.gov.justice.probation.courtcaseservice.restclient.communityapi.mapper.RequirementMapperTest.EXPECTED_RQMNT_1;

class OffenderRestClientIntTest extends BaseIntTest {

    private static final String CRN = "X320741";
    private static final String UNKNOWN_CRN = "CRNXXX";
    private static final Long CONVICTION_ID = 2500297061L;
    public static final String SERVER_ERROR_CRN = "SE12345";

    @Autowired
    private OffenderRestClientFactory offenderRestClientFactory;
    private OffenderRestClient offenderRestClient;

    @BeforeEach
    void beforeEach() {
        offenderRestClient = offenderRestClientFactory.build();
    }

    @Nested
    class getOffenderManagers {

        @Test
        void whenGetOffenderManagersByCrnCalled_thenMakeRestCallToCommunityApi() {
            var maybeOffenderManagers = offenderRestClient.getOffenderManagers(CRN).blockOptional();

            assertThat(maybeOffenderManagers).isNotEmpty();
            var offenderManagers = maybeOffenderManagers.get();
            assertThat(offenderManagers).hasSize(1);
            var offenderManager = offenderManagers.get(0);
            assertThat(offenderManager.getProvider()).isEqualTo("Essex");
            assertThat(offenderManager.getStaff().getForenames()).isEqualTo("JIM");
            assertThat(offenderManager.getStaff().getSurname()).isEqualTo("SNOW");
            assertThat(offenderManager.getStaff().getEmail()).isEqualTo("jim.snow@justice.gov.uk");
            assertThat(offenderManager.getStaff().getTelephone()).isEqualTo("01512112121");
            assertThat(offenderManager.getAllocatedDate()).isEqualTo(LocalDate.of(2018, Month.MAY, 4));
            assertThat(offenderManager.getTeam().getDescription()).isEqualTo("Team desc");
            assertThat(offenderManager.getTeam().getDistrict()).isEqualTo("Team district desc");
            assertThat(offenderManager.getTeam().getTelephone()).isEqualTo("02033334444");
            assertThat(offenderManager.getTeam().getLocalDeliveryUnit()).isEqualTo("LDU desc");
        }

        @Test
        void givenOffenderDoesNotExist_whenGetOffenderManagersByCrnCalled_ReturnEmpty() {
            assertThrows(OffenderNotFoundException.class, () ->
                offenderRestClient.getOffenderManagers("CRNXXX").blockOptional()
            );
        }

        @Test
        void givenServiceThrowsError_whenGetOffenderByCrnCalled_thenFailFastAndThrowException() {
            assertThrows(WebClientResponseException.class, () ->
                offenderRestClient.getOffenderManagers(SERVER_ERROR_CRN).block()
            );
        }
    }

    @Nested
    class convictionsByCrn {

        @Test
        void whenGetConvictionsByCrnCalled_thenMakeRestCallToCommunityApi() {
            var optionalConvictions = offenderRestClient.getConvictionsByCrn(CRN).blockOptional();

            assertThat(optionalConvictions).isNotEmpty();

            assertThat(optionalConvictions.get()).hasSize(3);
        }

        @Test
        void givenOffenderDoesNotExist_whenGetConvictionsByCrnCalled_ReturnEmpty() {
            assertThrows(OffenderNotFoundException.class, () ->
                offenderRestClient.getConvictionsByCrn(UNKNOWN_CRN).block()
            );
        }

        @Test
        void givenServiceThrowsError_whenGetConvictionsByCrnCalled_thenFailFastAndThrowException() {
            assertThrows(WebClientResponseException.class, () ->
                offenderRestClient.getConvictionsByCrn(SERVER_ERROR_CRN).block()
            );
        }
    }


    @Nested
    class convictionRequirements {

        @Test
        void whenGetConvictionRequirementsCalled_thenMakeRestCallToCommunityApi() {
            var optionalRequirements = offenderRestClient.getConvictionRequirements(CRN, CONVICTION_ID).blockOptional();

            assertThat(optionalRequirements).isNotEmpty();

            var rqmnts = optionalRequirements.get();
            assertThat(rqmnts).hasSize(2);

            var rqmt1 = rqmnts.stream()
                .filter(requirement -> requirement.getRequirementId().equals(2500083652L))
                .findFirst().orElse(null);

            assertThat(EXPECTED_RQMNT_1).usingRecursiveComparison().isEqualTo(rqmt1);
        }

        @Test
        void givenKnownCrnUnknownConvictionId_whenGetConvictionRequirementsCalled_thenReturnEmptyRequirements() {
            var optionalRequirements = offenderRestClient.getConvictionRequirements(CRN, 2500297999L).blockOptional();

            var reqs = optionalRequirements.get();

            assertThat(reqs).isEmpty();
        }

        @Test
        void givenServiceThrowsError_whenGetConvictionRequirementsCalled_thenReturnEmptyList() {
            // This endpoint is used as a composite so we will return an empty list for a 500 error
            var optionalRequirements = offenderRestClient.getConvictionRequirements(CRN, 99999L).block();

            assertThat(optionalRequirements).isEmpty();
        }
    }


    @Nested
    class getBreaches {

        @Test
        void whenGetBreaches_thenMakeRestCallToCommunityApi() {
            var optionalBreaches = offenderRestClient.getBreaches(CRN, CONVICTION_ID).blockOptional();
            assertThat(optionalBreaches).isNotEmpty();

            var breaches = optionalBreaches.get();
            assertThat(breaches.size()).isEqualTo(2);

            var breach = breaches.get(0);
            assertThat(breach.getStatus()).isEqualTo("Breach Initiated");
            assertThat(breach.getBreachId()).isEqualTo(11131321L);
            assertThat(breach.getDescription()).isEqualTo("Community Order");
            assertThat(breach.getStatusDate()).isEqualTo(LocalDate.of(2019, Month.DECEMBER, 18));
            assertThat(breach.getStarted()).isEqualTo(LocalDate.of(2019, Month.OCTOBER, 20));
        }

        @Test
        void whenGetBreaches_thenMakeRestCallToCommunityApi_404NoCRN() {
            assertThrows(ConvictionNotFoundException.class, () ->
                offenderRestClient.getBreaches("xxx", CONVICTION_ID).block()
            );
        }

        @Test
        void whenGetBreaches_thenMakeRestCallToCommunityApi_404NoConvictionId() {
            assertThrows(ConvictionNotFoundException.class, () ->
                offenderRestClient.getBreaches(CRN, 123L).block()
            );
        }

        @Test
        void whenGetBreaches_thenMakeRestCallToCommunityApi_500ServerError() {
            assertThrows(WebClientResponseException.class, () ->
                offenderRestClient.getBreaches(SERVER_ERROR_CRN, CONVICTION_ID).block()
            );
        }
    }

    @Test
    void whenGetOffenderMatchDetail_thenMakeRestCallToCommunityApi() {
        var optionalOffenderMatchDetail = offenderRestClient.getOffenderMatchDetailByCrn(CRN).blockOptional();
        assertThat(optionalOffenderMatchDetail).isNotEmpty();

        var offenderMatchDetail = optionalOffenderMatchDetail.get();
        assertThat(offenderMatchDetail.getMiddleNames()).containsExactlyInAnyOrder("Felix", "Hope");
    }

    @Test
    void givenOffenderDoesNotExist_whenGetOffenderMatchDetail_thenReturnNull() {
        var optionalOffenderMatchDetail = offenderRestClient.getOffenderMatchDetailByCrn(UNKNOWN_CRN).blockOptional();
        assertThat(optionalOffenderMatchDetail.get().getForename()).isNull();
    }

    @Test
    void givenServiceThrowsError_whenGetOffenderMatchDetail_thenFailFastAndThrowException() {
        assertThrows(WebClientResponseException.class, () ->
            offenderRestClient.getOffenderMatchDetailByCrn(SERVER_ERROR_CRN).block()
        );
    }

    @Test
    void whenGetOffenderDetailByCrnCalled_thenMakeRestCallToCommunityApi() {
        var optionalOffender = offenderRestClient.getOffender(CRN).blockOptional();

        assertThat(optionalOffender).isNotEmpty();
        var offenderDetail = optionalOffender.get();
        assertThat(offenderDetail.getTitle()).isEqualTo("Mr.");
        assertThat(offenderDetail.getDateOfBirth()).isEqualTo(LocalDate.of(2000, Month.JULY, 19));
        assertThat(offenderDetail.getFirstName()).isEqualTo("Aadland");
        assertThat(offenderDetail.getSurname()).isEqualTo("Bertrand");
        assertThat(offenderDetail.getOffenderId()).isEqualTo(2500343964L);
        assertThat(offenderDetail.getOtherIds().getCrn()).isEqualTo("X320741");
        assertThat(offenderDetail.getMiddleNames()).containsExactlyInAnyOrder("Hope", "Felix");
    }

    @Test
    void givenOffenderDetailDoesNotExist_whenGetOffenderByCrnCalled_thenExpectException() {
        assertThrows(OffenderNotFoundException.class, () ->
            offenderRestClient.getOffender(UNKNOWN_CRN).blockOptional()
        );
    }

    @Test
    void givenServiceThrowsError_whenGetOffenderDetailByCrnCalled_thenFailFastAndThrowException() {
        assertThrows(WebClientResponseException.class, () ->
            offenderRestClient.getOffender(SERVER_ERROR_CRN).block()
        );
    }

    @Test
    void whenGetConvictionPssRequirementsCalled_thenReturn() {
        var optionalRequirements = offenderRestClient.getConvictionPssRequirements(CRN, CONVICTION_ID).blockOptional();
        assertThat(optionalRequirements).isNotEmpty();

        var pssRqmnts = optionalRequirements.get();
        assertThat(pssRqmnts).hasSize(4);
        assertThat(pssRqmnts).extracting("description")
            .contains("Specified Activity", "Travel Restriction", "Inactive description", "UK Travel Restriction");
    }

    @Test
    void givenServiceThrowsError_whenGetConvictionPssRequirementsCalled_thenReturnEmptyList() {
        // This endpoint is used as a composite so we will return an empty list for a 500 error
        var optionalRequirements = offenderRestClient.getConvictionPssRequirements(CRN, 99999L).block();

        assertThat(optionalRequirements).isEmpty();
    }

    @Test
    void whenGetLicenceConditionsCalled_thenReturn() {
        var optionalRequirements = offenderRestClient.getConvictionLicenceConditions(CRN, CONVICTION_ID).blockOptional();
        assertThat(optionalRequirements).isNotEmpty();

        var licenceConditions = optionalRequirements.get();
        assertThat(licenceConditions).hasSize(3);
        assertThat(licenceConditions).extracting("description")
            .contains("Alcohol", "Curfew Arrangement", "Participate or co-op with Programme or Activities");
    }

    @Test
    void givenServiceThrowsError_whenGetLicenceConditionsCalled_thenReturnEmptyList() {
        var optionalRequirements = offenderRestClient.getConvictionLicenceConditions(CRN, 99999L).blockOptional();

        assertThat(optionalRequirements).isNotEmpty();
        assertThat(optionalRequirements.get()).isEmpty();
    }

    @Test
    void whenGetRegistrationsCalled_thenReturn() {
        var optionalRegistrations = offenderRestClient.getOffenderRegistrations(CRN).blockOptional();
        assertThat(optionalRegistrations).isNotEmpty();

        var registrations = optionalRegistrations.get();
        assertThat(registrations).hasSize(4);
        assertThat(registrations).extracting("type")
            .contains("Suicide/Self Harm", "Domestic Abuse Perpetrator", "Medium RoSH", "Risk to Staff");
    }

    @Test
    void  givenOffenderDoesNotExist_whenGetRegistrationsCalled_thenExpectException() {
        assertThrows(OffenderNotFoundException.class, () ->
            offenderRestClient.getOffenderRegistrations(UNKNOWN_CRN).blockOptional()
        );
    }

    @Test
    void whenGetCourtAppearancesCalled_thenReturn() {
        var optionalCourtAppearances = offenderRestClient.getOffenderCourtAppearances(CRN, 2500295343L).blockOptional();
        assertThat(optionalCourtAppearances).isNotEmpty();

        var appearances = optionalCourtAppearances.get();
        assertThat(appearances).hasSize(3);
        assertThat(appearances).extracting("courtName")
            .contains("Aberdare Magistrates Court", "Aberdare Magistrates Court", "Bicester Magistrates Court");
    }

    @Test
    void  givenOffenderDoesNotExist_whenGetCourtAppearancesCalled_thenExpectException() {
        assertThrows(OffenderNotFoundException.class, () ->
            offenderRestClient.getOffenderCourtAppearances(UNKNOWN_CRN, CONVICTION_ID).blockOptional()
        );
    }

    @Test
    void whenGetProbationStatus_thenReturn() {
        var optionalProbationStatusDetail = offenderRestClient.getProbationStatusByCrn(CRN).blockOptional();
        assertThat(optionalProbationStatusDetail).isNotEmpty();

        var probationStatusDetail = optionalProbationStatusDetail.get();
        assertThat(probationStatusDetail.getInBreach()).isTrue();
        assertThat(probationStatusDetail.isPreSentenceActivity()).isTrue();
        assertThat(probationStatusDetail.getStatus()).isEqualTo(ProbationStatus.PREVIOUSLY_KNOWN.name());
        assertThat(probationStatusDetail.getPreviouslyKnownTerminationDate()).isEqualTo(LocalDate.of(2010, Month.APRIL, 5));
    }

    @Test
    void givenUnknownCrn_whenGetProbationStatus_thenExpectException() {
        assertThrows(OffenderNotFoundException.class, () ->
            offenderRestClient.getProbationStatusByCrn(UNKNOWN_CRN).blockOptional()
        );
    }

    @Test
    void givenForbiddenError_whenGetProbationStatus_thenReturn() {
        assertThrows(ForbiddenException.class, () ->
            offenderRestClient.getProbationStatusByCrn("CRN403").blockOptional()
        );
    }

    @Test
    void givenServerError_whenGetProbationStatus_thenReturn() {
        assertThrows(WebClientResponseException.class, () ->
            offenderRestClient.getProbationStatusByCrn("SE12345").blockOptional()
        );
    }
}
