package uk.gov.justice.probation.courtcaseservice.jpa.entity;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.vladmihalcea.hibernate.type.json.JsonBinaryType;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.ToString;
import lombok.With;
import lombok.experimental.SuperBuilder;
import org.hibernate.annotations.LazyCollection;
import org.hibernate.annotations.LazyCollectionOption;
import org.hibernate.annotations.SQLDelete;
import org.hibernate.annotations.TypeDef;

import javax.persistence.CascadeType;
import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.EnumType;
import javax.persistence.Enumerated;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.JoinColumn;
import javax.persistence.ManyToOne;
import javax.persistence.OneToMany;
import javax.persistence.OrderBy;
import javax.persistence.Table;
import java.io.Serializable;
import java.time.LocalDateTime;
import java.util.Collection;
import java.util.List;
import java.util.Objects;
import java.util.Optional;
import java.util.stream.Collectors;

@Schema(description = "Hearing")
@Entity
@AllArgsConstructor
@NoArgsConstructor(access = AccessLevel.PROTECTED, force = true)
@SQLDelete(sql = "UPDATE HEARING SET deleted = true WHERE ID = ? AND VERSION = ?")
@ToString(doNotUseGetters = true)
@Getter
@With
@Table(name = "HEARING")
@TypeDef(name = "jsonb", typeClass = JsonBinaryType.class)
@SuperBuilder
public class HearingEntity extends BaseImmutableEntity implements Serializable {

    @Id
    @Column(name = "ID", updatable = false, nullable = false)
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @JsonIgnore
    private final Long id;

    @Column(name = "HEARING_ID", nullable = false)
    @Setter // TODO: This was added to enable ImmutableCourtCaseService.enforceValidHearingId() as a precautionary measure and should be removed ASAP
    private String hearingId;

    @ManyToOne(optional = false, cascade = CascadeType.ALL)
    @JoinColumn(name = "FK_COURT_CASE_ID", referencedColumnName = "id", nullable = false)
    @Setter
    private CourtCaseEntity courtCase;

    // If you have more than one collection with fetch = FetchType.EAGER then there is an exception
    // org.hibernate.loader.MultipleBagFetchException: cannot simultaneously fetch multiple bags
    // After CP integration, we will need to look at session boundaries @LazyCollection is one solution
    @ToString.Exclude
    @LazyCollection(value = LazyCollectionOption.FALSE)
    @JsonIgnore
    @OneToMany(mappedBy = "hearing", cascade = CascadeType.ALL, orphanRemoval=true)
    @OrderBy("day, time ASC")
    private final List<HearingDayEntity> hearingDays;

    @ToString.Exclude
    @LazyCollection(value = LazyCollectionOption.FALSE)
    @JsonIgnore
    @OneToMany(mappedBy = "hearing", orphanRemoval=true, cascade = CascadeType.ALL)
    private final List<HearingDefendantEntity> hearingDefendants;

    @Column(name = "deleted", nullable = false, updatable = false)
    private boolean deleted;

    @Column(name = "first_created", insertable = false, updatable = false)
    private LocalDateTime firstCreated;

    @Column(name = "HEARING_EVENT_TYPE")
    @Enumerated(EnumType.STRING)
    private HearingEventType hearingEventType;

    @Column(name = "HEARING_TYPE")
    private String hearingType;

    @Column(name = "LIST_NO")
    private String listNo;

    public String getCaseId() {
        return courtCase.getCaseId();
    }

    public String getCaseNo() {
        return courtCase.getCaseNo();
    }

    public SourceType getSourceType() {
        return courtCase.getSourceType();
    }

    public HearingDefendantEntity getHearingDefendant(String defendantId) {
        return Optional.ofNullable(hearingDefendants)
                .map(Collection::stream)
                .flatMap(defendantEntityStream -> defendantEntityStream
                    .filter(d ->  defendantId.equals(d.getDefendantId()))
                    .findFirst()
                )
                .orElse(null);
    }

    public HearingEntity update(HearingEntity hearingUpdate) {
        this.listNo = hearingUpdate.listNo;
        this.hearingType = hearingUpdate.hearingType;
        this.hearingEventType = hearingUpdate.hearingEventType;

        updateHearingDays(hearingUpdate);
        updateHearingDefendant(hearingUpdate);

        return this;
    }

    private void updateHearingDays(HearingEntity hearingUpdate) {
        this.hearingDays.forEach(hearingDay -> hearingDay.setHearing(null));
        this.hearingDays.clear();

        this.hearingDays.addAll(hearingUpdate.getHearingDays());
        this.hearingDays.forEach(hearingDay -> hearingDay.setHearing(this));
    }

    private void updateHearingDefendant(HearingEntity hearingUpdate) {
        // remove hearing defendants that are not on the hearing update
        this.hearingDefendants.stream().filter(
            hearingDefendantEntity -> Objects.isNull(hearingUpdate.getHearingDefendant(hearingDefendantEntity.getDefendantId())))
            .collect(Collectors.toList())
            .forEach(this::removeHearingDefendant);

        // add new hearing defendants
        hearingUpdate.hearingDefendants.stream().filter(
            hearingDefendantEntityUpdate -> Objects.isNull(this.getHearingDefendant(hearingDefendantEntityUpdate.getDefendantId())))
            .collect(Collectors.toList())
            .forEach(this::addHearingDefendant);
    }

    private void addHearingDefendant(HearingDefendantEntity hearingDefendantEntity) {
        hearingDefendantEntity.setHearing(this);
        this.hearingDefendants.add(hearingDefendantEntity);
    }

    private void removeHearingDefendant(HearingDefendantEntity toRemove) {
        this.hearingDefendants.remove(toRemove);
        toRemove.setHearing(null);
    }
}
