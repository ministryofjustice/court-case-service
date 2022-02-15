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
import org.hibernate.annotations.Type;
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
import javax.persistence.JoinColumn;
import javax.persistence.ManyToOne;
import javax.persistence.OneToMany;
import javax.persistence.PrePersist;
import javax.persistence.Table;
import java.io.Serializable;
import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

@Entity
@Table(name = "DEFENDANT")
@AllArgsConstructor
@NoArgsConstructor(access = AccessLevel.PROTECTED, force = true)
@SuperBuilder
@With
@Getter
@ToString
@EqualsAndHashCode(callSuper = true, exclude = "courtCase")
public class DefendantEntity extends BaseImmutableEntity implements Serializable {

    @Id
    @Column(name = "ID", updatable = false, nullable = false)
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @JsonIgnore
    private final Long id;

    @ToString.Exclude
    @ManyToOne(optional = false)
    @JoinColumn(name = "FK_HEARING_ID", referencedColumnName = "id", nullable = false)
    @Setter
    private HearingEntity hearing;

    @ToString.Exclude
    @ManyToOne(optional = true, fetch = FetchType.EAGER, cascade = CascadeType.MERGE)
    @JoinColumn(name = "CRN", referencedColumnName = "CRN")
    @Setter
    private OffenderEntity offender;

    @Column(name = "DEFENDANT_ID", nullable = false)
    private final String defendantId;

    @ToString.Exclude
    @LazyCollection(value = LazyCollectionOption.FALSE)
    @OneToMany(mappedBy = "defendant", cascade = CascadeType.ALL, orphanRemoval=true)
    private final List<DefendantOffenceEntity> offences;

    @Column(name = "DEFENDANT_NAME", nullable = false)
    private final String defendantName;

    @Type(type = "jsonb")
    @Column(columnDefinition = "jsonb", name = "NAME", nullable = false)
    private final NamePropertiesEntity name;

    @Column(name = "TYPE", nullable = false)
    @Enumerated(EnumType.STRING)
    private final DefendantType type;

    @Type(type = "jsonb")
    @Column(columnDefinition = "jsonb", name = "ADDRESS")
    private final AddressPropertiesEntity address;

    @Column(name = "PNC")
    private final String pnc;

    @Column(name = "CRO")
    private final String cro;

    @Column(name = "DATE_OF_BIRTH")
    private final LocalDate dateOfBirth;

    @Column(name = "SEX", nullable = false)
    @Enumerated(EnumType.STRING)
    private final Sex sex;

    @Column(name = "NATIONALITY_1")
    private final String nationality1;

    @Column(name = "NATIONALITY_2")
    private final String nationality2;

    @Column(name = "manual_update", nullable = false, updatable = false)
    private boolean manualUpdate;

    @Column(name = "OFFENDER_CONFIRMED", nullable = false, updatable = false)
    private boolean offenderConfirmed;

    @PrePersist
    public void prePersistManualUpdate(){
        manualUpdate = "prepare-a-case-for-court".equals(new ClientDetails().getClientId());
        offenderConfirmed = offenderConfirmed || manualUpdate;
    }

    public String getDefendantSurname() {
        return defendantName == null ? "" : defendantName.substring(defendantName.lastIndexOf(" ")+1);
    }

    public String getCrn() {
        return offender != null ? offender.getCrn() : null;
    }

    public DefendantProbationStatus getProbationStatusForDisplay() {
        return Optional.ofNullable(offender)
                .map(OffenderEntity::getProbationStatus)
                .map(OffenderProbationStatus::asDefendantProbationStatus)
                .orElse(offenderConfirmed ? DefendantProbationStatus.CONFIRMED_NO_RECORD : DefendantProbationStatus.UNCONFIRMED_NO_RECORD);
    }
}
