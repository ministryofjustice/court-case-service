package uk.gov.justice.probation.courtcaseservice.restclient.communityapi.mapper;

import org.springframework.stereotype.Component;
import uk.gov.justice.probation.courtcaseservice.restclient.communityapi.model.CommunityApiOffenderManager;
import uk.gov.justice.probation.courtcaseservice.restclient.communityapi.model.CommunityApiOffenderResponse;
import uk.gov.justice.probation.courtcaseservice.service.model.Offender;
import uk.gov.justice.probation.courtcaseservice.service.model.OffenderManager;

@Component
public class OffenderMapper {
    public Offender offenderFrom(CommunityApiOffenderResponse offenderResponse) {
        return Offender.builder()
                .crn(offenderResponse.getOtherIds().getCrn())
                .offenderManager(buildOffenderManager(offenderResponse.getOffenderManagers().get(0)))
                .build();
    }

    private OffenderManager buildOffenderManager(CommunityApiOffenderManager offenderManager) {
        var staff = offenderManager.getStaff();
        return OffenderManager.builder()
            .forenames(staff.getForenames())
            .surname(staff.getSurname())
            .allocatedDate(offenderManager.getFromDate())
            .build();
    }
}
