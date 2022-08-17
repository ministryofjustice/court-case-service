package uk.gov.justice.probation.courtcaseservice.service.model.event;

import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import javax.validation.constraints.NotBlank;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor(access = AccessLevel.PRIVATE, force = true)
public class EventMessage {
    @NotBlank
    private final String eventType;
    @NotBlank
    private int version;
    private String description;
    @NotBlank
    private final String detailUrl;
    @NotBlank
    private final String occurredAt;
    private final String additionalInformation;
    private final String personReference;

}
