package uk.gov.justice.probation.courtcaseservice.service;


import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import uk.gov.justice.probation.courtcaseservice.controller.model.GroupedOffenderMatchesRequest;
import uk.gov.justice.probation.courtcaseservice.jpa.entity.CourtCaseEntity;
import uk.gov.justice.probation.courtcaseservice.jpa.entity.GroupedOffenderMatchesEntity;
import uk.gov.justice.probation.courtcaseservice.jpa.repository.OffenderMatchRepository;
import uk.gov.justice.probation.courtcaseservice.service.mapper.OffenderMatchMapper;

import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class OffenderMatchServiceTest {
    public static final String COURT_CODE = "SHF";
    public static final String CASE_NO = "123456789";
    public static final long ID = 1234L;
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
    private OffenderMatchService service;

    @BeforeEach
    public void setUp() {
        service = new OffenderMatchService(courtCaseService, offenderMatchRepository, mapper);
    }

    @Test
    public void givenValidRequest_thenCreateAndReturnMatch() {
        when(courtCaseService.getCaseByCaseNumber(COURT_CODE, CASE_NO)).thenReturn(courtCaseEntity);
        when(offenderMatchRepository.save(any(GroupedOffenderMatchesEntity.class))).thenReturn(groupedOffenderMatchesEntity);
        when(mapper.entityOf(groupedOffenderMatchesRequest, courtCaseEntity)).thenReturn(groupedOffenderMatchesEntity);
        GroupedOffenderMatchesEntity match = service.createGroupedMatches(COURT_CODE, CASE_NO, groupedOffenderMatchesRequest);

        assertThat(match).isEqualTo(groupedOffenderMatchesEntity);
    }

    @Test
    public void givenValidGetRequest_thenReturnValidMatch() {
        when(offenderMatchRepository.findById(ID)).thenReturn(Optional.of(groupedOffenderMatchesEntity));

        Optional<GroupedOffenderMatchesEntity> entity = service.getGroupedMatches(COURT_CODE, CASE_NO, ID).blockOptional();
        assertThat(entity).isPresent();
        assertThat(entity.get()).isEqualTo(groupedOffenderMatchesEntity);
    }
}