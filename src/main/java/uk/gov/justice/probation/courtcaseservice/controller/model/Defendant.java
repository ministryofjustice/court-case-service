package uk.gov.justice.probation.courtcaseservice.controller.model;

import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import uk.gov.justice.probation.courtcaseservice.jpa.entity.DefendantType;
import uk.gov.justice.probation.courtcaseservice.jpa.entity.NamePropertiesEntity;

import java.time.LocalDate;
import java.util.List;

@Data
@Builder
@NoArgsConstructor(access = AccessLevel.PRIVATE, force = true)
@AllArgsConstructor
public class Defendant {
    private final String uuid;
    private final NamePropertiesEntity name;
    private final LocalDate dateOfBirth;
    private final AddressRequest address;
    private final String probationStatus;
    private final DefendantType type;
    private final String sex;
    private final String crn;
    private final String pnc;
    private final String cro;
    private final LocalDate previouslyKnownTerminationDate;
    private final Boolean suspendedSentenceOrder;
    private final Boolean breach;
    private final Boolean preSentenceActivity;
    private final Boolean awaitingPsr;
    private final List<OffenceRequest> offences;
}
