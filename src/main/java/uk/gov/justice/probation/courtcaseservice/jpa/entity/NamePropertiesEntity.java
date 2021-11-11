package uk.gov.justice.probation.courtcaseservice.jpa.entity;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serializable;
import java.util.Comparator;
import java.util.Objects;
import java.util.stream.Collectors;
import java.util.stream.Stream;

@Data
@Builder
@NoArgsConstructor(access = AccessLevel.PRIVATE, force = true)
@AllArgsConstructor
@JsonInclude(JsonInclude.Include.NON_NULL)
public class NamePropertiesEntity implements Serializable, Comparable<NamePropertiesEntity> {

    private String title;
    private String forename1;
    private String forename2;
    private String forename3;
    private String surname;

    @JsonIgnore
    public String getFullName() {
        return Stream.of(title, forename1, forename2, forename3, surname)
            .filter(Objects::nonNull)
            .collect(Collectors.joining(" "));
    }

    @Override
    public int compareTo(NamePropertiesEntity other) {
        return Comparator.comparing(NamePropertiesEntity::getSurname)
                .compare(this, other);
    }
}
