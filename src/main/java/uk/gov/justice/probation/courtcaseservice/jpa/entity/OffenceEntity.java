package uk.gov.justice.probation.courtcaseservice.jpa.entity;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.ToString;
import lombok.experimental.SuperBuilder;
import org.hibernate.annotations.LazyCollection;
import org.hibernate.annotations.LazyCollectionOption;
import org.hibernate.envers.Audited;
import javax.persistence.CascadeType;
import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.JoinColumn;
import javax.persistence.ManyToOne;
import javax.persistence.OneToMany;
import javax.persistence.OneToOne;
import javax.persistence.OrderColumn;
import javax.persistence.PrimaryKeyJoinColumn;
import javax.persistence.Table;
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
    @LazyCollection(value = LazyCollectionOption.FALSE)
    @JsonIgnore
    @OneToMany(mappedBy = "offence", orphanRemoval=true, cascade = CascadeType.ALL)
    @OrderColumn(name = "JUDICIAL_RESULTS_ORDER", nullable = false)
    @ToString.Exclude
    private final List<JudicialResultEntity> judicialResults;

    @Column(name = "OFFENCE_CODE")
    private final String offenceCode;

    @Column(name = "SHORT_TERM_CUSTODY_PREDICTOR_SCORE")
    @Setter
    private BigDecimal shortTermCustodyPredictorScore;

    @OneToOne(cascade = {CascadeType.ALL})
    @JoinColumn(name = "plea_id", referencedColumnName = "id")
    private PleaEntity plea;
}
