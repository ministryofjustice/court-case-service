package uk.gov.justice.probation.courtcaseservice.jpa.entity;

import com.fasterxml.jackson.annotation.JsonIgnore;
import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.ToString;
import lombok.With;
import lombok.experimental.SuperBuilder;
import org.hibernate.annotations.LazyCollection;
import org.hibernate.annotations.LazyCollectionOption;
import uk.gov.justice.probation.courtcaseservice.service.model.ProbationStatusDetail;

import jakarta.persistence.CascadeType;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.OneToMany;
import jakarta.persistence.Table;
import java.io.Serializable;
import java.time.LocalDate;
import java.util.List;

@Entity
@Table(name = "OFFENDER")
@AllArgsConstructor
@NoArgsConstructor(access = AccessLevel.PROTECTED, force = true)
@SuperBuilder
@With
@Setter
@Getter
@EqualsAndHashCode(callSuper = false, exclude = {"defendants"})
@ToString
public class OffenderEntity extends BaseEntity implements Serializable {

    @Id
    @Column(name = "ID", updatable = false, nullable = false)
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @JsonIgnore
    private Long id;

    @Column(name = "CRN", unique = true, nullable = false, updatable = false)
    private String crn;

    @Column(name = "PNC")
    private String pnc;

    @Column(name = "CRO")
    private String cro;

    @ToString.Exclude
    @LazyCollection(LazyCollectionOption.TRUE)
    @JsonIgnore
    @OneToMany(mappedBy = "offender", cascade = CascadeType.ALL)
    private List<DefendantEntity> defendants;

    @Column(name = "PROBATION_STATUS")
    @Enumerated(EnumType.STRING)
    private OffenderProbationStatus probationStatus;

    @Column(name = "AWAITING_PSR")
    private Boolean awaitingPsr;

    @Column(name = "BREACH", nullable = false)
    private boolean breach = false;

    @Column(name = "PRE_SENTENCE_ACTIVITY", nullable = false)
    private boolean preSentenceActivity = false;

    @Column(name = "SUSPENDED_SENTENCE_ORDER", nullable = false)
    private boolean suspendedSentenceOrder = false;

    @Column(name = "PREVIOUSLY_KNOWN_TERMINATION_DATE")
    private LocalDate previouslyKnownTerminationDate;

    public ProbationStatusDetail getProbationStatusDetail() {
        return ProbationStatusDetail.builder()
                .status(getProbationStatus().getName())
                .previouslyKnownTerminationDate(getPreviouslyKnownTerminationDate())
                .inBreach(isBreach())
                .preSentenceActivity(isPreSentenceActivity())
                .awaitingPsr(getAwaitingPsr())
                .build();
    }

    public void update(OffenderEntity updatedOffender) {
        this.cro = updatedOffender.getCro();
        this.pnc = updatedOffender.getPnc();
        this.breach = updatedOffender.isBreach();
        this.awaitingPsr = updatedOffender.getAwaitingPsr();
        this.probationStatus = updatedOffender.getProbationStatus();
        this.previouslyKnownTerminationDate = updatedOffender.getPreviouslyKnownTerminationDate();
        this.suspendedSentenceOrder = updatedOffender.isSuspendedSentenceOrder();
        this.preSentenceActivity = updatedOffender.isPreSentenceActivity();
    }
}
