package uk.gov.justice.probation.courtcaseservice.jpa.entity;

import com.fasterxml.jackson.annotation.JsonIgnore;
import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.experimental.SuperBuilder;
import org.hibernate.envers.Audited;
import uk.gov.justice.probation.courtcaseservice.controller.model.HearingOutcomeItemState;
import uk.gov.justice.probation.courtcaseservice.service.HearingOutcomeType;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.OneToOne;
import javax.persistence.Table;
import java.io.Serializable;
import java.time.LocalDateTime;

@Entity
@Table(name = "HEARING_OUTCOME")
@AllArgsConstructor
@SuperBuilder
@NoArgsConstructor(access = AccessLevel.PROTECTED, force = true)
@Getter
@Audited
public class HearingOutcomeEntity extends BaseAuditedEntity implements Serializable {

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
    @Column(name = "STATE")
    private String state;

    @Column(name = "ASSIGNED_TO")
    private String assignedTo;

    @Column(name = "ASSIGNED_TO_UUID")
    private String assignedToUuid;

    @OneToOne(mappedBy = "hearingOutcome")
    private HearingEntity hearing;

    public void update(HearingOutcomeType hearingOutcomeType) {
        this.setOutcomeDate(LocalDateTime.now());
        this.setOutcomeType(hearingOutcomeType.name());
    }

    public void assignTo(String hearingOutcomeAssignedTo, String hearingOutcomeAssignedToUUID) {
        this.assignedTo = hearingOutcomeAssignedTo;
        this.assignedToUuid = hearingOutcomeAssignedToUUID;
        this.state = HearingOutcomeItemState.IN_PROGRESS.toString();
    }
}
