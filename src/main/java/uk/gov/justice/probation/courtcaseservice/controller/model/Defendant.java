package uk.gov.justice.probation.courtcaseservice.controller.model;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;
import javax.validation.Valid;
import javax.validation.constraints.NotBlank;
import javax.validation.constraints.NotEmpty;
import javax.validation.constraints.NotNull;
import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import uk.gov.justice.probation.courtcaseservice.jpa.entity.DefendantEntity;
import uk.gov.justice.probation.courtcaseservice.jpa.entity.DefendantType;
import uk.gov.justice.probation.courtcaseservice.jpa.entity.NamePropertiesEntity;
import uk.gov.justice.probation.courtcaseservice.jpa.entity.Sex;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

@Data
@Builder
@NoArgsConstructor(access = AccessLevel.PRIVATE, force = true)
@AllArgsConstructor
@JsonInclude(JsonInclude.Include.NON_NULL)
public class Defendant {
    @NotBlank
    private final String defendantId;
    @NotNull
    private final NamePropertiesEntity name;
    private final LocalDate dateOfBirth;
    private final AddressRequestResponse address;
    private final String probationStatus;
    @NotNull
    private final DefendantType type;
    // Until CCM sends 'MALE' instead of 'M' we have to use a String here because this is a request and response object. May be null from LIBRA.
    private final String sex;
    private final String crn;
    private final String pnc;
    private final String cro;
    private final LocalDate previouslyKnownTerminationDate;
    private final Boolean suspendedSentenceOrder;
    private final Boolean breach;
    private final Boolean preSentenceActivity;
    private final Boolean awaitingPsr;
    private final PhoneNumber phoneNumber;

    @JsonProperty
    public String getSex() {
        return Optional.ofNullable(sex).map(s -> Sex.fromString(s).getName()).orElse(Sex.NOT_KNOWN.getName());
    }

    @Valid
    @NotEmpty
    private final List<OffenceRequestResponse> offences;

    public DefendantEntity asEntity() {
        // TODO
        return DefendantEntity.builder()
                .address(Optional.ofNullable(address)
                        .map(AddressRequestResponse::asEntity)
                        .orElse(null))
//                .offender(Optional.ofNullable(crn)
//                        .map(this::buildOffender)
//                        .orElse(null))
                .crn(crn)
                .dateOfBirth(dateOfBirth)
                .defendantName(Optional.ofNullable(name).map(NamePropertiesEntity::getFullName).orElse(null))
                .type(type)
//                .nationality1(nationality1)
//                .nationality2(nationality2)
                .name(name)
                .sex(Sex.fromString(sex))
                .defendantId(defendantId)
                .cro(cro)
                .pnc(pnc)
                .phoneNumber(Optional.ofNullable(phoneNumber).map(PhoneNumber::asEntity).orElse(null))
//                .offenderConfirmed(true/false)
                .build();
    }

    public static Defendant of(final DefendantEntity defendantEntity) {
        return Defendant.builder().build(); // TODO
    }
}
