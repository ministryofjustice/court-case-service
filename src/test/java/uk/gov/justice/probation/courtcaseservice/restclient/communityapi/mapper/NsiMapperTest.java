package uk.gov.justice.probation.courtcaseservice.restclient.communityapi.mapper;

import static java.util.Arrays.asList;
import static java.util.Collections.singletonList;
import static org.assertj.core.api.Assertions.assertThat;
import static uk.gov.justice.probation.courtcaseservice.service.model.document.DocumentType.NSI_DOCUMENT;
import static uk.gov.justice.probation.courtcaseservice.service.model.document.DocumentType.REFERRAL_DOCUMENT;

import java.time.LocalDate;
import java.util.Collections;
import java.util.List;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import uk.gov.justice.probation.courtcaseservice.controller.model.BreachResponse;
import uk.gov.justice.probation.courtcaseservice.restclient.communityapi.model.CommunityApiNsi;
import uk.gov.justice.probation.courtcaseservice.restclient.communityapi.model.CommunityApiNsiManager;
import uk.gov.justice.probation.courtcaseservice.restclient.communityapi.model.CommunityApiNsiStatus;
import uk.gov.justice.probation.courtcaseservice.restclient.communityapi.model.CommunityApiProbationArea;
import uk.gov.justice.probation.courtcaseservice.restclient.communityapi.model.CommunityApiStaff;
import uk.gov.justice.probation.courtcaseservice.restclient.communityapi.model.CommunityApiStaffWrapper;
import uk.gov.justice.probation.courtcaseservice.restclient.communityapi.model.CommunityApiTeam;
import uk.gov.justice.probation.courtcaseservice.service.model.Conviction;
import uk.gov.justice.probation.courtcaseservice.service.model.Sentence;
import uk.gov.justice.probation.courtcaseservice.service.model.document.ConvictionDocuments;
import uk.gov.justice.probation.courtcaseservice.service.model.document.DocumentType;
import uk.gov.justice.probation.courtcaseservice.service.model.document.GroupedDocuments;
import uk.gov.justice.probation.courtcaseservice.service.model.document.OffenderDocumentDetail;


class NsiMapperTest {

    private static final long NSI_ID = 123456L;
    private static final LocalDate INCIDENT_DATE = LocalDate.of(2020, 5, 13);
    private static final LocalDate STARTED_DATE = LocalDate.of(2020, 5, 14);
    private static final String OFFICER = "Forename Lastname";
    private static final String PROVIDER = "Some provider";
    private static final String CONVICTION_ID = "1234";
    private static final String TEAM = "Some Team";
    private static final String BREACH_STATUS = "Breach Status";
    public static final String ORDER = "Some Order";

    private final NsiMapper mapper = new NsiMapper();

    @DisplayName("Mapping of all values in NSI")
    @Test
    public void mapValues() {
        List<CommunityApiNsiManager> nsiManagers = singletonList(
                buildNsiManager(LocalDate.of(2020, 5, 1), TEAM));

        CommunityApiNsi communityApiNsi = buildNsi(nsiManagers);
        Conviction conviction = buildConviction();
        GroupedDocuments groupedDocuments = buildGroupedDocuments(CONVICTION_ID, NSI_ID);

        BreachResponse breach = mapper.breachOf(communityApiNsi, conviction, groupedDocuments);

        assertThat(breach.getBreachId()).isEqualTo(NSI_ID);
        assertThat(breach.getIncidentDate()).isEqualTo(INCIDENT_DATE);
        assertThat(breach.getStarted()).isEqualTo(STARTED_DATE);
        assertThat(breach.getOfficer()).isEqualTo(OFFICER);
        assertThat(breach.getProvider()).isEqualTo(PROVIDER);
        assertThat(breach.getTeam()).isEqualTo(TEAM);
        assertThat(breach.getStatus()).isEqualTo(BREACH_STATUS);
        assertThat(breach.getOrder()).isEqualTo(ORDER);
        assertThat(breach.getDocuments()).hasSize(1);
        assertThat(breach.getDocuments().get(0).getParentPrimaryKeyId()).isEqualTo(NSI_ID);
    }

