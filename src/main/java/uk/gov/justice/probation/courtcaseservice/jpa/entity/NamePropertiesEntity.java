package uk.gov.justice.probation.courtcaseservice.jpa.entity;

import java.io.Serializable;
import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor(access = AccessLevel.PRIVATE, force = true)
@AllArgsConstructor
@JsonInclude(JsonInclude.Include.NON_NULL)
public class NamePropertiesEntity implements Serializable {

    private String title;
    private String forename1;
    private String forename2;
    private String forename3;
    private String surname;
}
