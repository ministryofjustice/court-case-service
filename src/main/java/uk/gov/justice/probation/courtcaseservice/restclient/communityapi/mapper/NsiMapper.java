package uk.gov.justice.probation.courtcaseservice.restclient.communityapi.mapper;

import static uk.gov.justice.probation.courtcaseservice.service.model.document.DocumentType.NSI_DOCUMENT;

import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;
import org.springframework.stereotype.Component;
import uk.gov.justice.probation.courtcaseservice.controller.model.BreachResponse;
import uk.gov.justice.probation.courtcaseservice.restclient.communityapi.model.*;
import uk.gov.justice.probation.courtcaseservice.service.model.Conviction;

import java.util.Comparator;
import java.util.Optional;
import uk.gov.justice.probation.courtcaseservice.service.model.document.GroupedDocuments;
import uk.gov.justice.probation.courtcaseservice.service.model.document.OffenderDocumentDetail;

@Component
public class NsiMapper {

    public BreachResponse breachOf(CommunityApiNsi nsi, Conviction conviction, GroupedDocuments breachDocuments) {
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
                .documents(getMatchedDocsFor(breachDocuments, nsi.getNsiId(), conviction.getConvictionId()))
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
        return Optional.ofNullable(nsi.getNsiManagers()).orElse(Collections.emptyList())
                .stream()
                .max(Comparator.comparing(CommunityApiNsiManager::getStartDate));
    }

    private List<OffenderDocumentDetail> getMatchedDocsFor(GroupedDocuments groupedDocuments, Long parentTableKeyId, String convictionId) {
        return Optional.ofNullable(groupedDocuments).orElse(GroupedDocuments.builder().convictions(Collections.emptyList()).build())
            .getConvictions()
            .stream()
            .filter(convictionDoc -> convictionId.equals(convictionDoc.getConvictionId()))
            .flatMap(matchConvictionDoc -> matchConvictionDoc.getDocuments().stream().filter(doc -> NSI_DOCUMENT.equals(doc.getType())))
            .filter(matchNsiTypeDoc -> parentTableKeyId.equals(matchNsiTypeDoc.getParentPrimaryKeyId()))
            .collect(Collectors.toList());
    }
}
