package uk.gov.justice.probation.courtcaseservice.jpa.entity;

import com.fasterxml.jackson.annotation.JsonIgnore;
import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.ToString;
import lombok.With;
import lombok.experimental.SuperBuilder;
import org.apache.commons.lang3.StringUtils;
import org.hibernate.annotations.LazyCollection;
import org.hibernate.annotations.LazyCollectionOption;
import org.hibernate.envers.Audited;
import org.hibernate.envers.NotAudited;
import uk.gov.justice.probation.courtcaseservice.controller.model.HearingOutcomeItemState;
import uk.gov.justice.probation.courtcaseservice.service.HearingOutcomeType;

import java.io.Serializable;
import java.time.LocalDateTime;
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

    @Setter
    @Column(name = "PREP_STATUS", nullable = false)
    private String prepStatus;

    @ToString.Exclude
    @LazyCollection(value = LazyCollectionOption.FALSE)
    @OneToMany(mappedBy = "hearingDefendant", cascade = CascadeType.ALL, orphanRemoval=true)
    private List<OffenceEntity> offences;

    @ToString.Exclude
    @OneToMany(mappedBy = "hearingDefendant", cascade = CascadeType.ALL, orphanRemoval=true, fetch = FetchType.EAGER)
    @NotAudited
    private List<HearingNoteEntity> notes;

    @OneToOne(mappedBy = "hearingDefendant", cascade = CascadeType.ALL)
    private HearingOutcomeEntity hearingOutcome;

    public String getDefendantSurname() {
        return Optional.ofNullable(defendant)
                .map(DefendantEntity::getDefendantSurname)
                .orElse("");
    }

    public void addHearingOutcome(HearingOutcomeType hearingOutcomeType) {
        this.hearingOutcome = HearingOutcomeEntity.builder().outcomeType(hearingOutcomeType.name())
            .state(HearingOutcomeItemState.NEW.name())
            .outcomeDate(LocalDateTime.now())
            .hearingDefendant(this)
            .build();
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

    public Optional<HearingNoteEntity> getHearingNoteDraft(String createdByUuid) {
        return this.notes.stream().filter(hearingNoteEntity -> StringUtils.equals(hearingNoteEntity.getCreatedByUuid(), createdByUuid) && hearingNoteEntity.isDraft()).findFirst();
    }

    public HearingNoteEntity addHearingNote(HearingNoteEntity hearingNoteEntity) {

        hearingNoteEntity.setHearingDefendant(this);
        this.notes.add(hearingNoteEntity);
        return hearingNoteEntity;
    }
}
