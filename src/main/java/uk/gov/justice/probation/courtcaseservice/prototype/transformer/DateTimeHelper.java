package uk.gov.justice.probation.courtlistservice.prototype.transformer;

import org.springframework.util.StringUtils;

import java.time.LocalDate;
import java.time.LocalTime;
import java.time.format.DateTimeFormatter;
import java.util.Optional;

public class DateTimeHelper {
    public static LocalDate asDate(String date) {
        return Optional
                .ofNullable(date)
                .filter(StringUtils::hasText)
                .map(aDate -> LocalDate.parse(aDate, DateTimeFormatter.ofPattern("dd/MM/yyy")))
                .orElse(null);
    }

    public static LocalTime asTime(String time) {
        return Optional
                .ofNullable(time)
                .filter(StringUtils::hasText)
                .map(LocalTime::parse)
                .orElse(null);
    }

}
