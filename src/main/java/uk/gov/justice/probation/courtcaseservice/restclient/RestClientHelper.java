package uk.gov.justice.probation.courtcaseservice.restclient;

import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.HttpStatusCode;
import org.springframework.http.MediaType;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;
import org.springframework.web.reactive.function.client.ClientResponse;
import org.springframework.web.reactive.function.client.WebClient;
import org.springframework.web.reactive.function.client.WebClient.RequestHeadersSpec;
import org.springframework.web.reactive.function.client.WebClientResponseException;
import reactor.core.publisher.Mono;
import uk.gov.justice.probation.courtcaseservice.restclient.communityapi.CommunityApiError;
import uk.gov.justice.probation.courtcaseservice.restclient.exception.ConvictionNotFoundException;
import uk.gov.justice.probation.courtcaseservice.restclient.exception.ForbiddenException;
import uk.gov.justice.probation.courtcaseservice.restclient.exception.NsiNotFoundException;
import uk.gov.justice.probation.courtcaseservice.restclient.exception.OffenderNotFoundException;

import java.nio.charset.StandardCharsets;

import static org.springframework.security.oauth2.client.web.reactive.function.client.ServletOAuth2AuthorizedClientExchangeFilterFunction.clientRegistrationId;

@Slf4j
@AllArgsConstructor
public class RestClientHelper {
    private WebClient client;
    private String oauthClient;
    private Boolean disableAuthentication;

    public WebClient.RequestHeadersSpec<?> get(final String path) {
        return get(path, new LinkedMultiValueMap<>(0));
    }

    public WebClient.RequestHeadersSpec<?> get(final String path, final MultiValueMap<String, String> queryParams) {
        final WebClient.RequestHeadersSpec<?> spec = client
            .get()
            .uri(uriBuilder -> uriBuilder
                .path(path)
                .queryParams(queryParams)
                .build()
            )
            .accept(MediaType.APPLICATION_JSON);

        return addSpecAuthAttribute(spec, path);
    }

    public WebClient.RequestHeadersSpec<?> get(final String path, final MediaType mediaType) {
        final WebClient.RequestHeadersSpec<?> spec = client
            .get()
            .uri(uriBuilder -> uriBuilder
                .path(path)
                .build()
            )
            .accept(mediaType);

        return addSpecAuthAttribute(spec, path);
    }

    private RequestHeadersSpec<?> addSpecAuthAttribute(RequestHeadersSpec<?> spec, String path) {
        if (disableAuthentication) {
            log.info(String.format("Skipping authentication with community api for call to %s", path));
            return spec;
        }

        log.info(String.format("Authenticating with %s for call to %s", oauthClient, path));
        return spec.attributes(clientRegistrationId(oauthClient));
    }

    public Mono<? extends Throwable> handleOffenderError(final String crn, final ClientResponse clientResponse) {
        if (HttpStatus.NOT_FOUND.equals(clientResponse.statusCode())) {
            return Mono.error(new OffenderNotFoundException(crn));
        }
        if (HttpStatus.FORBIDDEN.equals(clientResponse.statusCode())) {
            return clientResponse.bodyToMono(CommunityApiError.class)
                    .flatMap((error) -> Mono.error(new ForbiddenException(error.getDeveloperMessage())));
        }
        return handleError(clientResponse);
    }

    public Mono<? extends Throwable> handleConvictionError(final String crn, Long convictionId, final ClientResponse clientResponse) {
        if (HttpStatus.NOT_FOUND.equals(clientResponse.statusCode())) {
            return Mono.error(new ConvictionNotFoundException(crn, convictionId));
        }
        return handleError(clientResponse);
    }

    public Mono<? extends Throwable> handleNsiError(String crn, Long convictionId, final Long nsiId, final ClientResponse clientResponse) {
        if (HttpStatus.NOT_FOUND.equals(clientResponse.statusCode())) {
            return Mono.error(new NsiNotFoundException(crn, convictionId, nsiId));
        }
        return handleError(clientResponse);
    }

    private Mono<? extends Throwable> handleError(ClientResponse clientResponse) {
        final HttpStatusCode httpStatusCode = clientResponse.statusCode();

        return Mono.error(WebClientResponseException.create(httpStatusCode.value(),
                httpStatusCode.toString(),
                clientResponse.headers().asHttpHeaders(),
                clientResponse.toString().getBytes(),
                StandardCharsets.UTF_8));
    }
}