    private Conviction buildConviction() {
        return Conviction.builder()
                .convictionId(CONVICTION_ID)
                .sentence(Sentence.builder()
                        .description(ORDER)
                        .build())
                .build();
    }

    @DisplayName("Selects the most recent team name from NSI managers")
    @Test
    public void givenMultipleNsiManagers_thenGetValuesFromMostRecent() {
        List<CommunityApiNsiManager> nsiManagers = asList(
                buildNsiManager(LocalDate.of(2020, 5, 5), "Wrong Team"),
                buildNsiManager(LocalDate.of(2020, 5, 1), "Wrong Team"),
                buildNsiManager(LocalDate.of(2020, 5, 13), "Expected Team"),
                buildNsiManager(LocalDate.of(2020, 5, 8), "Wrong Team")
        );

        CommunityApiNsi communityApiNsi = buildNsi(nsiManagers);
        Conviction conviction = buildConviction();
        BreachResponse breach = mapper.breachOf(communityApiNsi, conviction, buildGroupedDocuments(CONVICTION_ID, 123L));

        assertThat(breach.getTeam()).isEqualTo("Expected Team");
        assertThat(breach.getDocuments()).isEmpty();
    }

    @DisplayName("Handling null NSI managers")
    @Test
    public void givenNullNsiManagers_thenHandle() {

        CommunityApiNsi communityApiNsi = buildNsi(null);
        BreachResponse breach = mapper.breachOf(communityApiNsi, buildConviction(), buildGroupedDocuments(CONVICTION_ID, 123L));

        assertThat(breach.getTeam()).isNull();
        assertThat(breach.getDocuments()).isEmpty();
    }

    @DisplayName("Handling null grouped documents")
    @Test
    public void givenNullGroupedDocuments_thenHandle() {

        BreachResponse breach = mapper.breachOf(buildNsi(Collections.emptyList()), buildConviction(), null);

        assertThat(breach.getDocuments()).isEmpty();
    }

    @DisplayName("Handling case where there are no NSI managers")
    @Test
    public void givenZeroNsiManagers_thenReturnNullValues() {
        List<CommunityApiNsiManager> nsiManagers = Collections.emptyList();

        CommunityApiNsi communityApiNsi = buildNsi(nsiManagers);
        Conviction conviction = buildConviction();
        GroupedDocuments groupedDocuments = buildGroupedDocuments("NON_MATCH_CONVICTION_ID", NSI_ID);
        BreachResponse breach = mapper.breachOf(communityApiNsi, conviction, groupedDocuments);

        assertThat(breach.getOfficer()).isEqualTo(null);
        assertThat(breach.getProvider()).isEqualTo(null);
        assertThat(breach.getTeam()).isEqualTo(null);
        assertThat(breach.getDocuments()).isEmpty();
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

    private GroupedDocuments buildGroupedDocuments(String convictionId, Long nsiId) {

        OffenderDocumentDetail nsiDocument = buildDocument(nsiId, NSI_DOCUMENT);
        OffenderDocumentDetail referralReport = buildDocument(nsiId, REFERRAL_DOCUMENT);
        OffenderDocumentDetail nsiDocNonConviction = buildDocument(null, NSI_DOCUMENT);

        ConvictionDocuments convictionDocs1 = ConvictionDocuments.builder()
            .convictionId(convictionId)
            .documents(asList(nsiDocument, referralReport))
            .build();
        ConvictionDocuments convictionDocs2 = ConvictionDocuments.builder()
            .convictionId("NON_MATCH_CONVICTION_ID")
            .documents(singletonList(referralReport))
            .build();

        return GroupedDocuments.builder()
            .documents(singletonList(nsiDocNonConviction))
            .convictions(asList(convictionDocs1, convictionDocs2))
            .build();
    }

    private OffenderDocumentDetail buildDocument(Long parentPrimaryKeyId, DocumentType documentType) {
        return OffenderDocumentDetail.builder()
            .parentPrimaryKeyId(parentPrimaryKeyId)
            .type(documentType)
            .build();
    }
}
