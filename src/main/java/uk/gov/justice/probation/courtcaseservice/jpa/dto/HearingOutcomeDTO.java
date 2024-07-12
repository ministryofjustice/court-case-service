package uk.gov.justice.probation.courtcaseservice.jpa.dto;

import com.fasterxml.jackson.annotation.JsonIgnore;
import jakarta.persistence.*;
import lombok.*;
import lombok.experimental.SuperBuilder;
import uk.gov.justice.probation.courtcaseservice.jpa.entity.BaseAuditedEntity;

import java.io.Serializable;
import java.time.LocalDateTime;

@Entity
@Table(name = "HEARING_OUTCOME")
@AllArgsConstructor
@SuperBuilder
@NoArgsConstructor(access = AccessLevel.PROTECTED, force = true)
@Getter
public class HearingOutcomeDTO extends BaseAuditedEntity implements Serializable  {
    @Id
    @Column(name = "ID", updatable = false, nullable = false)
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @JsonIgnore
    private final Long id;

    @Setter
    @Column(name = "OUTCOME_TYPE")
    private String outcomeType;

    @Setter
    @Column(name = "OUTCOME_DATE")
    private LocalDateTime outcomeDate;

    @Setter
    @Column(name = "RESULTED_DATE")
    private LocalDateTime resultedDate;

    @Setter
    @Column(name = "STATE")
    private String state;

    @Column(name = "ASSIGNED_TO")
    private String assignedTo;

    @Column(name = "LEGACY")
    private boolean legacy;

    @Column(name = "ASSIGNED_TO_UUID")
    private String assignedToUuid;

    @OneToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "FK_HEARING_DEFENDANT_ID", referencedColumnName = "id")
    private HearingDefendantDTO hearingDefendant;
}