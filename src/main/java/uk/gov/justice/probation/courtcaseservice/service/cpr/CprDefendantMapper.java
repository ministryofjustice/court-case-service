package uk.gov.justice.probation.courtcaseservice.service.cpr;

import org.springframework.stereotype.Component;
import uk.gov.justice.probation.courtcaseservice.jpa.entity.AddressPropertiesEntity;
import uk.gov.justice.probation.courtcaseservice.jpa.entity.DefendantEntity;
import uk.gov.justice.probation.courtcaseservice.jpa.entity.NamePropertiesEntity;
import uk.gov.justice.probation.courtcaseservice.jpa.entity.Sex;
import uk.gov.justice.probation.courtcaseservice.restclient.cpr.CprAddress;
import uk.gov.justice.probation.courtcaseservice.restclient.cpr.CprDefendant;
import uk.gov.justice.probation.courtcaseservice.restclient.cpr.CprDescription;

import java.time.LocalDate;
import java.util.Optional;

@Component
public class CprDefendantMapper {

    public void map(
            CprDefendant source,
            DefendantEntity target
    ) {
        DefendantEntity update = target
                .withName(NamePropertiesEntity.builder()
                        .title(description(source.getTitle()))
                        .forename1(source.getFirstName())
                        .forename2(source.getMiddleNames())
                        .surname(source.getLastName())
                        .build())
                .withDefendantName(buildDefendantName(source))
                .withDateOfBirth(parseDate(source.getDateOfBirth()))
                .withSex(mapSex(source.getSex()));

        mapLatestAddress(source)
                .ifPresent(address ->
                        update = update.withAddress(address)
                );

        target.update(update);
    }

    private String buildDefendantName(CprDefendant source) {
        return String.join(
                        " ",
                        Optional.ofNullable(source.getFirstName())
                                .orElse(""),
                        Optional.ofNullable(source.getMiddleNames())
                                .orElse(""),
                        Optional.ofNullable(source.getLastName())
                                .orElse("")
                )
                .trim()
                .replaceAll("\\s+", " ");
    }

    private LocalDate parseDate(String dateOfBirth) {
        return dateOfBirth == null
                ? null
                : LocalDate.parse(dateOfBirth);
    }

    private String description(CprDescription value) {
        return value == null
                ? null
                : value.getDescription();
    }

    private Sex mapSex(CprDescription value) {
        if (value == null || value.getDescription() == null) {
            return null;
        }

        try {
            return Sex.valueOf(
                    value.getDescription().toUpperCase()
            );
        } catch (IllegalArgumentException exception) {
            return null;
        }
    }

    private Optional<AddressPropertiesEntity> mapLatestAddress(
            CprDefendant source
    ) {
        return source.getAddresses()
                .stream()
                .filter(address ->
                        address.getEndDate() == null
                                || address.getEndDate().isBlank()
                )
                .findFirst()
                .map(this::mapAddress);
    }

    private AddressPropertiesEntity mapAddress(
            CprAddress address
    ) {
        return AddressPropertiesEntity.builder()
                .line1(address.getBuildingName())
                .line2(address.getBuildingNumber())
                .line3(address.getThoroughfareName())
                .line4(address.getDependentLocality())
                .line5(address.getPostTown())
                .postcode(address.getPostcode())
                .build();
    }
}