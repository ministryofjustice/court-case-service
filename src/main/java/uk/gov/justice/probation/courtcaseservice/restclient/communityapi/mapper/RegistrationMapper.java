package uk.gov.justice.probation.courtcaseservice.restclient.communityapi.mapper;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;
import org.springframework.util.StringUtils;
import uk.gov.justice.probation.courtcaseservice.restclient.communityapi.model.CommunityApiRegistrationResponse;
import uk.gov.justice.probation.courtcaseservice.restclient.communityapi.model.CommunityApiRegistrationsResponse;
import uk.gov.justice.probation.courtcaseservice.service.model.KeyValue;
import uk.gov.justice.probation.courtcaseservice.service.model.Registration;


public class RegistrationMapper {

    public static List<Registration> registrationsFrom(final CommunityApiRegistrationsResponse registrations) {

        return Optional.ofNullable(registrations.getRegistrations()).orElse(Collections.emptyList())
                            .stream()
                            .map(RegistrationMapper::buildRegistration)
                            .collect(Collectors.toList());
    }

    static Registration buildRegistration(final CommunityApiRegistrationResponse registration) {

        return Registration.builder()
            .active(registration.isActive())
            .notes(buildNotes(registration))
            .endDate(registration.getEndDate())
            .startDate(registration.getStartDate())
            .nextReviewDate(registration.getNextReviewDate())
            .type(Optional.ofNullable(registration.getType())
                .map(KeyValue::getDescription)
                .orElse(null))
            .build();
    }

    static List<String> buildNotes(CommunityApiRegistrationResponse registration) {
        if (StringUtils.isEmpty(registration.getNotes())) {
            return Collections.emptyList();
        }

        return Arrays.asList(registration.getNotes().split( "\\n"));
    }

}
