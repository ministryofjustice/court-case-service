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
import uk.gov.justice.probation.courtcaseservice.controller.model.DefendantOffender;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.EnumType;
import javax.persistence.Enumerated;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.Table;
import java.io.Serializable;
import java.time.LocalDate;

@Entity
@Table(name = "OFFENDER")
@AllArgsConstructor
@NoArgsConstructor(access = AccessLevel.PROTECTED, force = true)
@SuperBuilder
@With
@Setter
@Getter
@EqualsAndHashCode(callSuper = false)
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

}
