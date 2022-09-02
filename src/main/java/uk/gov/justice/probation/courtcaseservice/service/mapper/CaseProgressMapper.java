package uk.gov.justice.probation.courtcaseservice.service.mapper;

import org.springframework.stereotype.Component;
import uk.gov.justice.probation.courtcaseservice.jpa.entity.HearingEntity;
import uk.gov.justice.probation.courtcaseservice.service.model.CaseProgressHearing;

import java.util.Collections;
import java.util.List;

@Component
public class CaseProgressMapper {
    public List<CaseProgressHearing> mapFrom(List<HearingEntity> hearingEntities) {
        return Collections.emptyList();
    }
}
