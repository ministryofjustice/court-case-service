package uk.gov.justice.probation.courtcaseservice.jpa.repository;

import uk.gov.justice.probation.courtcaseservice.jpa.entity.HearingEntity;
import uk.gov.justice.probation.courtcaseservice.service.model.HearingSearchFilter;

import java.util.List;


public interface HearingSearchRepositoryCustom {
    List<HearingEntity> filterHearings(HearingSearchFilter hearingSearchFilter);
}
