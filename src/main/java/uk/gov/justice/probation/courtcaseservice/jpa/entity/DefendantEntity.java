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
import org.hibernate.annotations.Type;
import uk.gov.justice.probation.courtcaseservice.application.ClientDetails;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.EnumType;
import javax.persistence.Enumerated;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.PrePersist;
import javax.persistence.Table;
import javax.persistence.Transient;
import java.io.Serializable;
import java.time.LocalDate;
import java.util.Optional;

@Entity
@Table(name = "DEFENDANT")
@AllArgsConstructor
@NoArgsConstructor(access = AccessLevel.PROTECTED, force = true)
@SuperBuilder
@With
@Getter
@ToString
@EqualsAndHashCode(exclude = {"hearingDefendant", "offender", "id"})
public class DefendantEntity extends BaseImmutableEntity implements Serializable {

    @Id
    @Column(name = "ID", updatable = false, nullable = false)
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @JsonIgnore
    private final Long id;

    @ToString.Exclude
    @Transient
    @Setter
    private OffenderEntity offender;

    @Column(name = "CRN", nullable = false, updatable = false)
    private final String crn;

    @Column(name = "DEFENDANT_ID", nullable = false)
    private final String defendantId;

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

    @Type(type = "jsonb")
    @Column(columnDefinition = "jsonb", name = "PHONE_NUMBER")
    private final PhoneNumberEntity phoneNumber;

    @Column(name = "PERSON_ID", nullable = false)
    private final String personId;

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
}
