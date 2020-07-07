package uk.gov.justice.probation.courtcaseservice.service;


import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import uk.gov.justice.probation.courtcaseservice.controller.model.GroupedOffenderMatchesRequest;
import uk.gov.justice.probation.courtcaseservice.jpa.entity.CourtCaseEntity;
import uk.gov.justice.probation.courtcaseservice.jpa.entity.GroupedOffenderMatchesEntity;
import uk.gov.justice.probation.courtcaseservice.jpa.repository.GroupedOffenderMatchRepository;
import uk.gov.justice.probation.courtcaseservice.service.exceptions.EntityNotFoundException;
import uk.gov.justice.probation.courtcaseservice.service.mapper.OffenderMatchMapper;

import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatExceptionOfType;
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
    private GroupedOffenderMatchRepository offenderMatchRepository;
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
    public void givenValidRequest_whenCreateGroupedMatchesCalled_thenCreateAndReturnMatch() {
        when(courtCaseService.getCaseByCaseNumber(COURT_CODE, CASE_NO)).thenReturn(courtCaseEntity);
        when(offenderMatchRepository.save(any(GroupedOffenderMatchesEntity.class))).thenReturn(groupedOffenderMatchesEntity);
        when(mapper.groupedMatchesOf(groupedOffenderMatchesRequest, courtCaseEntity)).thenReturn(groupedOffenderMatchesEntity);
        Optional<GroupedOffenderMatchesEntity> match = service.createGroupedMatches(COURT_CODE, CASE_NO, groupedOffenderMatchesRequest).blockOptional();

        assertThat(match).isPresent();
        assertThat(match.get()).isEqualTo(groupedOffenderMatchesEntity);
    }

    @Test
    public void givenValidRequest_whenGetGroupedMatches_thenReturnValidMatch() {
        when(groupedOffenderMatchesEntity.getCourtCase()).thenReturn(courtCaseEntity);
        when(courtCaseEntity.getCourtCode()).thenReturn(COURT_CODE);
        when(courtCaseEntity.getCaseNo()).thenReturn(CASE_NO);
        when(offenderMatchRepository.findById(ID)).thenReturn(Optional.of(groupedOffenderMatchesEntity));

        Optional<GroupedOffenderMatchesEntity> entity = service.getGroupedMatches(COURT_CODE, CASE_NO, ID).blockOptional();
        assertThat(entity).isPresent();
        assertThat(entity.get()).isEqualTo(groupedOffenderMatchesEntity);
    }

    @Test
    public void givenCourtCodeDoesNotMatch_whenGetGroupedMatches_thenThrowEntityNotFound() {
        when(groupedOffenderMatchesEntity.getCourtCase()).thenReturn(courtCaseEntity);
        when(courtCaseEntity.getCourtCode()).thenReturn(COURT_CODE);
        when(courtCaseEntity.getCaseNo()).thenReturn(CASE_NO);
        when(offenderMatchRepository.findById(ID)).thenReturn(Optional.of(groupedOffenderMatchesEntity));

        assertThatExceptionOfType(EntityNotFoundException.class)
                .isThrownBy(() -> service.getGroupedMatches("BAD_CODE", CASE_NO, ID).blockOptional());
    }

    @Test
    public void givenCaseNoDoesNotMatch_whenGetGroupedMatches_thenThrowEntityNotFound() {
        when(groupedOffenderMatchesEntity.getCourtCase()).thenReturn(courtCaseEntity);
        when(courtCaseEntity.getCaseNo()).thenReturn(CASE_NO);
        when(offenderMatchRepository.findById(ID)).thenReturn(Optional.of(groupedOffenderMatchesEntity));

        assertThatExceptionOfType(EntityNotFoundException.class)
                .isThrownBy(() -> service.getGroupedMatches(COURT_CODE, "99999", ID).blockOptional());
    }
}