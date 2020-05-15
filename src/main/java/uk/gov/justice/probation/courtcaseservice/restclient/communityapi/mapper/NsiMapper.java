package uk.gov.justice.probation.courtcaseservice.restclient.communityapi.mapper;

import org.springframework.stereotype.Component;
import uk.gov.justice.probation.courtcaseservice.controller.model.BreachResponse;
import uk.gov.justice.probation.courtcaseservice.restclient.communityapi.model.*;
import uk.gov.justice.probation.courtcaseservice.service.model.Conviction;

import java.util.Comparator;
import java.util.Optional;

@Component
public class NsiMapper {
    public BreachResponse breachOf(CommunityApiNsi nsi, Conviction conviction) {
        CommunityApiNsiManager nsiManager = getMostRecentNsiManager(nsi)
                .orElse(CommunityApiNsiManager.builder()
                        .build());

        return BreachResponse.builder()
                .breachId(nsi.getNsiId())
                .incidentDate(nsi.getReferralDate())
                .started(nsi.getActualStartDate())
                .officer(getOfficer(nsiManager))
                .provider(getProvider(nsiManager))
                .team(getTeam(nsiManager))
                .status(getStatus(nsi))
                .order(conviction.getSentence().getDescription())
                .build();
    }

    private String getStatus(CommunityApiNsi nsi) {
        return Optional.ofNullable(nsi.getStatus())
                .map(CommunityApiNsiStatus::getDescription)
                .orElse(null);
    }

    private String getTeam(CommunityApiNsiManager nsiManager) {
        return Optional.ofNullable(nsiManager.getTeam())
                .map(CommunityApiTeam::getDescription)
                .orElse(null);
    }

    private String getProvider(CommunityApiNsiManager nsiManager) {
        return Optional.ofNullable(nsiManager.getProbationArea())
                .map(CommunityApiProbationArea::getDescription)
                .orElse(null);
    }

    private String getOfficer(CommunityApiNsiManager nsiManager) {
        return Optional.ofNullable(nsiManager.getStaff())
                .map(CommunityApiStaffWrapper::getStaff)
                .map(staff -> String.format("%s %s", staff.getForenames(), staff.getSurname()))
                .orElse(null);
    }

    private Optional<CommunityApiNsiManager> getMostRecentNsiManager(CommunityApiNsi nsi) {
        return nsi.getNsiManagers()
                .stream()
                .max(Comparator.comparing(CommunityApiNsiManager::getStartDate));
    }
}
