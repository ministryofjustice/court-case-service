package uk.gov.justice.probation.courtcaseservice.jpa.entity;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonProperty;
import jakarta.persistence.*;
import lombok.*;
import lombok.experimental.SuperBuilder;
import org.hibernate.annotations.LazyCollection;
import org.hibernate.annotations.LazyCollectionOption;
import org.hibernate.envers.Audited;

import java.io.Serializable;
import java.math.BigDecimal;
import java.util.List;

@Entity
@Table(name = "OFFENCE")
@AllArgsConstructor
@SuperBuilder
@NoArgsConstructor(access = AccessLevel.PROTECTED, force = true)
@Getter
@EqualsAndHashCode(callSuper = true, exclude = "hearingDefendant")
@Audited
public class OffenceEntity extends BaseAuditedEntity implements Serializable  {

    @Id
    @Column(name = "ID", updatable = false, nullable = false)
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @JsonIgnore
    private final Long id;

    @ManyToOne
    @JoinColumn(name = "FK_HEARING_DEFENDANT_ID", referencedColumnName = "id")
    @Setter
    @JsonIgnore
    @ToString.Exclude
    private HearingDefendantEntity hearingDefendant;

    @Column(name = "TITLE", nullable = false)
    private final String title;

    @Column(name = "SUMMARY", nullable = false, columnDefinition = "TEXT")
    private final String summary;

    @Column(name = "ACT")
    private final String act;

    @OrderColumn
    @Column(name = "SEQUENCE", nullable = false)
    @JsonProperty
    private final Integer sequence;

    @Column(name = "LIST_NO", nullable = false)
    private final Integer listNo;

    // Order column is managed by hibernate
    @JsonIgnore
    @OneToMany(mappedBy = "offence", orphanRemoval=true, cascade = CascadeType.ALL, fetch = FetchType.EAGER)
    @OrderColumn(name = "JUDICIAL_RESULTS_ORDER", nullable = false)
    @ToString.Exclude
    private final List<JudicialResultEntity> judicialResults;

    @Column(name = "OFFENCE_CODE")
    private final String offenceCode;

    @Column(name = "SHORT_TERM_CUSTODY_PREDICTOR_SCORE", scale = 19, precision = 21)
    @Setter
    private BigDecimal shortTermCustodyPredictorScore;

    @Column(name = "DATA_MODEL_VERSION")
    @Setter
    private String dataModelVersion;

    @OneToOne(cascade = {CascadeType.ALL})
    @JoinColumn(name = "plea_id", referencedColumnName = "id")
    private PleaEntity plea;

    @OneToOne(cascade = {CascadeType.ALL})
    @JoinColumn(name = "verdict_id", referencedColumnName = "id")
    private VerdictEntity verdict;
}
