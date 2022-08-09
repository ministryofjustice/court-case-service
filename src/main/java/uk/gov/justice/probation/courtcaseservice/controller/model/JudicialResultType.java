package uk.gov.justice.probation.courtcaseservice.controller.model;

import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.With;
import uk.gov.justice.probation.courtcaseservice.jpa.entity.JudicialResultEntity;
import uk.gov.justice.probation.courtcaseservice.jpa.entity.JudicialResultTypeEntity;
import uk.gov.justice.probation.courtcaseservice.jpa.entity.PhoneNumberEntity;

import java.util.Optional;

@Data
@Builder
@With
@AllArgsConstructor
@NoArgsConstructor(access = AccessLevel.PRIVATE, force = true)
public class JudicialResultType {
    private String description;
    private String id;

    public static JudicialResultType of(final JudicialResultTypeEntity judicialResultTypeEntity) {
        return Optional.ofNullable(judicialResultTypeEntity).map(p -> builder()
                        .description(judicialResultTypeEntity.getDescription())
                        .id(judicialResultTypeEntity.getId())
                        .build())
                .orElse(null);
    }

    public JudicialResultTypeEntity asEntity() {
        return JudicialResultTypeEntity.builder()
                .description(description)
                .id(id)
                .build();
    }
}
