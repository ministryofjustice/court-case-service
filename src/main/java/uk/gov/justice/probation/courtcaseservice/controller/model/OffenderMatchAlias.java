package uk.gov.justice.probation.courtcaseservice.controller.model;

import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import uk.gov.justice.probation.courtcaseservice.jpa.entity.OffenderAliasEntity;

import java.time.LocalDate;
import java.util.List;

@Getter
@AllArgsConstructor
@Builder
@NoArgsConstructor(access = AccessLevel.PRIVATE, force = true)
@JsonInclude(JsonInclude.Include.NON_NULL)
public class OffenderMatchAlias {

    private final LocalDate dateOfBirth;
    private final String firstName;
    private final List<String> middleNames;
    private final String surname;
    private final String gender;

    public OffenderAliasEntity asEntity() {
        return OffenderAliasEntity.builder()
                .dateOfBirth(dateOfBirth)
                .firstName(firstName)
                .middleNames(middleNames)
                .surname(surname)
                .gender(gender)
            .build();
    }
}
