package uk.gov.justice.probation.courtcaseservice.controller.model;

import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import uk.gov.justice.probation.courtcaseservice.jpa.entity.PhoneNumberEntity;

import java.util.Optional;

@Data
@Builder
@NoArgsConstructor(access = AccessLevel.PRIVATE, force = true)
@AllArgsConstructor
@JsonInclude(JsonInclude.Include.NON_NULL)
public class PhoneNumber {
    private final String home;
    private final String mobile;
    private final String work;

    public static PhoneNumber of(final PhoneNumberEntity phoneNumberEntity) {
        return Optional.ofNullable(phoneNumberEntity).map(p -> builder()
                    .mobile(p.getMobile())
                    .work(p.getWork())
                    .home(p.getHome())
                    .build())
                .orElse(null);
    }

    public PhoneNumberEntity asEntity() {
        return PhoneNumberEntity.builder()
                    .work(work)
                    .mobile(mobile)
                    .home(home)
                    .build();
    }
}
