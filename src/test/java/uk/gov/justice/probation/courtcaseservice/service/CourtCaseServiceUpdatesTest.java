package uk.gov.justice.probation.courtcaseservice.service;

import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;
import org.mockito.ArgumentCaptor;
import org.mockito.Captor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.core.io.ClassPathResource;
import uk.gov.justice.probation.courtcaseservice.jpa.entity.CourtCaseEntity;
import uk.gov.justice.probation.courtcaseservice.jpa.entity.CourtEntity;
import uk.gov.justice.probation.courtcaseservice.jpa.entity.OffenceEntity;
import uk.gov.justice.probation.courtcaseservice.jpa.repository.CourtCaseRepository;
import uk.gov.justice.probation.courtcaseservice.jpa.repository.CourtRepository;

import java.io.IOException;
import java.nio.file.Files;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static uk.gov.justice.probation.courtcaseservice.service.CourtCaseServiceTest.buildOffenceEntity;

@ExtendWith(MockitoExtension.class)
public class CourtCaseServiceUpdatesTest {

    private static ObjectMapper mapper;

    private static final String CASE_NO = "100";
    private static final String COURT_CODE = "SHF";
    private static String case_two_offences;
    private static String case_three_offences;
    private static String case_no_offences;
    private static String case_fields_altered;

    @Mock
    private CourtRepository courtRepository;

    @Mock
    private CourtCaseRepository courtCaseRepository;

    @Mock
    private TelemetryService telemetryService;

    @Mock
    private CourtEntity courtEntity;

    @Captor
    private ArgumentCaptor<CourtCaseEntity> caseEntityCaptor;

    @InjectMocks
    private MutableCourtCaseService service;

