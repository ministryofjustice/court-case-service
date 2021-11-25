package uk.gov.justice.probation.courtcaseservice.jpa.entity;

import java.io.Serializable;
import java.time.LocalDate;
import com.fasterxml.jackson.annotation.JsonIgnore;
import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.Table;
import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.ToString;
import lombok.With;
import lombok.experimental.SuperBuilder;

@Entity
@Table(name = "OFFENDER")
@AllArgsConstructor
@NoArgsConstructor(access = AccessLevel.PROTECTED, force = true)
@SuperBuilder
@With
@Setter
@Getter
@EqualsAndHashCode(onlyExplicitlyIncluded = true, callSuper = false)
@ToString
public class OffenderEntity extends BaseEntity implements Serializable {

    @Id
    @Column(name = "ID", updatable = false, nullable = false)
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @JsonIgnore
    private Long id;

    @EqualsAndHashCode.Include
    @Column(name = "CRN", unique = true, nullable = false)
    private String crn;

    @Column(name = "PROBATION_STATUS")
    private String probationStatus;

    @Column(name = "AWAITING_PSR")
    private Boolean awaitingPsr;

    @Column(name = "BREACH", nullable = false)
    private Boolean breach;

    @Column(name = "PRE_SENTENCE_ACTIVITY", nullable = false)
    private Boolean preSentenceActivity;

    @Column(name = "SUSPENDED_SENTENCE_ORDER", nullable = false)
    private Boolean suspendedSentenceOrder;

    @Column(name = "PREVIOUSLY_KNOWN_TERMINATION_DATE")
    private LocalDate previouslyKnownTerminationDate;

}