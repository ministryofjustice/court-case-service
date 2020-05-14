package uk.gov.justice.probation.courtcaseservice.restclient.communityapi.mapper;

import org.springframework.stereotype.Component;
import uk.gov.justice.probation.courtcaseservice.controller.model.BreachResponse;
import uk.gov.justice.probation.courtcaseservice.restclient.communityapi.model.*;

import java.util.Comparator;
import java.util.Optional;

@Component
public class NsiMapper {
    public BreachResponse breachOf(CommunityApiNsi nsi) {
        NsiManager nsiManager = getMostRecentNsiManager(nsi)
                .orElse(NsiManager.builder()
                        .build());

        return BreachResponse.builder()
                .breachId(nsi.getNsiId())
                .incidentDate(nsi.getReferralDate())
                .started(nsi.getActualStartDate())
                .officer(getOfficer(nsiManager))
                .provider(getProvider(nsiManager))
                .team(getTeam(nsiManager))
                .status(getStatus(nsi))
                .build();
    }

    private String getStatus(CommunityApiNsi nsi) {
        return Optional.ofNullable(nsi.getNsiStatus())
                .map(NsiStatus::getDescription)
                .orElse(null);
    }

    private String getTeam(NsiManager nsiManager) {
        return Optional.ofNullable(nsiManager.getTeam())
                .map(Team::getDescription)
                .orElse(null);
    }

    private String getProvider(NsiManager nsiManager) {
        return Optional.ofNullable(nsiManager.getProbationArea())
                .map(ProbationArea::getDescription)
                .orElse(null);
    }

    private String getOfficer(NsiManager nsiManager) {
        return Optional.ofNullable(nsiManager.getStaff())
                .map(StaffWrapper::getStaff)
                .map(staff -> String.format("%s %s", staff.getForenames(), staff.getSurname()))
                .orElse(null);
    }

    private Optional<NsiManager> getMostRecentNsiManager(CommunityApiNsi nsi) {
        return nsi.getNsiManagers()
                .stream()
                .max(Comparator.comparing(NsiManager::getStartDate));
    }
}
