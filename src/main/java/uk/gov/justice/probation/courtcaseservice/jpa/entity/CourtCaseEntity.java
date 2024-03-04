package uk.gov.justice.probation.courtcaseservice.jpa.entity;

import com.fasterxml.jackson.annotation.JsonIgnore;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.ToString;
import lombok.With;
import lombok.experimental.SuperBuilder;
import org.apache.commons.lang3.StringUtils;
import org.hibernate.annotations.LazyCollection;
import org.hibernate.annotations.LazyCollectionOption;
import org.hibernate.annotations.SQLDelete;
import org.hibernate.envers.Audited;
import org.hibernate.envers.NotAudited;
import org.springframework.util.CollectionUtils;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.Optional;

@Schema(description = "Court Case")
@Entity
@AllArgsConstructor
@NoArgsConstructor(access = AccessLevel.PROTECTED, force = true)
@SQLDelete(sql = "UPDATE COURT_CASE SET deleted = true WHERE ID = ? AND VERSION = ?")
@SuperBuilder
@ToString(doNotUseGetters = true)
@Getter
@With
@Table(name = "COURT_CASE")
@Audited
@SqlResultSetMapping(
    name = "search_defendants_result_mapping",
    entities = {
        @EntityResult(entityClass = CourtCaseEntity.class, discriminatorColumn = "case_id"),
        @EntityResult(entityClass = DefendantEntity.class, discriminatorColumn = "defendant_id")
    }
)
@SqlResultSetMapping(
    name = "search_hearings_custom",
    columns = { @ColumnResult(name = "match_count", type = Integer.class) },
    entities = {
        @EntityResult(entityClass = HearingDefendantEntity.class)
    }
)
public class CourtCaseEntity extends BaseAuditedEntity implements Serializable {

    @Id
    @Column(name = "ID", updatable = false, nullable = false)
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @JsonIgnore
    private final Long id;

    @Column(name = "CASE_ID", nullable = false)
    private final String caseId;

    @Column(name = "CASE_NO", nullable = false)
    private final String caseNo;

    @Column(name = "urn", nullable = false)
    private String urn;

    @ToString.Exclude
    @LazyCollection(LazyCollectionOption.FALSE)
    @JsonIgnore
    @OneToMany(mappedBy = "courtCase", cascade = CascadeType.ALL)
    private final List<HearingEntity> hearings;

    @ToString.Exclude
    @Transient
    @Setter
    private List<CaseCommentEntity> caseComments;

    @Column(name = "SOURCE_TYPE")
    @Enumerated(EnumType.STRING)
    private final SourceType sourceType;


    @ToString.Exclude
    @LazyCollection(value = LazyCollectionOption.FALSE)
    @JsonIgnore
    @OneToMany(mappedBy = "courtCase", orphanRemoval = true, cascade = CascadeType.ALL)
    private final List<CaseMarkerEntity> caseMarkers;

    @NotAudited
    @ToString.Exclude
    @LazyCollection(value = LazyCollectionOption.FALSE)
    @JsonIgnore
    @OneToMany(mappedBy = "courtCase", orphanRemoval = true, cascade = CascadeType.ALL)
    @Setter
    private List<CaseDefendantEntity> caseDefendants;

    public void update(CourtCaseEntity courtCaseUpdate) {
        if(!CollectionUtils.isEmpty(courtCaseUpdate.getCaseMarkers())) {
            updateCaseMarkers(courtCaseUpdate);
        }

        this.urn = courtCaseUpdate.getUrn();
    }

    public void addHearing(HearingEntity newHearing) {
        newHearing.setCourtCase(this);
        this.hearings.add(newHearing);
    }

    public void addCaseMarkers(List<CaseMarkerEntity> caseMarkers){
        caseMarkers.forEach(caseMarkerEntity -> caseMarkerEntity.setCourtCase(this));
        this.caseMarkers.addAll(caseMarkers);
    }

    private void updateCaseMarkers(CourtCaseEntity courtCaseEntity) {
            this.caseMarkers.forEach(caseMarkerEntity -> caseMarkerEntity.setCourtCase(null));
            this.caseMarkers.clear();

            this.caseMarkers.addAll(courtCaseEntity.getCaseMarkers());
            this.caseMarkers.forEach(caseMarkerEntity -> caseMarkerEntity.setCourtCase(this));
    }

    public Optional<CaseDefendantEntity> getCaseDefendant(String defendantId) {
        return getCaseDefendants().stream()
            .filter(caseDefendantEntity -> StringUtils.equals(defendantId, caseDefendantEntity.getDefendant().getDefendantId())).findFirst();
    }

    public CaseDefendantEntity getOrCreateCaseDefendant(DefendantEntity defendant) {
        var defendantId = defendant.getDefendantId();
        return getCaseDefendant(defendantId).orElseGet(() -> addCaseDefendant(defendant));
    }

    public CaseDefendantEntity addCaseDefendant(DefendantEntity defendant) {
        var caseDefendant = CaseDefendantEntity.builder().defendant(defendant).courtCase(this).build();
        defendant.setCaseDefendant(caseDefendant);
        caseDefendant.setDefendant(defendant);
        caseDefendant.setCourtCase(this);
        var caseDefendants = Optional.ofNullable(getCaseDefendants())
            .orElseGet(() -> {
                this.caseDefendants = new ArrayList<>();
                return this.caseDefendants;
            });
        caseDefendants.add(caseDefendant);
        return caseDefendant;
    }

    public boolean hasDefendant(String defendantId) {
        return getHearings().stream().filter(hearingEntity -> Objects.nonNull(hearingEntity.getHearingDefendant(defendantId))).findAny().isPresent();
    }
}
