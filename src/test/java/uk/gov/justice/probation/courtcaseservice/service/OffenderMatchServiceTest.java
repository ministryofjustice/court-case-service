package uk.gov.justice.probation.courtcaseservice.service;


import java.time.LocalDate;
import java.time.Month;
import java.util.Collections;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import reactor.core.publisher.Mono;
import uk.gov.justice.probation.courtcaseservice.controller.model.GroupedOffenderMatchesRequest;
import uk.gov.justice.probation.courtcaseservice.controller.model.OffenderMatchDetail;
import uk.gov.justice.probation.courtcaseservice.controller.model.OffenderMatchDetailResponse;
import uk.gov.justice.probation.courtcaseservice.jpa.entity.CourtCaseEntity;
import uk.gov.justice.probation.courtcaseservice.jpa.entity.GroupedOffenderMatchesEntity;
import uk.gov.justice.probation.courtcaseservice.jpa.entity.OffenderMatchEntity;
import uk.gov.justice.probation.courtcaseservice.jpa.repository.GroupedOffenderMatchRepository;
import uk.gov.justice.probation.courtcaseservice.restclient.OffenderRestClient;
import uk.gov.justice.probation.courtcaseservice.restclient.communityapi.mapper.OffenderMapper;
import uk.gov.justice.probation.courtcaseservice.restclient.exception.OffenderNotFoundException;
import uk.gov.justice.probation.courtcaseservice.service.exceptions.EntityNotFoundException;
import uk.gov.justice.probation.courtcaseservice.service.mapper.OffenderMatchMapper;
import uk.gov.justice.probation.courtcaseservice.service.model.Conviction;
import uk.gov.justice.probation.courtcaseservice.service.model.Sentence;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatExceptionOfType;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class OffenderMatchServiceTest {
    public static final String COURT_CODE = "SHF";
    public static final String CASE_NO = "123456789";
    public static final long ID = 1234L;
    @Mock
    private OffenderRestClient offenderRestClient;
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
    @Mock
    private OffenderMapper offenderMapper;

    @InjectMocks
    private OffenderMatchService service;

    @Test
    void givenValidRequest_whenCreateGroupedMatchesCalled_thenCreateAndReturnMatch() {
        when(offenderMatchRepository.findByCourtCodeAndCaseNo(COURT_CODE, CASE_NO)).thenReturn(Optional.empty());
        when(courtCaseService.getCaseByCaseNumber(COURT_CODE, CASE_NO)).thenReturn(courtCaseEntity);
        when(mapper.groupedMatchesOf(groupedOffenderMatchesRequest, courtCaseEntity)).thenReturn(groupedOffenderMatchesEntity);
        when(offenderMatchRepository.save(groupedOffenderMatchesEntity)).thenReturn(groupedOffenderMatchesEntity);

        Optional<GroupedOffenderMatchesEntity> match = service.createOrUpdateGroupedMatches(COURT_CODE, CASE_NO, groupedOffenderMatchesRequest).blockOptional();

        assertThat(match).isPresent();
        assertThat(match.get()).isEqualTo(groupedOffenderMatchesEntity);
    }

    @Test
    void givenValidRequest_whenUpdateGroupedMatchesCalled_thenCreateAndReturnMatch() {

        when(offenderMatchRepository.findByCourtCodeAndCaseNo(COURT_CODE, CASE_NO)).thenReturn(Optional.of(groupedOffenderMatchesEntity));
        when(groupedOffenderMatchesEntity.getId()).thenReturn(99L);
        when(groupedOffenderMatchesEntity.getCourtCase()).thenReturn(courtCaseEntity);
        when(mapper.groupedMatchesOf(groupedOffenderMatchesRequest, courtCaseEntity, 99L)).thenReturn(groupedOffenderMatchesEntity);
        when(offenderMatchRepository.save(any(GroupedOffenderMatchesEntity.class))).thenReturn(groupedOffenderMatchesEntity);

        Optional<GroupedOffenderMatchesEntity> match = service.createOrUpdateGroupedMatches(COURT_CODE, CASE_NO, groupedOffenderMatchesRequest).blockOptional();

        assertThat(match).isPresent();
        assertThat(match.get()).isEqualTo(groupedOffenderMatchesEntity);
    }

    @Test
    void givenValidRequest_whenGetGroupedMatches_thenReturnValidMatch() {
        when(groupedOffenderMatchesEntity.getCourtCase()).thenReturn(courtCaseEntity);
        when(courtCaseEntity.getCourtCode()).thenReturn(COURT_CODE);
        when(courtCaseEntity.getCaseNo()).thenReturn(CASE_NO);
        when(offenderMatchRepository.findById(ID)).thenReturn(Optional.of(groupedOffenderMatchesEntity));

        Optional<GroupedOffenderMatchesEntity> entity = service.getGroupedMatches(COURT_CODE, CASE_NO, ID).blockOptional();
        assertThat(entity).isPresent();
        assertThat(entity.get()).isEqualTo(groupedOffenderMatchesEntity);
    }

    @Test
    void givenCourtCodeDoesNotMatch_whenGetGroupedMatches_thenThrowEntityNotFound() {
        when(groupedOffenderMatchesEntity.getCourtCase()).thenReturn(courtCaseEntity);
        when(courtCaseEntity.getCourtCode()).thenReturn(COURT_CODE);
        when(courtCaseEntity.getCaseNo()).thenReturn(CASE_NO);
        when(offenderMatchRepository.findById(ID)).thenReturn(Optional.of(groupedOffenderMatchesEntity));

        assertThatExceptionOfType(EntityNotFoundException.class)
                .isThrownBy(() -> service.getGroupedMatches("BAD_CODE", CASE_NO, ID).blockOptional());
    }

    @Test
    void givenCaseNoDoesNotMatch_whenGetGroupedMatches_thenThrowEntityNotFound() {
        when(groupedOffenderMatchesEntity.getCourtCase()).thenReturn(courtCaseEntity);
        when(courtCaseEntity.getCaseNo()).thenReturn(CASE_NO);
        when(offenderMatchRepository.findById(ID)).thenReturn(Optional.of(groupedOffenderMatchesEntity));

        assertThatExceptionOfType(EntityNotFoundException.class)
                .isThrownBy(() -> service.getGroupedMatches(COURT_CODE, "99999", ID).blockOptional());
    }

    @Test
    void givenMultipleConvictionsIncludingNullDates_whenGetMostRecentSentence_theReturn() {
        LocalDate date20July = LocalDate.of(2020, Month.JULY, 20);
        LocalDate date21July = LocalDate.of(2020, Month.JULY, 21);
        Sentence sentence3 = Sentence.builder().description("B").build();
        Conviction conviction1 = Conviction.builder().sentence(Sentence.builder().description("A").build()).build();
        Conviction conviction2 = Conviction.builder().convictionDate(date20July).sentence(Sentence.builder().description("B").build()).build();
        Conviction conviction3 = Conviction.builder().convictionDate(date21July).sentence(sentence3).build();

        Sentence sentence = service.getSentenceForMostRecentConviction(List.of(conviction1, conviction2, conviction3));

        assertThat(sentence).isSameAs(sentence3);
    }

    @Test
    void givenConvictionWithNoSentence_whenGetMostRecentSentence_thenReturnNull() {
        Sentence sentence = service.getSentenceForMostRecentConviction(List.of(Conviction.builder().build()));

        assertThat(sentence).isNull();
    }

    @Test
    void givenNullInput_whenGetMostRecentSentence_thenReturnNull() {
        assertThat(service.getSentenceForMostRecentConviction(null)).isNull();
    }

    @Test
    void whenGetOffenderMatchDetail_thenReturn() {
        Conviction conviction = buildConviction(true, "sentence1");
        OffenderMatchDetail matchDetail = OffenderMatchDetail.builder().forename("Chris").build();
        String crn = "X320741";
        mockOffenderDetailMatch(crn, matchDetail, List.of(conviction));
        OffenderMatchDetail resultMatchDetail = mock(OffenderMatchDetail.class);
        when(offenderMapper.offenderMatchDetailFrom(matchDetail, conviction.getSentence())).thenReturn(resultMatchDetail);

        OffenderMatchDetail offenderMatchDetail = service.getOffenderMatchDetail(crn);

        assertThat(offenderMatchDetail).isSameAs(resultMatchDetail);
        verify(offenderRestClient).getOffenderMatchDetailByCrn(crn);
        verify(offenderRestClient).getConvictionsByCrn(crn);
        verify(offenderMapper).offenderMatchDetailFrom(matchDetail, conviction.getSentence());
    }

    @Test
    void givenNoConvictions_whenGetOffenderMatchDetail_thenReturn() {

        OffenderMatchDetail matchDetail = OffenderMatchDetail.builder().forename("Chris").build();
        String crn = "X320741";
        mockOffenderDetailMatch(crn, matchDetail, Collections.emptyList());
        OffenderMatchDetail resultMatchDetail = mock(OffenderMatchDetail.class);
        when(offenderMapper.offenderMatchDetailFrom(matchDetail, null)).thenReturn(resultMatchDetail);

        OffenderMatchDetail offenderMatchDetail = service.getOffenderMatchDetail("X320741");

        assertThat(offenderMatchDetail).isSameAs(resultMatchDetail);
        verify(offenderRestClient).getOffenderMatchDetailByCrn(crn);
        verify(offenderRestClient).getConvictionsByCrn(crn);
        verify(offenderMapper).offenderMatchDetailFrom(matchDetail, null);
    }

    @Test
    void given404OnConvictionsCall_whenGetOffenderMatchDetail_thenReturn() {

        OffenderMatchDetail matchDetail = OffenderMatchDetail.builder().forename("Chris").build();
        String crn = "X320741";
        when(offenderRestClient.getOffenderMatchDetailByCrn(crn)).thenReturn(Mono.justOrEmpty(matchDetail));
        when(offenderRestClient.getConvictionsByCrn(crn)).thenReturn(Mono.error( new OffenderNotFoundException(crn)));
        OffenderMatchDetail resultMatchDetail = mock(OffenderMatchDetail.class);
        when(offenderMapper.offenderMatchDetailFrom(matchDetail, null)).thenReturn(resultMatchDetail);

        OffenderMatchDetail offenderMatchDetail = service.getOffenderMatchDetail("X320741");

        assertThat(offenderMatchDetail).isSameAs(resultMatchDetail);
    }

    @Test
    void givenNoMatchDetail_whenGetOffenderMatchDetail_thenReturnNull() {

        String crn = "X320741";
        Conviction conviction = buildConviction(true, "sentence1");
        mockOffenderDetailMatch(crn, null, List.of(conviction));

        OffenderMatchDetail offenderMatchDetail = service.getOffenderMatchDetail("X320741");

        assertThat(offenderMatchDetail).isNull();
        verify(offenderRestClient).getOffenderMatchDetailByCrn(crn);
        verify(offenderRestClient).getConvictionsByCrn(crn);
        verify(offenderMapper, never()).offenderMatchDetailFrom(any(OffenderMatchDetail.class), any(Sentence.class));
    }

    @Test
    void givenMultipleCrns_whenGetOffenderMatchDetails_thenReturn() {

        String crn1 = "X320741";
        String crn2 = "X320742";

        when(courtCaseService.getCaseByCaseNumber(COURT_CODE, CASE_NO)).thenReturn(courtCaseEntity);
        when(offenderMatchRepository.findByCourtCase(courtCaseEntity)).thenReturn(buildGroupedOffenderMatchesEntity(List.of(crn1, crn2)));

        Conviction conviction1 = buildConviction(true, "sentence1");
        Conviction conviction2 = buildConviction(false, "sentence2");
        OffenderMatchDetail matchDetail1 = OffenderMatchDetail.builder().forename("Chris").build();
        OffenderMatchDetail matchDetail2 = OffenderMatchDetail.builder().forename("Dave").build();

        mockOffenderDetailMatch(crn1, matchDetail1, Collections.emptyList());
        mockOffenderDetailMatch(crn2, matchDetail2, List.of(conviction2, conviction1));

        OffenderMatchDetail resultMatchDetail1 = mock(OffenderMatchDetail.class);
        OffenderMatchDetail resultMatchDetail2 = mock(OffenderMatchDetail.class);
        when(offenderMapper.offenderMatchDetailFrom(matchDetail1, null)).thenReturn(resultMatchDetail1);
        when(offenderMapper.offenderMatchDetailFrom(matchDetail2, conviction1.getSentence())).thenReturn(resultMatchDetail2);

        OffenderMatchDetailResponse response = service.getOffenderMatchDetails(COURT_CODE, CASE_NO);

        assertThat(response.getOffenderMatchDetails()).containsExactlyInAnyOrder(resultMatchDetail1, resultMatchDetail2);
    }

    private void mockOffenderDetailMatch(String crn, OffenderMatchDetail matchDetail, List<Conviction> convictions) {
        when(offenderRestClient.getOffenderMatchDetailByCrn(crn)).thenReturn(Mono.justOrEmpty(matchDetail));
        when(offenderRestClient.getConvictionsByCrn(crn)).thenReturn(Mono.just(convictions));
    }

    private Conviction buildConviction(boolean active, String sentenceDesc) {
        LocalDate date = LocalDate.of(2020, Month.JULY, 20);
        Sentence sentence = Sentence.builder().description(sentenceDesc).build();
        return Conviction.builder()
            .active(active)
            .convictionDate(date)
            .sentence(sentence)
            .build();
    }

    private GroupedOffenderMatchesEntity buildGroupedOffenderMatchesEntity(List<String> crns) {

        List<OffenderMatchEntity> offenderMatchEntities = crns.stream()
            .map(crn -> OffenderMatchEntity.builder().crn(crn).build())
            .collect(Collectors.toList());

        return GroupedOffenderMatchesEntity.builder().offenderMatches(offenderMatchEntities).build();
    }
}