    @BeforeAll
    static void beforeAll() throws IOException {
        mapper = new ObjectMapper();
        mapper.configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false);
        mapper.registerModule(new JavaTimeModule());
        ClassLoader loader = CourtCaseServiceUpdatesTest.class.getClassLoader();
        case_two_offences = Files.readString(new ClassPathResource("court-case/caseno_100_SHF_2_offences.json", loader).getFile().toPath());
        case_no_offences = Files.readString(new ClassPathResource("court-case/caseno_100_SHF_no_offences.json", loader).getFile().toPath());
        case_fields_altered = Files.readString(new ClassPathResource("court-case/caseno_100_SHF_updated.json", loader).getFile().toPath());
        case_three_offences = Files.readString(new ClassPathResource("court-case/caseno_100_SHF_3_offences.json", loader).getFile().toPath());
    }

    @DisplayName("CREATE case with no offence sequence numbers on incoming")
    @Test
    void givenNoMatch_whenUpdateByCourtAndCaseNoWithNoSequenceNumbers_ThenCreate() {
        mockForCaseForUpdate(null);

        CourtCaseEntity courtCase = CourtCaseEntity.builder().caseNo(CASE_NO).courtCode(COURT_CODE)
            .offences(List.of(OffenceEntity.builder().act("ACT-1").build(), OffenceEntity.builder().act("ACT-2").build()))
            .build();

        service.createOrUpdateCase(COURT_CODE, CASE_NO, courtCase);

        verify(courtCaseRepository).save(caseEntityCaptor.capture());
        CourtCaseEntity saved = caseEntityCaptor.getAllValues().get(0);
        // The only logic applied in this flow is that all offences get sequence numbers.
        assertThat(saved.getOffences().get(0).getSequenceNumber()).isEqualTo(1);
        assertThat(saved.getOffences().get(1).getSequenceNumber()).isEqualTo(2);
    }

    @DisplayName("CREATE case with duplicate sequence numbers in offences")
    @Test
    void givenNoMatch_whenUpdateByCourtAndCaseNoWithDuplicateSequenceNumbers_ThenCreate() {
        mockForCaseForUpdate(null);

        CourtCaseEntity courtCase = CourtCaseEntity.builder().caseNo(CASE_NO).courtCode(COURT_CODE)
            .offences(List.of(OffenceEntity.builder().sequenceNumber(2).build(), OffenceEntity.builder().sequenceNumber(2).build()))
            .build();

        service.createOrUpdateCase(COURT_CODE, CASE_NO, courtCase);

        verify(courtCaseRepository).save(caseEntityCaptor.capture());
        CourtCaseEntity saved = caseEntityCaptor.getAllValues().get(0);
        // The only logic applied in this flow is that all offences get non-duplicated sequence numbers.
        assertThat(saved.getOffences().get(0).getSequenceNumber()).isEqualTo(1);
        assertThat(saved.getOffences().get(1).getSequenceNumber()).isEqualTo(2);
    }

    @DisplayName("UPDATE on existing case, all fields are changed. Two new offences added.")
    @Test
    void whenUpdateByCourtAndCaseNo_ThenUpdateAllFields() throws IOException {

        CourtCaseEntity existing = mapper.readValue(case_no_offences, CourtCaseEntity.class);
        CourtCaseEntity incoming = mapper.readValue(case_fields_altered, CourtCaseEntity.class);
        mockForCaseForUpdate(existing);
        when(courtCaseRepository.save(existing)).thenReturn(existing);

        service.createOrUpdateCase(COURT_CODE, CASE_NO, incoming);

        verify(courtCaseRepository).save(caseEntityCaptor.capture());
        CourtCaseEntity capturedCase = caseEntityCaptor.getAllValues().get(0);

        assertThat(incoming).isEqualToIgnoringGivenFields(capturedCase, "offences");
        // From 0 offences to 2
        assertThat(capturedCase.getOffences()).hasSize(2);
        capturedCase.getOffences().forEach(offence ->
            assertThat(getOffenceBySeqNum(incoming.getOffences(), offence.getSequenceNumber())).isEqualToComparingFieldByField(offence)
        );
    }

    @DisplayName("UPDATE case. More offences, from 2 to 3.")
    @Test
    void givenMatchZeroOffences_WhenUpdateByCourtAndCaseNo_ThenUpdateAllFields() throws IOException {

        CourtCaseEntity existing = mapper.readValue(case_two_offences, CourtCaseEntity.class);
        CourtCaseEntity incoming = mapper.readValue(case_three_offences, CourtCaseEntity.class);
        mockForCaseForUpdate(existing);
        when(courtCaseRepository.save(existing)).thenReturn(existing);

        service.createOrUpdateCase(COURT_CODE, CASE_NO, incoming);

        verify(courtCaseRepository).save(caseEntityCaptor.capture());
        CourtCaseEntity capturedCase = caseEntityCaptor.getAllValues().get(0);

        assertThat(incoming).isEqualToIgnoringGivenFields(capturedCase, "offences");
        // From 2 offences to 3
        assertThat(capturedCase.getOffences()).hasSize(3);
        capturedCase.getOffences().forEach(offence ->
            assertThat(getOffenceBySeqNum(incoming.getOffences(), offence.getSequenceNumber())).isEqualToComparingFieldByField(offence)
        );
    }

    @DisplayName("UPDATE on existing case. Fewer offences, from 3 to 2.")
    @Test
    void givenUpdateWithFewerOffences_whenUpdateByCourtAndCaseNo_ThenRemoveOffences() throws IOException {

        CourtCaseEntity existing = mapper.readValue(case_three_offences, CourtCaseEntity.class);
        CourtCaseEntity incoming = mapper.readValue(case_two_offences, CourtCaseEntity.class);
        mockForCaseForUpdate(existing);
        when(courtCaseRepository.save(existing)).thenReturn(existing);

        service.createOrUpdateCase(COURT_CODE, CASE_NO, incoming);

        verify(courtCaseRepository).save(caseEntityCaptor.capture());
        CourtCaseEntity capturedCase = caseEntityCaptor.getAllValues().get(0);

        assertThat(incoming).isEqualToIgnoringGivenFields(capturedCase, "offences");
        assertThat(capturedCase.getOffences()).hasSize(2);
    }

    @DisplayName("UPDATE on existing case, existing 2 offences deleted.")
    @Test
    void givenUpdateWithNoOffences_whenUpdateByCourtAndCaseNo_ThenRemoveOffences() throws IOException {

        CourtCaseEntity existing = mapper.readValue(case_two_offences, CourtCaseEntity.class);
        CourtCaseEntity incoming = mapper.readValue(case_no_offences, CourtCaseEntity.class);
        mockForCaseForUpdate(existing);
        when(courtCaseRepository.save(existing)).thenReturn(existing);

        service.createOrUpdateCase(COURT_CODE, CASE_NO, incoming);

        verify(courtCaseRepository).save(caseEntityCaptor.capture());
        CourtCaseEntity capturedCase = caseEntityCaptor.getAllValues().get(0);

        assertThat(incoming).isEqualToIgnoringGivenFields(capturedCase, "offences");
        // From 0 offences to 2
        assertThat(capturedCase.getOffences()).hasSize(0);
    }

    private OffenceEntity getOffenceBySeqNum(List<OffenceEntity> offences, Integer sequenceNumber) {
        return offences.stream().filter(off -> sequenceNumber.equals(off.getSequenceNumber())).findFirst().orElseThrow();
    }

    @DisplayName("Tests sequencing of offences with nulls and unsorted input")
    @ParameterizedTest(name = "Input \"{0}\". Spaces are null.")
    @ValueSource(strings = {"4, 2, 2, 1", "1, 2, 3", "100,,3,,100, 2", ""})
    void processSequenceNumbersWithNulls(String sequenceNumberArg) {
        String[] arrayParam = sequenceNumberArg.split("\\s*,\\s*");
        List<OffenceEntity> offences = Arrays.stream(arrayParam)
            .map((String sequenceNumber) -> buildOffenceEntity(sequenceNumber, null))
            .collect(Collectors.toList());

        List<OffenceEntity> offenceEntities = service.applyOffenceSequencing(offences);

        assertThat(offenceEntities).hasSize(arrayParam.length);

        List<Integer> expected = IntStream.range(1, arrayParam.length+1).boxed().collect(Collectors.toList());
        List<Integer> actual = offenceEntities.stream()
            .map(OffenceEntity::getSequenceNumber)
            .collect(Collectors.toList());
        assertThat(expected).isEqualTo(actual);
    }

    private void mockForCaseForUpdate(CourtCaseEntity courtCaseEntity) {
        when(courtCaseRepository.findByCourtCodeAndCaseNo(COURT_CODE, CASE_NO)).thenReturn(Optional.ofNullable(courtCaseEntity));
        when(courtRepository.findByCourtCode(COURT_CODE)).thenReturn(Optional.of(courtEntity));
    }
}
