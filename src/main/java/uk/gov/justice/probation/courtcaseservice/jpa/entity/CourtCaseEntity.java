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
import java.time.LocalDateTime;

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

    @Type(type = "jsonb")
    @Column(columnDefinition = "jsonb", name = "DATA")
    private String data;

    @Column(name = "PROBATION_RECORD")
    private String probationRecord;

}
