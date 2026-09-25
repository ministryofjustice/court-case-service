package uk.gov.justice.probation.courtcaseservice.service.cpr;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import uk.gov.justice.probation.courtcaseservice.controller.model.GroupedOffenderMatchesRequest;
import uk.gov.justice.probation.courtcaseservice.controller.model.MatchIdentifiers;
import uk.gov.justice.probation.courtcaseservice.controller.model.OffenderMatchAlias;
import uk.gov.justice.probation.courtcaseservice.controller.model.OffenderMatchRequest;
import uk.gov.justice.probation.courtcaseservice.jpa.entity.GroupedOffenderMatchesEntity;
import uk.gov.justice.probation.courtcaseservice.jpa.entity.HearingEntity;
import uk.gov.justice.probation.courtcaseservice.jpa.repository.GroupedOffenderMatchRepository;
import uk.gov.justice.probation.courtcaseservice.restclient.cpr.CprAlias;
import uk.gov.justice.probation.courtcaseservice.restclient.cpr.CprDefendant;
import uk.gov.justice.probation.courtcaseservice.service.mapper.OffenderMatchMapper;
import uk.gov.justice.probation.courtcaseservice.service.model.MatchType;

import java.util.List;

@Service
@RequiredArgsConstructor
public class DefaultGroupedOffenderMatchUpdateService
        implements GroupedOffenderMatchUpdateService {

    private final GroupedOffenderMatchRepository groupedOffenderMatchRepository;

    @Override
    @Transactional
    public void update(
            HearingEntity savedHearing,
            String defendantId,
            CprDefendant cprDefendant
    ) {
        List<String> crns = cprDefendant.getCrns();

        if (crns.isEmpty()) {
            return;
        }

        GroupedOffenderMatchesRequest request =
                GroupedOffenderMatchesRequest.builder()
                        .matches(
                                crns.stream()
                                        .map(crn ->
                                                buildMatchRequest(
                                                        crn,
                                                        cprDefendant
                                                                .getAliases()
                                                )
                                        )
                                        .toList()
                        )
                        .build();

        String caseId = savedHearing.getCaseId();

        GroupedOffenderMatchesEntity group =
                groupedOffenderMatchRepository
                        .findByCaseIdAndDefendantId(
                                caseId,
                                defendantId
                        )
                        .map(existing ->
                                OffenderMatchMapper.update(
                                        caseId,
                                        defendantId,
                                        existing,
                                        request
                                )
                        )
                        .orElseGet(() ->
                                OffenderMatchMapper.newGroupedMatchesOf(
                                        defendantId,
                                        request,
                                        caseId
                                )
                        );

        groupedOffenderMatchRepository.save(group);
    }

    private OffenderMatchRequest buildMatchRequest(
            String crn,
            List<CprAlias> aliases
    ) {
        return OffenderMatchRequest.builder()
                .matchIdentifiers(
                        MatchIdentifiers.builder()
                                .crn(crn)
                                .aliases(mapAliases(aliases))
                                .build()
                )
                .matchType(MatchType.NAME_DOB_ALIAS)
                .confirmed(false)
                .rejected(false)
                .build();
    }

    private List<OffenderMatchAlias> mapAliases(
            List<CprAlias> aliases
    ) {
        if (aliases == null || aliases.isEmpty()) {
            return null;
        }

        return aliases.stream()
                .map(alias ->
                        OffenderMatchAlias.builder()
                                .firstName(alias.getFirstName())
                                .surname(alias.getLastName())
                                .middleNames(
                                        alias.getMiddleNames() == null
                                                ? null
                                                : List.of(
                                                        alias.getMiddleNames()
                                                )
                                )
                                .build()
                )
                .toList();
    }
}