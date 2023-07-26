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

import javax.persistence.*;
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
public class HearingDefendantEntity extends BaseAuditedEntity implements Serializable {

    @Id
    @Column(name = "id", updatable = false, nullable = false)
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

    @Setter
    @ManyToOne(optional = false, cascade = { CascadeType.MERGE, CascadeType.PERSIST })
    @JoinColumn(name = "FK_DEFENDANT_ID", referencedColumnName = "id", nullable = false)
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

        this.defendant.update(hearingDefendant.getDefendant());
    }
}
