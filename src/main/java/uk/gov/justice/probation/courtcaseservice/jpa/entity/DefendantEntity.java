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
import org.hibernate.annotations.JdbcTypeCode;
import org.hibernate.annotations.LazyCollection;
import org.hibernate.annotations.LazyCollectionOption;
import org.hibernate.envers.Audited;
import org.hibernate.envers.NotAudited;
import org.hibernate.type.SqlTypes;
import uk.gov.justice.probation.courtcaseservice.application.ClientDetails;

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
@EqualsAndHashCode(exclude = {"offender", "hearingDefendants", "id"})
@Audited
public class DefendantEntity extends BaseAuditedEntity implements Serializable {

    @Id
    @Column(name = "ID", updatable = false, nullable = false)
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @JsonIgnore
    private final Long id;

    @Setter
    @ToString.Exclude
    @ManyToOne(cascade = { CascadeType.MERGE, CascadeType.PERSIST })
    @JoinColumn(name = "fk_offender_id", referencedColumnName = "id")
    @NotAudited
    private OffenderEntity offender;

    @Column(name = "CRN")
    private String crn;

    @ToString.Exclude
    @LazyCollection(value = LazyCollectionOption.TRUE)
    @JsonIgnore
    @OneToMany(mappedBy = "defendant")
    private List<HearingDefendantEntity> hearingDefendants;

    @ToString.Exclude
    @Setter
    @JsonIgnore
    @OneToOne(mappedBy = "defendant")
    private CaseDefendantEntity caseDefendant;

    @Column(name = "DEFENDANT_ID", nullable = false)
    private String defendantId;

    @Column(name = "DEFENDANT_NAME", nullable = false)
    private String defendantName;

    @JdbcTypeCode(SqlTypes.JSON)
    @Column(columnDefinition = "jsonb", name = "NAME", nullable = false)
    private NamePropertiesEntity name;

    @Column(name = "TYPE", nullable = false)
    @Enumerated(EnumType.STRING)
    private DefendantType type;

    @JdbcTypeCode(SqlTypes.JSON)
    @Column(columnDefinition = "jsonb", name = "ADDRESS")
    private AddressPropertiesEntity address;

    @Column(name = "PNC")
    private String pnc;

    @Column(name = "CRO")
    private String cro;

    @Column(name = "DATE_OF_BIRTH")
    private LocalDate dateOfBirth;

    @Column(name = "SEX", nullable = false)
    @Enumerated(EnumType.STRING)
    private Sex sex;

    @Column(name = "NATIONALITY_1")
    private String nationality1;

    @Column(name = "NATIONALITY_2")
    private String nationality2;

    @Column(name = "manual_update", nullable = false)
    private boolean manualUpdate;

    @Column(name = "OFFENDER_CONFIRMED", nullable = false)
    private boolean offenderConfirmed;

    @JdbcTypeCode(SqlTypes.JSON)
    @Column(columnDefinition = "jsonb", name = "PHONE_NUMBER")
    private PhoneNumberEntity phoneNumber;

    @Column(name = "PERSON_ID", nullable = false)
    private String personId;

    public String getCrn() {
        return Optional.ofNullable(offender).map(OffenderEntity::getCrn).orElse(null);
    }

    @PrePersist
    public void prePersistManualUpdate(){
        manualUpdate = "prepare-a-case-for-court".equals(new ClientDetails().getClientId());
        offenderConfirmed = offenderConfirmed || manualUpdate;
    }

    public String getDefendantSurname() {
        return defendantName == null ? "" : defendantName.substring(defendantName.lastIndexOf(" ")+1);
    }

    public DefendantProbationStatus getProbationStatusForDisplay() {
        return Optional.ofNullable(offender)
                .map(OffenderEntity::getProbationStatus)
                .map(OffenderProbationStatus::asDefendantProbationStatus)
                .orElse(offenderConfirmed ? DefendantProbationStatus.CONFIRMED_NO_RECORD : DefendantProbationStatus.UNCONFIRMED_NO_RECORD);
    }

    public void update(DefendantEntity defendantUpdate) {
        this.crn = defendantUpdate.getCrn();
        this.defendantId = defendantUpdate.getDefendantId();
        this.defendantName = defendantUpdate.getDefendantName();
        this.name = defendantUpdate.getName();
        this.type = defendantUpdate.getType();
        this.address = defendantUpdate.getAddress();
        this.pnc = defendantUpdate.getPnc();
        this.cro = defendantUpdate.getCro();
        this.dateOfBirth = defendantUpdate.getDateOfBirth();
        this.sex = defendantUpdate.getSex();
        this.nationality1 = defendantUpdate.getNationality1();
        this.nationality2 = defendantUpdate.getNationality2();
        this.phoneNumber = defendantUpdate.getPhoneNumber();
        this.personId = defendantUpdate.getPersonId();
        Optional.ofNullable(this.offender).ifPresentOrElse(offenderEntity -> Optional.ofNullable(defendantUpdate.getOffender()).ifPresent(offenderUpdate -> {
            if(StringUtils.equals(this.getOffender().getCrn(), defendantUpdate.getOffender().getCrn())) {
                this.offender.update(defendantUpdate.getOffender());
            } else {
                this.offender = defendantUpdate.getOffender();
            }
        }), () -> this.offender = defendantUpdate.getOffender());
    }

    public void addHearingDefendant(HearingDefendantEntity hearingDefendantEntity) {
        this.hearingDefendants.add(hearingDefendantEntity);
        hearingDefendantEntity.setDefendant(this);
    }

    public void confirmMatch(OffenderEntity updatedOffender) {
        this.crn = updatedOffender.getCrn();
        this.offender = updatedOffender;
        this.manualUpdate = true;
        this.offenderConfirmed = true;
    }

    public void confirmNoMatch() {
        this.crn = null;
        this.offender = null;
        this.manualUpdate = true;
        this.offenderConfirmed = true;
    }
}
