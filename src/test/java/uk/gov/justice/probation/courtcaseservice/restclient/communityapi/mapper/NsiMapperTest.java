package uk.gov.justice.probation.courtcaseservice.restclient.communityapi.mapper;

import org.junit.jupiter.api.Test;
import uk.gov.justice.probation.courtcaseservice.controller.model.BreachResponse;
import uk.gov.justice.probation.courtcaseservice.restclient.communityapi.model.*;
import uk.gov.justice.probation.courtcaseservice.service.model.Conviction;
import uk.gov.justice.probation.courtcaseservice.service.model.Sentence;

import java.time.LocalDate;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

class NsiMapperTest {

    private static final long NSI_ID = 123456L;
    private static final LocalDate INCIDENT_DATE = LocalDate.of(2020, 5, 13);
    private static final LocalDate STARTED_DATE = LocalDate.of(2020, 5, 14);
    private static final String OFFICER = "Forename Lastname";
    private static final String PROVIDER = "Some provider";
    private static final String TEAM = "Some Team";
    private static final String BREACH_STATUS = "Breach Status";
    public static final String ORDER = "Some Order";
    private final NsiMapper mapper = new NsiMapper();

    @Test
    public void mapValues() {
        List<CommunityApiNsiManager> nsiManagers = Collections.singletonList(
                buildNsiManager(LocalDate.of(2020, 5, 1), TEAM));

        CommunityApiNsi communityApiNsi = buildNsi(nsiManagers);
        Conviction conviction = buildConviction();
        BreachResponse breach = mapper.breachOf(communityApiNsi, conviction);

        assertThat(breach.getBreachId()).isEqualTo(NSI_ID);
        assertThat(breach.getIncidentDate()).isEqualTo(INCIDENT_DATE);
        assertThat(breach.getStarted()).isEqualTo(STARTED_DATE);
        assertThat(breach.getOfficer()).isEqualTo(OFFICER);
        assertThat(breach.getProvider()).isEqualTo(PROVIDER);
        assertThat(breach.getTeam()).isEqualTo(TEAM);
        assertThat(breach.getStatus()).isEqualTo(BREACH_STATUS);
        assertThat(breach.getOrder()).isEqualTo(ORDER);
    }

    private Conviction buildConviction() {
        return Conviction.builder()
                .sentence(Sentence.builder()
                        .description(ORDER)
                        .build())
                .build();
    }

    @Test
    public void givenMultipleNsiManagers_thenGetValuesFromMostRecent() {
        List<CommunityApiNsiManager> nsiManagers = Arrays.asList(
                buildNsiManager(LocalDate.of(2020, 5, 5), "Wrong Team"),
                buildNsiManager(LocalDate.of(2020, 5, 1), "Wrong Team"),
                buildNsiManager(LocalDate.of(2020, 5, 13), "Expected Team"),
                buildNsiManager(LocalDate.of(2020, 5, 8), "Wrong Team")
        );

        CommunityApiNsi communityApiNsi = buildNsi(nsiManagers);
        Conviction conviction = buildConviction();
        BreachResponse breach = mapper.breachOf(communityApiNsi, conviction);

        assertThat(breach.getTeam()).isEqualTo("Expected Team");
    }

    @Test
    public void givenZeroNsiManagers_thenReturnNullValues() {
        List<CommunityApiNsiManager> nsiManagers = Collections.emptyList();

        CommunityApiNsi communityApiNsi = buildNsi(nsiManagers);
        Conviction conviction = buildConviction();
        BreachResponse breach = mapper.breachOf(communityApiNsi, conviction);

        assertThat(breach.getOfficer()).isEqualTo(null);
        assertThat(breach.getProvider()).isEqualTo(null);
        assertThat(breach.getTeam()).isEqualTo(null);
    }

    private CommunityApiNsi buildNsi(List<CommunityApiNsiManager> nsiManagers) {
        return CommunityApiNsi.builder()
                .nsiId(NSI_ID)
                .referralDate(INCIDENT_DATE)
                .actualStartDate(STARTED_DATE)
                .nsiManagers(nsiManagers)
                .status(CommunityApiNsiStatus.builder()
                        .description(BREACH_STATUS)
                        .build())
                .build();
    }

    private CommunityApiNsiManager buildNsiManager(LocalDate startDate, String team) {
        return CommunityApiNsiManager.builder()
                    .staff(CommunityApiStaffWrapper.builder()
                            .staff(CommunityApiStaff.builder()
                                    .forenames("Forename")
                                    .surname("Lastname")
                                    .build())
                            .build())
                    .probationArea(CommunityApiProbationArea.builder()
                            .description(PROVIDER)
                            .build())
                    .team(CommunityApiTeam.builder()
                            .description(team)
                            .build())
                    .startDate(startDate)
                    .build();
    }
}