package uk.gov.justice.probation.courtcaseservice.jpa.entity;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.vladmihalcea.hibernate.type.json.JsonBinaryType;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
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
import javax.persistence.OneToMany;
import javax.persistence.Table;
import java.io.Serializable;
import java.time.LocalDateTime;
import java.util.Collections;
import java.util.List;
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
@TypeDef(name = "jsonb", typeClass = JsonBinaryType.class)
public class CourtCaseEntity extends BaseImmutableEntity implements Serializable {

    @Id
    @Column(name = "ID", updatable = false, nullable = false)
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @JsonIgnore
    private final Long id;

    @Column(name = "CASE_ID", nullable = false)
    private final String caseId;

    @Column(name = "CASE_NO", nullable = false)
    private final String caseNo;

    @Column(name = "COURT_CODE", nullable = false)
    private final String courtCode;

    @Column(name = "COURT_ROOM", nullable = false)
    private final String courtRoom;

    @Column(name = "LIST_NO")
    private final String listNo;

    // If you have more than one collection with fetch = FetchType.EAGER then there is an exception
    // org.hibernate.loader.MultipleBagFetchException: cannot simultaneously fetch multiple bags
    // After CP integration, we will need to look at session boundaries @LazyCollection is one solution
    @ToString.Exclude
    @LazyCollection(value = LazyCollectionOption.FALSE)
    @JsonIgnore
    @OneToMany(mappedBy = "courtCase", cascade = CascadeType.ALL, orphanRemoval=true)
    private final List<HearingDayEntity> hearingDays;

    @ToString.Exclude
    @LazyCollection(value = LazyCollectionOption.FALSE)
    @JsonIgnore
    @OneToMany(mappedBy = "courtCase", cascade = CascadeType.ALL, orphanRemoval=true)
    private final List<DefendantEntity> defendants;

    @Column(name = "SOURCE_TYPE")
    @Enumerated(EnumType.STRING)
    private final SourceType sourceType;

    @Column(name = "deleted", nullable = false, updatable = false)
    private final boolean deleted;

    @Column(name = "first_created", insertable = false, updatable = false)
    private final LocalDateTime firstCreated;

    @Deprecated(forRemoval = true)
    @Column(name = "manual_update", nullable = false, updatable = false)
    private boolean manualUpdate;

    public DefendantEntity getDefendant(String defendantId) {
        return Optional.ofNullable(getDefendants()).orElse(Collections.emptyList())
            .stream()
            .filter(defendantEntity -> defendantId.equalsIgnoreCase(defendantEntity.getDefendantId()))
            .findFirst()
            .orElse(null);
    }
}
