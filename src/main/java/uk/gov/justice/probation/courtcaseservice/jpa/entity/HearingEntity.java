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
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.JoinColumn;
import javax.persistence.ManyToOne;
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
public class HearingEntity extends BaseImmutableEntity implements Serializable {

    @Id
    @Column(name = "ID", updatable = false, nullable = false)
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @JsonIgnore
    private final Long id;

    @Column(name = "HEARING_ID", nullable = false)
    private final String hearingId;            // Uniquely identifies a subset of defendants and offences to be heard at a given hearing

    @Column(name = "COURT_ROOM", nullable = false)
    private final String courtRoom;         // This is received at hearing level from CP so makes sense to have it here

    @ManyToOne(optional = false)
    @JoinColumn(name = "COURT_CASE_ID", referencedColumnName = "id", nullable = false)
    @Setter
    private HearingEntity courtCase;        // If we need to link back to the case for case tracking in future we can do it through this relationship

    @ToString.Exclude
    @LazyCollection(value = LazyCollectionOption.FALSE)
    @JsonIgnore
    @OneToMany(mappedBy = "hearing", cascade = CascadeType.ALL, orphanRemoval=true)
    private final List<HearingDayEntity> hearingDays;       // Renamed from 'HearingEntity' to free up that name, these hold just the details of the different dates and timings for a hearing

    @ToString.Exclude
    @LazyCollection(value = LazyCollectionOption.FALSE)
    @JsonIgnore
    @OneToMany(mappedBy = "courtCase", cascade = CascadeType.ALL, orphanRemoval=true)
    private final List<DefendantEntity> defendants;         // Defendants are now associated with a HearingEntity to reflect the fact that they can change from hearing to hearing

    @Column(name = "deleted", nullable = false, updatable = false)
    private final boolean deleted;                          // As with the entire case we may want to mark a single hearing as deleted

    @Column(name = "first_created", insertable = false, updatable = false)
    private final LocalDateTime firstCreated;               // Used to identify recently added cases, this is a dynamically calculated field built in the SQL. This will show the date of the first database record with a given hearingId

    @Deprecated(forRemoval = true)
    @Column(name = "manual_update", nullable = false, updatable = false)
    private boolean manualUpdate;                           // Historically used to identify if a case had been manually updated for the purposes of prioritising user made changes. Not sure if we're still using this as there's a separate field in defendant which fulfils this role now. Could do with Spiking this to find out and get rid if possible.

    public DefendantEntity getDefendant(String defendantId) {
        return Optional.ofNullable(getDefendants()).orElse(Collections.emptyList())
            .stream()
            .filter(defendantEntity -> defendantId.equalsIgnoreCase(defendantEntity.getDefendantId()))
            .findFirst()
            .orElse(null);
    }
}
