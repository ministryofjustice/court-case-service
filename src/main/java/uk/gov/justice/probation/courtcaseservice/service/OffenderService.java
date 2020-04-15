package uk.gov.justice.probation.courtcaseservice.service;

import java.util.Collections;
import java.util.List;
import java.util.concurrent.ConcurrentMap;
import java.util.function.Predicate;
import java.util.stream.Collectors;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Mono;
import reactor.util.function.Tuple3;
import uk.gov.justice.probation.courtcaseservice.restclient.OffenderRestClient;
import uk.gov.justice.probation.courtcaseservice.restclient.exception.OffenderNotFoundException;
import uk.gov.justice.probation.courtcaseservice.service.model.Conviction;
import uk.gov.justice.probation.courtcaseservice.service.model.Offender;
import uk.gov.justice.probation.courtcaseservice.service.model.Requirement;
import uk.gov.justice.probation.courtcaseservice.service.model.document.ConvictionDocuments;
import uk.gov.justice.probation.courtcaseservice.service.model.document.GroupedDocuments;
import uk.gov.justice.probation.courtcaseservice.service.model.document.OffenderDocumentDetail;

@Service
@Slf4j
public class OffenderService {

    private final OffenderRestClient client;

    private final Predicate<OffenderDocumentDetail> documentTypeFilter;

    public OffenderService(final OffenderRestClient client, final DocumentTypeFilter documentTypeFilter) {
        this.client = client;
        this.documentTypeFilter = documentTypeFilter;
    }

    public Offender getOffender(String crn, boolean applyDocumentFilter) {

        Tuple3<Offender, List<Conviction>, GroupedDocuments> tuple3 = Mono.zip(client.getOffenderByCrn(crn), client.getConvictionsByCrn(crn), client.getDocumentsByCrn(crn))
            .blockOptional()
            .orElseThrow(() -> new OffenderNotFoundException(crn));

        Offender offender = combineOffenderAndConvictions(tuple3.getT1(), tuple3.getT2());
        combineConvictionsAndDocuments(offender, tuple3.getT3().getConvictions(), applyDocumentFilter);

        return offender;
    }

    private void combineConvictionsAndDocuments(final Offender offender, final List<ConvictionDocuments> convictionDocuments, boolean applyDocumentFilter) {

        final ConcurrentMap<String, List<OffenderDocumentDetail>> allConvictionDocuments;
        allConvictionDocuments = convictionDocuments.stream()
                                .map(convictionDocument -> ConvictionDocuments.builder()
                                                                .convictionId(convictionDocument.getConvictionId())
                                                                .documents(convictionDocument.getDocuments().stream()
                                                                            .filter(doc -> (!applyDocumentFilter || documentTypeFilter.test(doc)))
                                                                            .collect(Collectors.toList()))
                                                                .build())
                                .collect(Collectors.toConcurrentMap(ConvictionDocuments::getConvictionId, ConvictionDocuments::getDocuments));

        offender.getConvictions()
            .forEach((conviction) -> {
                final String convictionId = conviction.getConvictionId();
                conviction.setDocuments(allConvictionDocuments.getOrDefault(convictionId, Collections.emptyList()));
            });
    }

    private Offender combineOffenderAndConvictions(Offender offender, List<Conviction> convictions) {
        offender.setConvictions(convictions);
        return offender;
    }

    public List<Requirement> getConvictionRequirements(String crn, String convictionId) {
        return client.getConvictionRequirements(crn, convictionId).block();
    }

}
