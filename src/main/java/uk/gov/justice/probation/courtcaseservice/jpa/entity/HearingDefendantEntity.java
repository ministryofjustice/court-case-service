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
import javax.persistence.Table;
import javax.persistence.Transient;
import java.io.Serializable;
import java.util.List;
import java.util.Optional;

@Entity
@Table(name = "HEARING_DEFENDANT")
@AllArgsConstructor
@NoArgsConstructor(access = AccessLevel.PROTECTED, force = true)
@SuperBuilder
@With
@Getter
@ToString
@EqualsAndHashCode(callSuper = true, exclude = "hearing")
@Audited
public class HearingDefendantEntity extends BaseImmutableEntity implements Serializable {

    @Id
    @Column(name = "ID", updatable = false, nullable = false)
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @JsonIgnore
    private final Long id;

    @ToString.Exclude
    @ManyToOne()
    @JoinColumn(name = "FK_HEARING_ID", referencedColumnName = "id")
    @Setter
    private HearingEntity hearing;

    @Column(name = "DEFENDANT_ID", nullable = false)
    private final String defendantId;

    @ToString.Exclude
    @Transient
    @Setter
    private DefendantEntity defendant;

    @ToString.Exclude
    @LazyCollection(value = LazyCollectionOption.FALSE)
    @OneToMany(mappedBy = "hearingDefendant", cascade = CascadeType.ALL, orphanRemoval=true)
    private List<OffenceEntity> offences;

    public String getDefendantSurname() {
        return Optional.ofNullable(defendant)
                .map(DefendantEntity::getDefendantSurname)
                .orElse("");
    }

    public String getCrn() {
        return defendant.getCrn();
    }

    public DefendantProbationStatus getProbationStatusForDisplay() {
        return defendant.getProbationStatusForDisplay();
    }

    public void update(HearingDefendantEntity hearingDefendant) {
        this.offences.forEach(offenceEntity -> offenceEntity.setHearingDefendant(null));
        this.offences.clear();

        this.offences.addAll(hearingDefendant.getOffences());
        this.offences.forEach(offenceEntity -> offenceEntity.setHearingDefendant(this));

        this.defendant = hearingDefendant.getDefendant();
    }
}
