package uk.gov.justice.probation.courtcaseservice.controller.mapper;

import lombok.experimental.UtilityClass;
import uk.gov.justice.probation.courtcaseservice.controller.model.PhoneNumber;
import uk.gov.justice.probation.courtcaseservice.jpa.entity.PhoneNumberEntity;

import java.util.Optional;

@UtilityClass
public class PhoneNumberMapper {

    public static PhoneNumber mapFrom(final PhoneNumberEntity phoneNumberEntity) {
        return Optional.ofNullable(phoneNumberEntity).map(p -> PhoneNumber.builder()
                    .mobile(p.getMobile())
                    .work(p.getWork())
                    .home(p.getHome())
                    .build())
                .orElse(null);
    }

    public static PhoneNumberEntity mapFrom(final PhoneNumber phoneNumber) {
        return Optional.ofNullable(phoneNumber).map(p -> PhoneNumberEntity.builder()
                    .work(p.getWork())
                    .mobile(p.getMobile())
                    .home(p.getHome())
                    .build())
                .orElse(null);
    }

}
