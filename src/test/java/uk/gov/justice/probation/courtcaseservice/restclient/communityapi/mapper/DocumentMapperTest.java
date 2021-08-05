package uk.gov.justice.probation.courtcaseservice.restclient.communityapi.mapper;

import static java.time.Month.FEBRUARY;
import static java.time.Month.SEPTEMBER;
import static org.assertj.core.api.Assertions.assertThat;
import static uk.gov.justice.probation.courtcaseservice.service.model.document.DocumentType.COURT_REPORT_DOCUMENT;
import static uk.gov.justice.probation.courtcaseservice.service.model.document.DocumentType.PRECONS_DOCUMENT;

import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import java.io.File;
import java.io.IOException;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import uk.gov.justice.probation.courtcaseservice.restclient.communityapi.model.CommunityApiGroupedDocumentsResponse;
import uk.gov.justice.probation.courtcaseservice.service.model.KeyValue;
import uk.gov.justice.probation.courtcaseservice.service.model.document.OffenderDocumentDetail;
import uk.gov.justice.probation.courtcaseservice.service.model.document.ReportDocumentDates;

    class DocumentMapperTest {

    private static final ObjectMapper OBJECT_MAPPER = new ObjectMapper();

    private static final String BASE_MOCK_PATH = "src/test/resources/mocks/__files/";

    private DocumentMapper mapper;

    @BeforeAll
    public static void setUpBeforeClass() {
        OBJECT_MAPPER.configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false);
        OBJECT_MAPPER.registerModule(new JavaTimeModule());
    }

    @BeforeEach
    void setUp() {
        mapper = new DocumentMapper();
        mapper.setPsrTypeCodes(List.of("CJF", "CJO", "CJS", "PSR"));
    }

    @DisplayName("Prove that null input lists in the community API will be handled and create empty lists")
    @Test
    void shouldMapGroupedDocumentsWithNullsToEmptyDocumentLists() {

        final var response = new CommunityApiGroupedDocumentsResponse(null, null);

        final var groupedDocuments = mapper.documentsFrom(response);

        assertThat(groupedDocuments.getDocuments()).isEmpty();
        assertThat(groupedDocuments.getConvictions()).isEmpty();
    }

    @DisplayName("Maps grouped documents from community API to Court Case Service")
    @Test
    void shouldMapGroupedDocumentsToDocuments() throws IOException {

        final var response
            = OBJECT_MAPPER.readValue(new File(BASE_MOCK_PATH + "offender-documents/GET_documents_success_X320741.json"), CommunityApiGroupedDocumentsResponse.class);

        final var documentsResponse = mapper.documentsFrom(response);

        // Check sizes and one from each collection
        assertThat(documentsResponse.getDocuments()).hasSize(7);
        assertThat(documentsResponse.getConvictions()).hasSize(2);

        assertThat(documentsResponse.getDocuments())
            .extracting("documentId")
            .containsExactlyInAnyOrder("1e593ff6-d5d6-4048-a671-cdeb8f65608b", "0ec9b16c-b292-4d27-b11a-c0ddde852804",
                                    "aeb43e06-a4a1-460f-9acf-e2495de84604", "b2d92238-215c-4d6d-b91c-208ea747087e",
                                    "5152b060-9650-4f22-9974-038a38590d9f", "7ceda384-3624-4a62-849d-7c729b6d0dd1",
                                    "2b167448-31a5-45a5-85a5-dcdd4a783f1d");

        final var documentDetail = documentsResponse.getDocuments().stream()
                                                    .filter(doc -> doc.getDocumentId().equals("1e593ff6-d5d6-4048-a671-cdeb8f65608b"))
                                                    .findFirst().get();
        assertThat(documentDetail)
            .usingRecursiveComparison()
            .isEqualTo(OffenderDocumentDetail.builder()
                                        .documentId("1e593ff6-d5d6-4048-a671-cdeb8f65608b")
                                        .documentName("PRE-CONS.pdf")
                                        .author("Andy Marke")
                                        .type(PRECONS_DOCUMENT)
                                        .extendedDescription("Previous convictions as of 01/09/2019")
                                        .createdAt(LocalDateTime.of(2019, SEPTEMBER, 10, 0, 0, 0))
                                        .build());

        final var convictionDocuments1 = documentsResponse.getConvictions().stream()
            .filter(conviction -> conviction.getConvictionId().equals("2500295343"))
            .findFirst().get();
        assertThat(convictionDocuments1.getDocuments()).hasSize(7);
        assertThat(convictionDocuments1.getDocuments())
            .extracting("documentId")
            .containsExactlyInAnyOrder("cc8bf04c-2f8c-4e72-a14b-ab6a5702bf59", "ec450eca-cf81-420d-8712-873a5df2274b",
                "086bfb96-28a7-4b0a-80d5-7b877dd7bb75", "5058ca66-3751-4701-855a-86bf518d9392",
                "44f37749-18b8-46ff-803a-150746f6d1bc", "b88547cf-7464-4cbc-b5f9-ebe2bafc19d9",
                "4191eada-6e03-4bd8-b52d-9e050eefa745");

        final var convictionDocuments2 = documentsResponse.getConvictions().stream()
            .filter(conviction -> conviction.getConvictionId().equals("2500295345"))
            .findFirst().get();
        assertThat(convictionDocuments2.getDocuments()).hasSize(8);
        final var preSentenceReport = convictionDocuments2.getDocuments().stream()
            .filter(document -> document.getDocumentName().startsWith("shortFormatPreSentenceReport"))
            .findFirst().get();
        assertThat(preSentenceReport)
            .usingRecursiveComparison()
            .isEqualTo(OffenderDocumentDetail.builder()
                            .documentId("1d842fce-ec2d-45dc-ac9a-748d3076ca6b")
                            .documentName("shortFormatPreSentenceReport_04092019_121424_OMIC_A_X320741.pdf")
                            .author("Andy Marke")
                            .type(COURT_REPORT_DOCUMENT)
                            .extendedDescription("Pre-Sentence Report - Fast requested by Sheffield Crown Court on 04/09/2018")
                            .createdAt(LocalDateTime.of(2019, SEPTEMBER, 4, 0, 0, 0))
                            .subType(new KeyValue("CJF", "Pre-Sentence Report - Fast"))
                            .parentPrimaryKeyId(2500079873L)
                            .reportDocumentDates(ReportDocumentDates.builder()
                                .requestedDate(LocalDate.of(2018, SEPTEMBER, 4))
                                .requiredDate(LocalDate.of(2019, SEPTEMBER, 4))
                                .completedDate(LocalDateTime.of(2018, FEBRUARY, 28, 0, 0, 0))
                                .build())
                            .psr(true)
                            .build());
    }

}
