package uk.gov.justice.digital.probation.court.list.courtlistservice.service;

import com.google.common.collect.ImmutableMap;
import lombok.extern.slf4j.Slf4j;
import lombok.val;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.web.client.RestTemplateBuilder;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.util.DefaultUriBuilderFactory;
import uk.gov.justice.digital.probation.court.list.courtlistservice.data.api.CourtList;
import uk.gov.justice.digital.probation.court.list.courtlistservice.data.entity.CourtListType;
import uk.gov.justice.digital.probation.court.list.courtlistservice.transformer.SessionTransformer;

import java.time.LocalDate;
import java.util.Collections;
import java.util.Optional;

@Service
@Slf4j
public class CourtListService {
    private final RestTemplate restTemplate;
    private final SessionTransformer sessionTransformer;

    public CourtListService(RestTemplateBuilder restTemplateBuilder, SessionTransformer sessionTransformer, @Value("${crime.portal.base.url}")String baseUrl) {
        log.info("Crime portal API at {}", baseUrl);
        this.restTemplate = restTemplateBuilder.uriTemplateHandler(new DefaultUriBuilderFactory(baseUrl)).build();
        this.sessionTransformer = sessionTransformer;
    }

    public CourtList courtList(String court, LocalDate date) {
        val courtList = this.restTemplate.getForObject("/court/{court}/list?date={date}", CourtListType.class, ImmutableMap.of("court", court, "date", date));
        return CourtList
                .builder()
                .courtName(court)
                .sessions(Optional.ofNullable(courtList.getSessions()).map(sessionTransformer::toSessions).orElse(Collections.emptyList()))
                .build();

    }

    public CourtList allCourtLists() {
        val courtList = this.restTemplate.getForObject("/court/list", CourtListType.class);
        return CourtList
                .builder()
                .sessions(Optional.ofNullable(courtList.getSessions()).map(sessionTransformer::toSessions).orElse(Collections.emptyList()))
                .build();
    }


}
