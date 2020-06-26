package uk.gov.justice.probation.courtcaseservice.service;


import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import uk.gov.justice.probation.courtcaseservice.controller.model.GroupedOffenderMatchesRequest;
import uk.gov.justice.probation.courtcaseservice.jpa.entity.CourtCaseEntity;
import uk.gov.justice.probation.courtcaseservice.jpa.entity.GroupedOffenderMatchesEntity;
import uk.gov.justice.probation.courtcaseservice.jpa.repository.OffenderMatchRepository;
import uk.gov.justice.probation.courtcaseservice.service.mapper.OffenderMatchMapper;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class OffenderMatchServiceTest {
    public static final String COURT_CODE = "SHF";
    public static final String CASE_NO = "123456789";
    @Mock
    private CourtCaseService courtCaseService;
    @Mock
    private OffenderMatchRepository offenderMatchRepository;
    @Mock
    private CourtCaseEntity courtCaseEntity;
    @Mock
    private GroupedOffenderMatchesEntity groupedOffenderMatchesEntity;
    @Mock
    private GroupedOffenderMatchesRequest groupedOffenderMatchesRequest;
    @Mock
    private OffenderMatchMapper mapper;


    @Test
    public void givenValidRequest_thenCreateAndReturnMatch() {
        when(courtCaseService.getCaseByCaseNumber(COURT_CODE, CASE_NO)).thenReturn(courtCaseEntity);
        when(offenderMatchRepository.save(any(GroupedOffenderMatchesEntity.class))).thenReturn(groupedOffenderMatchesEntity);
        when(mapper.entityOf(groupedOffenderMatchesRequest, courtCaseEntity)).thenReturn(groupedOffenderMatchesEntity);
        OffenderMatchService service = new OffenderMatchService(courtCaseService, offenderMatchRepository, mapper);
        GroupedOffenderMatchesEntity match = service.createGroupedMatches(COURT_CODE, CASE_NO, groupedOffenderMatchesRequest);

        assertThat(match).isEqualTo(groupedOffenderMatchesEntity);
    }

}