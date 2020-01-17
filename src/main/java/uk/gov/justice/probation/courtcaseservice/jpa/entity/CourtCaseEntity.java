package uk.gov.justice.probation.courtcaseservice.jpa.entity;


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
    @Column(name = "ID")
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "CASE_ID")
    private Long caseId;

    @Column(name = "CASE_NO")
    private String caseNo;

    @Column(name = "COURT_ID")
    private Long courtId;

    @Column(name = "COURT_ROOM")
    private String courtRoom;

    @Column(name = "SESSION_START_TIME")
    private LocalDateTime sessionStartTime;

    @Type(type = "jsonb")
    @Column(columnDefinition = "jsonb", name = "DATA")
    private String data;

    @Column(name = "PROBATION_RECORD")
    private String probationRecord;

    public CourtSession getSession() {
        return CourtSession.from(sessionStartTime);
    }

}
