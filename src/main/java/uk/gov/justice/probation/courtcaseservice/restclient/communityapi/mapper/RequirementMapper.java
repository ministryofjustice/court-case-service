package uk.gov.justice.probation.courtcaseservice.restclient.communityapi.mapper;

import java.util.Collections;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;
import uk.gov.justice.probation.courtcaseservice.restclient.communityapi.model.CommunityApiLicenceConditionsResponse;
import uk.gov.justice.probation.courtcaseservice.restclient.communityapi.model.CommunityApiPssRequirementResponse;
import uk.gov.justice.probation.courtcaseservice.restclient.communityapi.model.CommunityApiPssRequirementsResponse;
import uk.gov.justice.probation.courtcaseservice.restclient.communityapi.model.CommunityApiRequirementResponse;
import uk.gov.justice.probation.courtcaseservice.restclient.communityapi.model.CommunityApiRequirementsResponse;
import uk.gov.justice.probation.courtcaseservice.service.model.LicenceCondition;
import uk.gov.justice.probation.courtcaseservice.service.model.PssRequirement;
import uk.gov.justice.probation.courtcaseservice.service.model.Requirement;


public class RequirementMapper {

    public static List<Requirement> requirementsFrom(CommunityApiRequirementsResponse requirementsResponse) {
        return requirementsResponse.getRequirements().stream()
                .map(RequirementMapper::buildRequirement)
                .collect(Collectors.toList());
    }

    private static Requirement buildRequirement(CommunityApiRequirementResponse requirement) {
        return Requirement.builder()
                .requirementId(requirement.getRequirementId())
                .commencementDate(requirement.getCommencementDate())
                .startDate(requirement.getStartDate())
                .terminationDate(requirement.getTerminationDate())
                .expectedStartDate(requirement.getExpectedStartDate())
                .expectedEndDate(requirement.getExpectedEndDate())
                .active(requirement.getActive() != null && requirement.getActive())
                .length(requirement.getLength())
                .lengthUnit(requirement.getLengthUnit())
                .adRequirementTypeMainCategory(requirement.getAdRequirementTypeMainCategory())
                .adRequirementTypeSubCategory(requirement.getAdRequirementTypeSubCategory())
                .requirementTypeMainCategory(requirement.getRequirementTypeMainCategory())
                .requirementTypeSubCategory(requirement.getRequirementTypeSubCategory())
                .terminationReason(requirement.getTerminationReason())
                .build();
    }

    public static List<PssRequirement> pssRequirementsFrom(CommunityApiPssRequirementsResponse pssRequirementsResponse) {
        return Optional.ofNullable(pssRequirementsResponse.getPssRequirements()).orElse(Collections.emptyList())
            .stream()
            .map(RequirementMapper::buildRequirement)
            .collect(Collectors.toList());
    }

    private static PssRequirement buildRequirement(CommunityApiPssRequirementResponse pssRequirementResponse) {
        return PssRequirement.builder()
            .description(pssRequirementResponse.getType().getDescription())
            .subTypeDescription(Optional.ofNullable(pssRequirementResponse.getSubType())
                .map(pss -> pssRequirementResponse.getSubType().getDescription())
                .orElse(null))
            .active(Optional.ofNullable(pssRequirementResponse.getActive()).orElse(false))
            .build();
    }

    public static List<LicenceCondition> licenceConditionsFrom(CommunityApiLicenceConditionsResponse licenceConditionsResponse) {
        return Optional.ofNullable(licenceConditionsResponse.getLicenceConditions()).orElse(Collections.emptyList())
            .stream()
            .map(lic ->  LicenceCondition.builder()
                .description(lic.getLicenceConditionTypeMainCat().getDescription())
                .active(Optional.ofNullable(lic.getActive()).orElse(false))
                .build())
            .collect(Collectors.toList());
    }
}
