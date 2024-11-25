package uk.gov.justice.probation.courtcaseservice.jpa.entity;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.IdClass;
import jakarta.persistence.Table;
import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.ToString;
import lombok.With;
import lombok.experimental.SuperBuilder;

import java.io.Serializable;

@Schema(description = "Hearing Court Case")
@Entity
@AllArgsConstructor
@NoArgsConstructor(access = AccessLevel.PROTECTED, force = true)
@ToString(doNotUseGetters = true)
@Getter
@With
@Table(name = "HEARING_COURT_CASE")
@SuperBuilder
@IdClass(HearingCourtCaseId.class)
public class HearingCourtCaseEntity implements Serializable {
    @Id
    private String hearingId;

    @Id
    private final String caseId;
}
