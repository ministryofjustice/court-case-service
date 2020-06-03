package uk.gov.justice.probation.courtcaseservice.jpa.entity;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.vladmihalcea.hibernate.type.json.JsonBinaryType;
import io.swagger.annotations.ApiModel;
import java.io.Serializable;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.Collections;
import java.util.List;
import javax.persistence.CascadeType;
import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.OneToMany;
import javax.persistence.Table;
import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.Setter;
import org.hibernate.annotations.Type;
import org.hibernate.annotations.TypeDef;

@ApiModel(description = "Court Case")
@Entity
@AllArgsConstructor
@Data
@Builder
@Table(name = "COURT_CASE")
@TypeDef(name = "jsonb", typeClass = JsonBinaryType.class)
public class CourtCaseEntity implements Serializable {

    @Id
    @Column(name = "ID", updatable = false, nullable = false)
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @JsonIgnore
    private Long id;

    @Column(name = "LAST_UPDATED", nullable = false, columnDefinition = "TIMESTAMP DEFAULT CURRENT_TIMESTAMP")
    private LocalDateTime lastUpdated;

    @Column(name = "CASE_ID", nullable = false)
    private String caseId;

    @Column(name = "CASE_NO", nullable = false)
    @Setter(AccessLevel.NONE)
    private String caseNo;

    @Column(name = "COURT_CODE", nullable = false)
    @Setter(AccessLevel.NONE)
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

    @OneToMany(mappedBy = "courtCase", cascade = CascadeType.ALL)
    private List<OffenceEntity> offences;

    @Column(name = "DEFENDANT_NAME")
    private String defendantName;

    @Type(type = "jsonb")
    @Column(columnDefinition = "jsonb", name = "DEFENDANT_ADDRESS")
    private AddressPropertiesEntity defendantAddress;

    @Column(name = "CRN")
    private String crn;

    @Column(name = "PNC")
    private String pnc;

    @Column(name = "LIST_NO")
    private String listNo;

    @Column(name = "DEFENDANT_DOB")
    private LocalDate defendantDob;

    @Column(name = "DEFENDANT_SEX")
    private String defendantSex;

    @Column(name = "NATIONALITY_1")
    private String nationality1;

    @Column(name = "NATIONALITY_2")
    private String nationality2;

    protected CourtCaseEntity() {
        super();
        this.lastUpdated = LocalDateTime.now();
        this.offences = Collections.emptyList();
    }


    /**
     * Construct with required identifying fields which form a unique key.
     * Application should construct with this and do not need setter access for either field.
     * @param courtCode
     *             the court code
     * @param caseNo
     *              the case number
     */
    public CourtCaseEntity(String courtCode, String caseNo) {
        super();
        this.courtCode = courtCode;
        this.caseNo = caseNo;
        this.lastUpdated = LocalDateTime.now();
        this.offences = Collections.emptyList();
    }

    public CourtSession getSession() {
        return CourtSession.from(sessionStartTime);
    }

}
