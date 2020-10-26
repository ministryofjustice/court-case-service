package uk.gov.justice.probation.courtcaseservice.restclient.communityapi.mapper;

import java.time.LocalDateTime;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;
import uk.gov.justice.probation.courtcaseservice.controller.model.BreachResponse;
import uk.gov.justice.probation.courtcaseservice.restclient.communityapi.model.CommunityApiNsi;
import uk.gov.justice.probation.courtcaseservice.restclient.communityapi.model.CommunityApiNsiManager;
import uk.gov.justice.probation.courtcaseservice.restclient.communityapi.model.CommunityApiNsiStatus;
import uk.gov.justice.probation.courtcaseservice.restclient.communityapi.model.CommunityApiProbationArea;
import uk.gov.justice.probation.courtcaseservice.restclient.communityapi.model.CommunityApiStaffWrapper;
import uk.gov.justice.probation.courtcaseservice.restclient.communityapi.model.CommunityApiTeam;
import uk.gov.justice.probation.courtcaseservice.service.model.Conviction;
import uk.gov.justice.probation.courtcaseservice.service.model.Sentence;
import uk.gov.justice.probation.courtcaseservice.service.model.document.GroupedDocuments;
import uk.gov.justice.probation.courtcaseservice.service.model.document.OffenderDocumentDetail;

import static uk.gov.justice.probation.courtcaseservice.service.model.document.DocumentType.NSI_DOCUMENT;

public class NsiMapper {

    public static BreachResponse breachOf(CommunityApiNsi nsi, Conviction conviction, GroupedDocuments breachDocuments, String courtName) {
        CommunityApiNsiManager nsiManager = getMostRecentNsiManager(nsi)
                .orElse(CommunityApiNsiManager.builder()
                        .build());

        return BreachResponse.builder()
                .breachId(nsi.getNsiId())
                .incidentDate(nsi.getReferralDate())
                .started(nsi.getActualStartDate())
                .statusDate(Optional.ofNullable(nsi.getStatusDate()).map(LocalDateTime::toLocalDate).orElse(null))
                .officer(getOfficer(nsiManager))
                .provider(getProvider(nsiManager))
                .team(getTeam(nsiManager))
                .status(getStatus(nsi))
                .sentencingCourtName(courtName)
                .order(getOrder(conviction.getSentence()))
                .documents(getMatchedDocsFor(breachDocuments, nsi.getNsiId(), conviction.getConvictionId()))
            .build();
    }

    private static String getOrder(Sentence sentence) {
        return Optional.ofNullable(sentence)
                .map(sentenceDetail -> getOrderName(sentenceDetail.getDescription(), sentenceDetail.getLength(), sentenceDetail.getLengthUnits()))
                .orElse(null);
    }

    private static String getOrderName(String description, Integer length, String lengthUnits) {
        if (length == null || lengthUnits == null) {
            return Optional.ofNullable(description).orElse(null);
        }
        return Optional.ofNullable(description)
                .map(desc -> desc + " (" + length + " " + lengthUnits + ")")
                .orElse(null);
    }

    private static String getStatus(CommunityApiNsi nsi) {
        return Optional.ofNullable(nsi.getStatus())
                .map(CommunityApiNsiStatus::getDescription)
                .orElse(null);
    }

    private static String getTeam(CommunityApiNsiManager nsiManager) {
        return Optional.ofNullable(nsiManager.getTeam())
                .map(CommunityApiTeam::getDescription)
                .orElse(null);
    }

    private static String getProvider(CommunityApiNsiManager nsiManager) {
        return Optional.ofNullable(nsiManager.getProbationArea())
                .map(CommunityApiProbationArea::getDescription)
                .orElse(null);
    }

    private static String getOfficer(CommunityApiNsiManager nsiManager) {
        return Optional.ofNullable(nsiManager.getStaff())
                .map(CommunityApiStaffWrapper::getStaff)
                .map(staff -> String.format("%s %s", staff.getForenames(), staff.getSurname()))
                .orElse(null);
    }

    private static Optional<CommunityApiNsiManager> getMostRecentNsiManager(CommunityApiNsi nsi) {
        return Optional.ofNullable(nsi.getNsiManagers()).orElse(Collections.emptyList())
                .stream()
                .max(Comparator.comparing(CommunityApiNsiManager::getStartDate));
    }

    private static List<OffenderDocumentDetail> getMatchedDocsFor(GroupedDocuments groupedDocuments, Long parentTableKeyId, String convictionId) {
        return Optional.ofNullable(groupedDocuments).orElse(GroupedDocuments.builder().convictions(Collections.emptyList()).build())
            .getConvictions()
            .stream()
            .filter(convictionDoc -> convictionId.equals(convictionDoc.getConvictionId()))
            .flatMap(matchConvictionDoc -> matchConvictionDoc.getDocuments().stream().filter(doc -> NSI_DOCUMENT.equals(doc.getType())))
            .filter(matchNsiTypeDoc -> parentTableKeyId.equals(matchNsiTypeDoc.getParentPrimaryKeyId()))
            .collect(Collectors.toList());
    }
}
