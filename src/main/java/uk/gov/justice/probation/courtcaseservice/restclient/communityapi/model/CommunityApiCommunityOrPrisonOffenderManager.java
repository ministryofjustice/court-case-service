package uk.gov.justice.probation.courtcaseservice.restclient.communityapi.model;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDate;

@Data
@Builder
@NoArgsConstructor(access = AccessLevel.PRIVATE, force = true)
@AllArgsConstructor
public class CommunityApiCommunityOrPrisonOffenderManager {
    @Schema(description = "Staff code", example = "CHSE755")
    private final String staffCode;
    @Schema(description = "Staff id", example = "123455")
    private final Long staffId;
    @Schema(description = "True if this offender manager is the current responsible officer", example = "true")
    private final Boolean isResponsibleOfficer;
    @Schema(description = "True if this offender manager is the prison OM else False", example = "true")
    private final Boolean isPrisonOffenderManager;
    @Schema(description = "True if no real offender manager has been allocated and this is just a placeholder", example = "true")
    private final Boolean isUnallocated;
    @Schema(description = "staff name and contact details")
    private final CommunityApiStaff staff;
    @Schema(description = "Team details for this offender manager")
    private final CommunityApiTeam team;
    @Schema(description = "Probation area / prison institution for this OM")
    private final CommunityApiProbationArea probationArea;
    @Schema(description = "Date since the offender manager was assigned", example = "2019-12-04")
    private final LocalDate fromDate;
}
