package uk.gov.justice.probation.courtcaseservice.jpa.dto;

import com.fasterxml.jackson.annotation.JsonIgnore;
import jakarta.persistence.*;
import lombok.*;
import lombok.experimental.SuperBuilder;
import uk.gov.justice.probation.courtcaseservice.controller.model.HearingPrepStatus;
import uk.gov.justice.probation.courtcaseservice.jpa.entity.DefendantProbationStatus;

import java.util.List;

@Entity
@Table(name = "HEARING_DEFENDANT")
@AllArgsConstructor
@NoArgsConstructor(access = AccessLevel.PROTECTED, force = true)
@Getter
@SuperBuilder
@With
public class HearingDefendantDTO {
    @Id
    @Column(name = "id", updatable = false, nullable = false)
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @JsonIgnore
    private Long id;

    @Column(name = "DEFENDANT_ID", nullable = false)
    private final String defendantId;

    @Setter
    @ManyToOne(optional = false, fetch = FetchType.LAZY)
    @JoinColumn(name = "FK_DEFENDANT_ID", referencedColumnName = "id", nullable = false)
    private DefendantDTO defendant;

    @ToString.Exclude
    @OneToMany(mappedBy = "hearingDefendantDTO",fetch = FetchType.LAZY)
    @Setter
    private List<OffenceDTO> offences;

    @Setter
    @Column(name = "PREP_STATUS")
    private String prepStatus = HearingPrepStatus.NOT_STARTED.name();

    @ToString.Exclude
    @ManyToOne()
    @JoinColumn(name = "FK_HEARING_ID", referencedColumnName = "id")
    @Getter
    @Setter
    private HearingDTO hearing;

    @OneToOne(mappedBy = "hearingDefendant")
    private HearingOutcomeDTO hearingOutcome;

    @Setter
    @Builder.Default
    @Column(name = "OUTCOME_NOT_REQUIRED")
    private Boolean outcomeNotRequired = false;

    public String getCrn() {
        return defendant.getCrn();
    }

    public DefendantProbationStatus getProbationStatusForDisplay() {
        return defendant.getProbationStatusForDisplay();
    }
}