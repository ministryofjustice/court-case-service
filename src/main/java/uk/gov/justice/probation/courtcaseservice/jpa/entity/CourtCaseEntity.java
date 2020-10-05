package uk.gov.justice.probation.courtcaseservice.jpa.entity;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonManagedReference;
import com.vladmihalcea.hibernate.type.json.JsonBinaryType;
import io.swagger.annotations.ApiModel;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import lombok.Setter;
import lombok.ToString;
import lombok.experimental.SuperBuilder;
import org.hibernate.annotations.SQLDelete;
import org.hibernate.annotations.Type;
import org.hibernate.annotations.TypeDef;

import javax.persistence.CascadeType;
import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.FetchType;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.OneToMany;
import javax.persistence.Table;
import java.io.Serializable;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Objects;

@ApiModel(description = "Court Case")
@Entity
@AllArgsConstructor
@RequiredArgsConstructor
@SQLDelete(sql = "UPDATE COURT_CASE SET deleted = true WHERE ID = ? AND VERSION = ?")
@SuperBuilder
@ToString
@Getter
@Setter
@Table(name = "COURT_CASE")
@TypeDef(name = "jsonb", typeClass = JsonBinaryType.class)
public class CourtCaseEntity extends BaseImmutableEntity implements Serializable {

    @Id
    @Column(name = "ID", updatable = false, nullable = false)
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @JsonIgnore
    private Long id;

    @Column(name = "CASE_ID", nullable = false)
    private String caseId;

    @Column(name = "CASE_NO", nullable = false)
    private String caseNo;

    @Column(name = "COURT_CODE", nullable = false)
    private String courtCode;

    @Column(name = "COURT_ROOM")
    private String courtRoom;

    @Column(name = "SESSION_START_TIME", nullable = false)
    private LocalDateTime sessionStartTime;

    @Column(name = "PROBATION_STATUS", nullable = false)
    private String probationStatus;

    @Column(name = "PREVIOUSLY_KNOWN_TERMINATION_DATE")
    private LocalDate previouslyKnownTerminationDate;

    @Column(name = "SUSPENDED_SENTENCE_ORDER", nullable = false)
    private Boolean suspendedSentenceOrder;

    @Column(name = "BREACH", nullable = false)
    private Boolean breach;

    @JsonManagedReference
    @OneToMany(mappedBy = "courtCase", fetch = FetchType.EAGER, cascade = { CascadeType.ALL }, orphanRemoval=true)
    private List<ImmutableOffenceEntity> offences;

    @Column(name = "DEFENDANT_NAME")
    private String defendantName;

    @Type(type = "jsonb")
    @Column(columnDefinition = "jsonb", name = "DEFENDANT_ADDRESS")
    private AddressPropertiesEntity defendantAddress;

    @Column(name = "DEFENDANT_DOB")
    private LocalDate defendantDob;

    @Column(name = "DEFENDANT_SEX")
    private String defendantSex;

    @Column(name = "CRN")
    private String crn;

    @Column(name = "PNC")
    private String pnc;

    @Column(name = "CRO")
    private String cro;

    @Column(name = "LIST_NO")
    private String listNo;

    @Column(name = "NATIONALITY_1")
    private String nationality1;

    @Column(name = "NATIONALITY_2")
    private String nationality2;

    @JsonIgnore
    @OneToMany(mappedBy = "courtCase", cascade = { CascadeType.ALL }, orphanRemoval=true)
    private List<GroupedOffenderMatchesEntity> groupedOffenderMatches;

    public CourtSession getSession() {
        return CourtSession.from(sessionStartTime);
    }

    public String getDefendantSurname() {
        return defendantName == null ? "" : defendantName.substring(defendantName.lastIndexOf(" ")+1);
    }
//
//    public void clearOffences() {
//        for (ImmutableOffenceEntity offenceEntity : this.offences) {
//            offenceEntity.setCourtCase(null);
//        }
//        this.offences.clear();
//    }
//
//    public void removeOffences(List<OffenceEntity> offences) {
//        this.offences.removeAll(offences);
//        for (OffenceEntity offenceEntity : offences) {
//            offenceEntity.setCourtCase(null);
//        }
//    }
//
//    public void addOffence(OffenceEntity offenceEntity) {
//        offenceEntity.setCourtCase(this);
//        this.offences.add(offenceEntity);
//    }

    @Override
    public boolean equals(Object other) {
        if (this == other) {
            return true;
        }
        if (!(other instanceof CourtCaseEntity)) {
            return false;
        }

        CourtCaseEntity that = (CourtCaseEntity) other;

        return Objects.equals(this.caseNo, that.getCaseNo()) && Objects.equals(this.courtCode, that.getCourtCode());
    }

    @Override
    public int hashCode() {
        int result = getCaseNo() != null ? getCaseNo().hashCode() : 0;
        result = 31 * result + (getCourtCode() != null ? getCourtCode().hashCode() : 0);
        return result;
    }


}
