package uk.gov.justice.probation.courtcaseservice.restclient.communityapi.mapper;

import java.io.File;
import java.io.IOException;
import java.time.LocalDate;
import java.time.Month;
import java.util.List;
import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import uk.gov.justice.probation.courtcaseservice.restclient.communityapi.model.CommunityApiLicenceCondition;
import uk.gov.justice.probation.courtcaseservice.restclient.communityapi.model.CommunityApiLicenceConditionsResponse;
import uk.gov.justice.probation.courtcaseservice.restclient.communityapi.model.CommunityApiPssRequirementResponse;
import uk.gov.justice.probation.courtcaseservice.restclient.communityapi.model.CommunityApiPssRequirementResponse.CommunityApiPssRequirementResponseBuilder;
import uk.gov.justice.probation.courtcaseservice.restclient.communityapi.model.CommunityApiPssRequirementsResponse;
import uk.gov.justice.probation.courtcaseservice.restclient.communityapi.model.CommunityApiRequirementsResponse;
import uk.gov.justice.probation.courtcaseservice.service.model.KeyValue;
import uk.gov.justice.probation.courtcaseservice.service.model.LicenceCondition;
import uk.gov.justice.probation.courtcaseservice.service.model.PssRequirement;
import uk.gov.justice.probation.courtcaseservice.service.model.Requirement;

import static org.assertj.core.api.Assertions.assertThat;

public class RequirementMapperTest {

    private static final ObjectMapper OBJECT_MAPPER = new ObjectMapper();
    private static final String DESC = "Supervisor visits";
    private static final String SUB_TYPE_DESC = "Adjourned - Pre-Sentence Report";

    private static final String BASE_MOCK_PATH = "src/test/resources/mocks/__files/";

    public static final Requirement EXPECTED_RQMNT_1 = Requirement.builder()
        .requirementId(2500083652L)
        .length(60L)
        .lengthUnit("Hours")
        .startDate(LocalDate.of(2017,6,1))
        .terminationDate(LocalDate.of(2017,12,1))
        .expectedStartDate(LocalDate.of(2017,6,1))
        .expectedEndDate(LocalDate.of(2017,12,1))
        .active(false)
        .requirementTypeSubCategory(new KeyValue("W01", "Regular"))
        .requirementTypeMainCategory(new KeyValue("W", "Unpaid Work"))
        .terminationReason(new KeyValue("74", "Hours Completed Outside 12 months (UPW only)"))
        .build();

    public static final Requirement EXPECTED_RQMNT_2 = Requirement.builder()
        .requirementId(2500007925L)
        .startDate(LocalDate.of(2015,7,1))
        .commencementDate(LocalDate.of(2015,6,29))
        .active(true)
        .adRequirementTypeMainCategory(new KeyValue("7", "Court - Accredited Programme"))
        .adRequirementTypeSubCategory(new KeyValue("P12", "ASRO"))
        .build();

