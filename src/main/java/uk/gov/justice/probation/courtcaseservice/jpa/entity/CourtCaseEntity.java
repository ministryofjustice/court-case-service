package uk.gov.justice.probation.courtcaseservice.jpa.entity;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.vladmihalcea.hibernate.type.json.JsonBinaryType;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.hibernate.annotations.Type;
import org.hibernate.annotations.TypeDef;

import javax.persistence.*;
import java.io.Serializable;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.Collections;
import java.util.List;

@Entity

@AllArgsConstructor
@NoArgsConstructor
@Data
@Table(name = "COURT_CASE")
@TypeDef(name = "jsonb", typeClass = JsonBinaryType.class)
public class CourtCaseEntity implements Serializable {

    @Id
    @Column(name = "ID", updatable = false, nullable = false)
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @JsonIgnore
    private Long id;

    @Column(name = "LAST_UPDATED", nullable = false, columnDefinition = "TIMESTAMP DEFAULT CURRENT_TIMESTAMP")
    private LocalDateTime lastUpdated = LocalDateTime.now();

    @Column(name = "CASE_ID")
    private String caseId;

    @Column(name = "CASE_NO")
    private String caseNo;

    @Column(name = "COURT_CODE")
    private String courtCode;

    @Column(name = "COURT_ROOM")
    private String courtRoom;

    @Column(name = "SESSION_START_TIME")
    private LocalDateTime sessionStartTime;

    @Column(name = "PROBATION_STATUS")
    private String probationStatus;

    @Column(name = "PREVIOUSLY_KNOWN_TERMINATION_DATE")
    private LocalDate previouslyKnownTerminationDate;

    @Column(name = "SUSPENDED_SENTENCE_ORDER")
    private Boolean suspendedSentenceOrder;

    @Column(name = "BREACH")
    private Boolean breach;

    @OneToMany(mappedBy = "courtCase", cascade = CascadeType.ALL)
    private List<OffenceEntity> offences = Collections.emptyList();

    @Column(name = "DEFENDANT_NAME")
    private String defendantName;

    @Type(type = "jsonb")
    @Column(columnDefinition = "jsonb", name = "DEFENDANT_ADDRESS")
    private AddressPropertiesEntity defendantAddress;

    @Type(type = "jsonb")
    @Column(columnDefinition = "jsonb", name = "DATA")
    private String data;

    public CourtSession getSession() {
        return CourtSession.from(sessionStartTime);
    }

}
