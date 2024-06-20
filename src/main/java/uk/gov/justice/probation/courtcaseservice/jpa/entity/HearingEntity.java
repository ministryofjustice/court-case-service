package uk.gov.justice.probation.courtcaseservice.jpa.entity;

import com.fasterxml.jackson.annotation.JsonIgnore;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.persistence.CascadeType;
import jakarta.persistence.Column;
import jakarta.persistence.ColumnResult;
import jakarta.persistence.Entity;
import jakarta.persistence.EntityResult;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.OneToMany;
import jakarta.persistence.OrderBy;
import jakarta.persistence.SqlResultSetMapping;
import jakarta.persistence.Table;
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
import org.hibernate.envers.Audited;

import java.io.Serializable;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.Collection;
import java.util.List;
import java.util.Objects;
import java.util.Optional;

@Schema(description = "Hearing")
@Entity
@AllArgsConstructor
@NoArgsConstructor(access = AccessLevel.PROTECTED, force = true)
@SQLDelete(sql = "UPDATE HEARING SET deleted = true WHERE ID = ? AND VERSION = ?")
@ToString(doNotUseGetters = true)
@Getter
@With
@Table(name = "HEARING")
@SuperBuilder
@Audited
@SqlResultSetMapping(
    name = "search_hearing_outcomes_custom",
    columns = { @ColumnResult(name = "hearing_day", type = LocalDate.class) },
    entities = {
        @EntityResult(entityClass = HearingDefendantEntity.class)
    }
)
public class HearingEntity extends BaseAuditedEntity implements Serializable {

    @Id
    @Column(name = "ID", updatable = false, nullable = false)
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @JsonIgnore
    private final Long id;

    @Column(name = "HEARING_ID", nullable = false)
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
    @OneToMany(mappedBy = "hearing", cascade = CascadeType.ALL, orphanRemoval = true)
    @OrderBy("day, time ASC")
    private final List<HearingDayEntity> hearingDays;

    @ToString.Exclude
    @LazyCollection(value = LazyCollectionOption.FALSE)
    @JsonIgnore
    @OneToMany(mappedBy = "hearing", orphanRemoval = true, cascade = CascadeType.ALL)
    private final List<HearingDefendantEntity> hearingDefendants;

    @Column(name = "first_created", insertable = false, updatable = false)
    private LocalDateTime firstCreated;

    @Column(name = "HEARING_EVENT_TYPE")
    @Enumerated(EnumType.STRING)
    @Audited
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
                        .filter(d -> defendantId.equals(d.getDefendantId()))
                        .findFirst()
                )
                .orElse(null);
    }

    public HearingEntity update(HearingEntity hearingUpdate) {
        this.listNo = hearingUpdate.listNo;
        this.hearingType = hearingUpdate.hearingType;
        this.hearingEventType = hearingUpdate.hearingEventType;

        this.courtCase.update(hearingUpdate.getCourtCase());
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
                .toList()
                .forEach(this::removeHearingDefendant);

        // update existing
        this.hearingDefendants.stream().filter(
                        hearingDefendantEntity -> Objects.nonNull(hearingUpdate.getHearingDefendant(hearingDefendantEntity.getDefendantId())))
                .forEach(hearingDefendantEntity -> hearingDefendantEntity.update(hearingUpdate.getHearingDefendant(hearingDefendantEntity.getDefendantId())));

        // add new hearing defendants
        hearingUpdate.hearingDefendants.stream().filter(
                        hearingDefendantEntityUpdate -> Objects.isNull(this.getHearingDefendant(hearingDefendantEntityUpdate.getDefendantId())))
                .toList()
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
