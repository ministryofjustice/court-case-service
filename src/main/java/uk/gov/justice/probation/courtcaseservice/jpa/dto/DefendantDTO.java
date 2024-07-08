package uk.gov.justice.probation.courtcaseservice.jpa.dto;

import com.fasterxml.jackson.annotation.JsonIgnore;
import jakarta.persistence.*;
import lombok.*;
import lombok.experimental.SuperBuilder;
import org.hibernate.annotations.JdbcTypeCode;
import org.hibernate.envers.NotAudited;
import org.hibernate.type.SqlTypes;
import uk.gov.justice.probation.courtcaseservice.application.ClientDetails;
import uk.gov.justice.probation.courtcaseservice.jpa.entity.*;

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
@EqualsAndHashCode(exclude = {"offender", "hearingDefendants", "id"})
public class DefendantDTO {
    @Id
    @Column(name = "ID", updatable = false, nullable = false)
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @JsonIgnore
    private final Long id;

    @Setter
    @ToString.Exclude
    @ManyToOne()
    @JoinColumn(name = "fk_offender_id", referencedColumnName = "id")
    @NotAudited
    private OffenderDTO offender;

    @Column(name = "CRN")
    private String crn;

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
        return Optional.ofNullable(offender).map(OffenderDTO::getCrn).orElse(null);
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
                .map(OffenderDTO::getProbationStatus)
                .map(OffenderProbationStatus::asDefendantProbationStatus)
                .orElse(offenderConfirmed ? DefendantProbationStatus.CONFIRMED_NO_RECORD : DefendantProbationStatus.UNCONFIRMED_NO_RECORD);
    }
}