    @BeforeAll
    public static void setUpBeforeClass() {
        OBJECT_MAPPER.configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false);
        OBJECT_MAPPER.registerModule(new JavaTimeModule());
    }

    @DisplayName("Tests mapping of Community API requirements to Court Case Service equivalent")
    @Test
    void shouldMapRequirementDetailsToRequirement() throws IOException {

        CommunityApiRequirementsResponse requirementsResponse
            = OBJECT_MAPPER.readValue(new File(BASE_MOCK_PATH + "offender-requirements/GET_requirements_X320741.json"),
            CommunityApiRequirementsResponse.class);
        List<Requirement> requirements = RequirementMapper.requirementsFrom(requirementsResponse);

        assertThat(requirements).hasSize(2);

        final Requirement rqmt1 = requirements.stream()
            .filter(requirement -> requirement.getRequirementId().equals(2500083652L))
            .findFirst().orElse(null);
        final Requirement rqmt2 = requirements.stream()
            .filter(requirement -> requirement.getRequirementId().equals(2500007925L))
            .findFirst().orElse(null);
        assertThat(EXPECTED_RQMNT_1).usingRecursiveComparison().isEqualTo(rqmt1);
        assertThat(EXPECTED_RQMNT_2).usingRecursiveComparison().isEqualTo(rqmt2);
    }

    @DisplayName("Tests empty requirements list.")
    @Test
    void shouldMapEmpty() throws IOException {
        CommunityApiRequirementsResponse emptyResponse = OBJECT_MAPPER
            .readValue(new File(BASE_MOCK_PATH + "offender-requirements/GET_requirements_X320741_empty.json"), CommunityApiRequirementsResponse.class);

        assertThat(emptyResponse.getRequirements()).isEmpty();
    }

    @DisplayName("Tests PSS requirements list and ensures that mapper is just mapping and not filtering.")
    @Test
    void shouldMapPssRequirements() {
        CommunityApiPssRequirementResponse pssRequirement1 = buildCommunityApiPssRequirementResponse(Boolean.FALSE, DESC, SUB_TYPE_DESC);
        CommunityApiPssRequirementResponse pssRequirement2 = buildCommunityApiPssRequirementResponse(Boolean.TRUE, DESC, SUB_TYPE_DESC);
        CommunityApiPssRequirementResponse pssRequirement3 = buildCommunityApiPssRequirementResponse(Boolean.TRUE, DESC, null);

        CommunityApiPssRequirementsResponse pssRequirementsResponse = CommunityApiPssRequirementsResponse.builder()
            .pssRequirements(List.of(pssRequirement1, pssRequirement2, pssRequirement3))
            .build();

        List<PssRequirement> pssRequirements = RequirementMapper.pssRequirementsFrom(pssRequirementsResponse);

        assertThat(pssRequirements).hasSize(3);

        PssRequirement pssRequirement = PssRequirement.builder()
            .description("Supervisor visits")
            .subTypeDescription("Adjourned - Pre-Sentence Report")
            .active(false)
            .build();
        assertThat(pssRequirements).contains(pssRequirement);
    }

    @DisplayName("Tests null PSS requirements list and ensures that empty list is returned.")
    @Test
    void givenNullInput_whenMapPssRequirement_thenReturnEmptyList() {

        List<PssRequirement> pssRequirements = RequirementMapper.pssRequirementsFrom(CommunityApiPssRequirementsResponse.builder().build());

        assertThat(pssRequirements).isEmpty();
    }

    @DisplayName("Tests licence conditions list and ensures that mapper is just mapping and not filtering.")
    @Test
    void shouldMapLicenceConditions() {
        LocalDate date1 = LocalDate.of(2020, Month.AUGUST, 28);
        LocalDate date2 = LocalDate.of(2020, Month.SEPTEMBER, 28);
        CommunityApiLicenceCondition licCondition1 = CommunityApiLicenceCondition.builder()
            .active(Boolean.FALSE)
            .licenceConditionTypeMainCat(KeyValue.builder().code("CODE1").description("desc1").build())
            .licenceConditionTypeSubCat(KeyValue.builder().code("CODE2").description("subtype description 1").build())
            .startDate(date1)
            .licenceConditionNotes("Some notes")
            .build();
        CommunityApiLicenceCondition licCondition2 = CommunityApiLicenceCondition.builder()
            .active(Boolean.TRUE)
            .licenceConditionTypeSubCat(KeyValue.builder().code("CODE3").description("subtype description 2").build())
            .startDate(date2)
            .licenceConditionNotes("Some more notes")
            .licenceConditionTypeMainCat(KeyValue.builder().code("CODE4").description("desc2").build())
            .build();

        CommunityApiLicenceConditionsResponse licenceConditionsResponse = CommunityApiLicenceConditionsResponse.builder()
            .licenceConditions(List.of(licCondition1, licCondition2))
            .build();

        List<LicenceCondition> licenceConditions = RequirementMapper.licenceConditionsFrom(licenceConditionsResponse);

        assertThat(licenceConditions).hasSize(2);

        assertThat(licenceConditions).extracting(LicenceCondition::getNotes)
            .contains("Some notes", "Some more notes");
        assertThat(licenceConditions).extracting(LicenceCondition::getDescription)
            .contains("desc1", "desc1");
        assertThat(licenceConditions).extracting(LicenceCondition::getSubTypeDescription)
            .contains("subtype description 2", "subtype description 1");
        assertThat(licenceConditions).extracting(LicenceCondition::getStartDate)
            .contains(date1, date2);
        assertThat(licenceConditions).extracting(LicenceCondition::isActive)
            .contains(true, false);
    }

    @DisplayName("Tests licence conditions list and ensures that mapper is just mapping and not filtering.")
    @Test
    void givenNullContent_whenMapLicenceCondition_shouldReturn() {
        CommunityApiLicenceCondition licCondition1 = CommunityApiLicenceCondition.builder()
            .active(Boolean.FALSE)
            .build();
        CommunityApiLicenceCondition licCondition2 = CommunityApiLicenceCondition.builder()
            .active(Boolean.TRUE)
            .build();

        CommunityApiLicenceConditionsResponse licenceConditionsResponse = CommunityApiLicenceConditionsResponse.builder()
            .licenceConditions(List.of(licCondition1, licCondition2))
            .build();

        List<LicenceCondition> licenceConditions = RequirementMapper.licenceConditionsFrom(licenceConditionsResponse);

        assertThat(licenceConditions).hasSize(2);

        assertThat(licenceConditions).extracting(LicenceCondition::isActive)
            .contains(true, false);
    }

    @DisplayName("Tests null licence conditions list and ensures that empty list is returned.")
    @Test
    void givenNullInput_whenMapLicenceConditions_thenReturnEmptyList() {

        List<LicenceCondition> licenceConditions = RequirementMapper.licenceConditionsFrom(
            CommunityApiLicenceConditionsResponse.builder().build());

        assertThat(licenceConditions).isEmpty();
    }

    public static CommunityApiPssRequirementResponse buildCommunityApiPssRequirementResponse(Boolean active, String description, String subTypeDescription) {
        CommunityApiPssRequirementResponseBuilder builder = CommunityApiPssRequirementResponse.builder()
            .active(active)
            .type(new KeyValue("CODE", description));
        if (subTypeDescription != null) {
            builder.subType(new KeyValue("CODE", subTypeDescription));
        }
        return builder.build();
    }
}
