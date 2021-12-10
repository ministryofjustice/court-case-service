package uk.gov.justice.probation.courtcaseservice.jpa.entity;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonManagedReference;
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
import org.hibernate.annotations.Type;
import org.hibernate.annotations.TypeDef;
import uk.gov.justice.probation.courtcaseservice.application.ClientDetails;

import javax.persistence.CascadeType;
import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.EnumType;
import javax.persistence.Enumerated;
import javax.persistence.FetchType;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.OneToMany;
import javax.persistence.PrePersist;
import javax.persistence.Table;
import java.io.Serializable;
import java.time.LocalDate;
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

    @Deprecated(forRemoval = true)
    @Column(name = "PROBATION_STATUS")
    private final String probationStatus;

    @Deprecated(forRemoval = true)
    @Column(name = "PREVIOUSLY_KNOWN_TERMINATION_DATE")
    private final LocalDate previouslyKnownTerminationDate;

    @Deprecated(forRemoval = true)
    @Column(name = "SUSPENDED_SENTENCE_ORDER", nullable = false)
    private final Boolean suspendedSentenceOrder;

    @Deprecated(forRemoval = true)
    @Column(name = "BREACH", nullable = false)
    private final Boolean breach;

    @Deprecated(forRemoval = true)
    @Column(name = "PRE_SENTENCE_ACTIVITY", nullable = false)
    private final Boolean preSentenceActivity;

    @ToString.Exclude
    @JsonManagedReference
    @OneToMany(mappedBy = "courtCase", fetch = FetchType.EAGER, cascade = CascadeType.ALL, orphanRemoval=true)
    private final List<OffenceEntity> offences;

    // If you have more than one collection with fetch = FetchType.EAGER then there is an exception
    // org.hibernate.loader.MultipleBagFetchException: cannot simultaneously fetch multiple bags
    // After CP integration, we will need to look at session boundaries @LazyCollection is one solution
    @ToString.Exclude
    @LazyCollection(value = LazyCollectionOption.FALSE)
    @JsonIgnore
    @OneToMany(mappedBy = "courtCase", cascade = CascadeType.ALL, orphanRemoval=true)
    private final List<HearingEntity> hearings;

    @ToString.Exclude
    @LazyCollection(value = LazyCollectionOption.FALSE)
    @JsonIgnore
    @OneToMany(mappedBy = "courtCase", cascade = CascadeType.ALL, orphanRemoval=true)
    private final List<DefendantEntity> defendants;

    @Column(name = "DEFENDANT_NAME")
    private final String defendantName;

    @Type(type = "jsonb")
    @Column(columnDefinition = "jsonb", name = "NAME")
    private final NamePropertiesEntity name;

    @Type(type = "jsonb")
    @Column(columnDefinition = "jsonb", name = "DEFENDANT_ADDRESS")
    private final AddressPropertiesEntity defendantAddress;

    @Column(name = "DEFENDANT_DOB")
    private final LocalDate defendantDob;

    @Column(name = "DEFENDANT_SEX")
    @Enumerated(EnumType.STRING)
    private final Sex defendantSex;

    @Column(name = "DEFENDANT_TYPE", nullable = false)
    @Enumerated(EnumType.STRING)
    private final DefendantType defendantType;

    @Deprecated(forRemoval = true)
    @Column(name = "CRN")
    private final String crn;

    @Column(name = "PNC")
    private final String pnc;

    @Column(name = "CRO")
    private final String cro;

    @Column(name = "NATIONALITY_1")
    private final String nationality1;

    @Column(name = "NATIONALITY_2")
    private final String nationality2;

    @Column(name = "SOURCE_TYPE")
    @Enumerated(EnumType.STRING)
    private final SourceType sourceType;

    @Deprecated(forRemoval = true)
    @Column(name = "AWAITING_PSR")
    private final Boolean awaitingPsr;

    @Column(name = "deleted", nullable = false, updatable = false)
    private final boolean deleted;

    @Column(name = "first_created", insertable = false, updatable = false)
    private final LocalDateTime firstCreated;

    @Deprecated(forRemoval = true)
    @Column(name = "manual_update", nullable = false, updatable = false)
    private boolean manualUpdate;

    @Deprecated(forRemoval = true)
    @PrePersist
    public void isManualUpdate(){
        manualUpdate = "prepare-a-case-for-court".equals(new ClientDetails().getClientId());
    }

    public DefendantEntity getDefendant(String defendantId) {
        return Optional.ofNullable(getDefendants()).orElse(Collections.emptyList())
            .stream()
            .filter(defendantEntity -> defendantId.equalsIgnoreCase(defendantEntity.getDefendantId()))
            .findFirst()
            .orElse(null);
    }
}
