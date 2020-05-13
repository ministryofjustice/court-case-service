package uk.gov.justice.probation.courtcaseservice.restclient.communityapi.mapper;

import org.springframework.stereotype.Component;
import uk.gov.justice.probation.courtcaseservice.controller.model.BreachResponse;
import uk.gov.justice.probation.courtcaseservice.restclient.communityapi.model.CommunityApiNsi;
import uk.gov.justice.probation.courtcaseservice.restclient.communityapi.model.NsiManager;
import uk.gov.justice.probation.courtcaseservice.restclient.communityapi.model.Staff;

@Component
public class NsiMapper {
    public BreachResponse breachOf(CommunityApiNsi nsi) {
        NsiManager nsiManager = nsi.getNsiManagers().get(0);
        Staff staff = nsiManager.getStaff().getStaff();
        return BreachResponse.builder()
                .breachId(nsi.getNsiId())
                .incidentDate(nsi.getReferralDate())
                .started(nsi.getActualStartDate())
                .officer(String.format("%s %s", staff.getForenames(), staff.getSurname()))
                .provider(nsiManager.getProbationArea().getDescription())
                .team(nsiManager.getTeam().getDescription())
                .status(nsi.getNsiStatus().getDescription())
                .build();
    }
}
