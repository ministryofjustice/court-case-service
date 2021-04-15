package uk.gov.justice.probation.courtcaseservice.restclient.communityapi.model;

import java.time.LocalDate;
import io.swagger.annotations.ApiModelProperty;
import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor(access = AccessLevel.PRIVATE, force = true)
@AllArgsConstructor
public class CommunityApiCommunityOrPrisonOffenderManager {
    @ApiModelProperty(value = "Staff code", example = "CHSE755")
    private final String staffCode;
    @ApiModelProperty(value = "Staff id", example = "123455")
    private final Long staffId;
    @ApiModelProperty(value = "True if this offender manager is the current responsible officer", example = "true")
    private final Boolean isResponsibleOfficer;
    @ApiModelProperty(value = "True if this offender manager is the prison OM else False", example = "true")
    private final Boolean isPrisonOffenderManager;
    @ApiModelProperty(value = "True if no real offender manager has been allocated and this is just a placeholder", example = "true")
    private final Boolean isUnallocated;
    @ApiModelProperty(value = "staff name and contact details")
    private final CommunityApiStaff staff;
    @ApiModelProperty(value = "Team details for this offender manager")
    private final CommunityApiTeam team;
    @ApiModelProperty(value = "Probation area / prison institution for this OM")
    private final CommunityApiProbationArea probationArea;
    @ApiModelProperty(value = "Date since the offender manager was assigned", example = "2019-12-04")
    private final LocalDate fromDate;
}
