package uk.gov.justice.probation.courtcaseservice.restclient.communityapi.mapper;

import org.junit.jupiter.api.Test;
import uk.gov.justice.probation.courtcaseservice.controller.model.BreachResponse;
import uk.gov.justice.probation.courtcaseservice.restclient.communityapi.model.*;

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
    private final NsiMapper mapper = new NsiMapper();

    @Test
    public void mapValues() {
        List<NsiManager> nsiManagers = Collections.singletonList(
                buildNsiManager(LocalDate.of(2020, 5, 1), TEAM));

        CommunityApiNsi communityApiNsi = buildNsi(nsiManagers);
        BreachResponse breach = mapper.breachOf(communityApiNsi);

        assertThat(breach.getBreachId()).isEqualTo(NSI_ID);
        assertThat(breach.getIncidentDate()).isEqualTo(INCIDENT_DATE);
        assertThat(breach.getStarted()).isEqualTo(STARTED_DATE);
        assertThat(breach.getOfficer()).isEqualTo(OFFICER);
        assertThat(breach.getProvider()).isEqualTo(PROVIDER);
        assertThat(breach.getTeam()).isEqualTo(TEAM);
        assertThat(breach.getStatus()).isEqualTo(BREACH_STATUS);
    }

    private CommunityApiNsi buildNsi(List<NsiManager> nsiManagers) {
        return CommunityApiNsi.builder()
                .nsiId(NSI_ID)
                .referralDate(INCIDENT_DATE)
                .actualStartDate(STARTED_DATE)
                .nsiManagers(nsiManagers)
                .nsiStatus(NsiStatus.builder()
                        .description(BREACH_STATUS)
                        .build())
                .build();
    }

    @Test
    public void givenMultipleNsiManagers_thenGetValuesFromMostRecent() {
        List<NsiManager> nsiManagers = Arrays.asList(
                buildNsiManager(LocalDate.of(2020, 5, 5), "Wrong Team"),
                buildNsiManager(LocalDate.of(2020, 5, 1), "Wrong Team"),
                buildNsiManager(LocalDate.of(2020, 5, 13), "Expected Team"),
                buildNsiManager(LocalDate.of(2020, 5, 8), "Wrong Team")
        );

        CommunityApiNsi communityApiNsi = buildNsi(nsiManagers);
        BreachResponse breach = mapper.breachOf(communityApiNsi);

        assertThat(breach.getTeam()).isEqualTo("Expected Team");
    }

    @Test
    public void givenZeroNsiManagers_thenReturnNullValues() {
        List<NsiManager> nsiManagers = Collections.emptyList();

        CommunityApiNsi communityApiNsi = buildNsi(nsiManagers);
        BreachResponse breach = mapper.breachOf(communityApiNsi);

        assertThat(breach.getOfficer()).isEqualTo(null);
        assertThat(breach.getProvider()).isEqualTo(null);
        assertThat(breach.getTeam()).isEqualTo(null);
    }

    private NsiManager buildNsiManager(LocalDate startDate, String team) {
        return NsiManager.builder()
                    .staff(StaffWrapper.builder()
                            .staff(Staff.builder()
                                    .forenames("Forename")
                                    .surname("Lastname")
                                    .build())
                            .build())
                    .probationArea(ProbationArea.builder()
                            .description(PROVIDER)
                            .build())
                    .team(Team.builder()
                            .description(team)
                            .build())
                    .startDate(startDate)
                    .build();
    }
}