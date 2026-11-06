package uk.gov.justice.probation.courtcaseservice.restclient.cpr;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatus;
import org.springframework.http.HttpStatusCode;
import org.springframework.stereotype.Component;
import org.springframework.web.reactive.function.client.ClientResponse;
import reactor.core.publisher.Mono;
import uk.gov.justice.probation.courtcaseservice.restclient.RestClientHelper;

@Component
@Slf4j
public class CprRestClient {

    private final RestClientHelper clientHelper;

    @Value("${cpr-service.common-platform-path:/person/commonplatform/%s}")
    private String commonPlatformPath;

    @Value("${cpr-service.libra-path:/person/libra/%s}")
    private String libraPath;

    public CprRestClient(
            @Qualifier("cprServiceClient") RestClientHelper clientHelper
    ) {
        this.clientHelper = clientHelper;
    }

    public Mono<CprDefendant> getByCommonPlatformId(
            String defendantId
    ) {
        return get(
                String.format(commonPlatformPath, defendantId),
                defendantId
        );
    }

    public Mono<CprDefendant> getByLibraId(String cId) {
        return get(
                String.format(libraPath, cId),
                cId
        );
    }

    private Mono<CprDefendant> get(
            String path,
            String identifier
    ) {
        return clientHelper.get(path)
                .retrieve()
                .onStatus(
                        status -> status.equals(HttpStatus.NOT_FOUND),
                        response -> Mono.error(
                                new CprRecordNotFoundException(identifier)
                        )
                )
                .onStatus(
                        HttpStatusCode::isError,
                        response -> handleError(response, identifier)
                )
                .bodyToMono(CprDefendant.class)
                .onErrorResume(
                        CprRecordNotFoundException.class,
                        exception -> Mono.empty()
                );
    }

    private Mono<? extends Throwable> handleError(
            ClientResponse response,
            String identifier
    ) {
        return response.bodyToMono(String.class)
                .defaultIfEmpty("")
                .flatMap(body -> Mono.error(
                        new CprClientException(
                                String.format(
                                        "CPR request failed for identifier %s " +
                                                "with status %s: %s",
                                        identifier,
                                        response.statusCode(),
                                        body
                                )
                        )
                ));
    }
}