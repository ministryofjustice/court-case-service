package uk.gov.justice.probation.courtcaseservice.service.mapper;

import org.springframework.stereotype.Component;
import uk.gov.justice.probation.courtcaseservice.jpa.entity.HearingEntity;
import uk.gov.justice.probation.courtcaseservice.service.model.CaseProgressHearing;

import java.util.List;
import java.util.stream.Collectors;

@Component
public class CaseProgressMapper {
    public List<CaseProgressHearing> mapFrom(List<HearingEntity> hearingEntities) {
        return hearingEntities.stream().map(hearingEntity -> CaseProgressHearing.of(hearingEntity)).collect(Collectors.toList());
    }
}
