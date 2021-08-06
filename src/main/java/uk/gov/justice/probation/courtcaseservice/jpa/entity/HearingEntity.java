package uk.gov.justice.probation.courtcaseservice.jpa.entity;

import java.io.Serializable;
import java.time.LocalDateTime;
import com.fasterxml.jackson.annotation.JsonIgnore;
import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.JoinColumn;
import javax.persistence.ManyToOne;
import javax.persistence.Table;
import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.ToString;
import lombok.experimental.SuperBuilder;

@Entity
@Table(name = "HEARING")
@AllArgsConstructor
@NoArgsConstructor(access = AccessLevel.PROTECTED, force = true)
@SuperBuilder
@Getter
@ToString(exclude = "courtCase")
@EqualsAndHashCode(callSuper = true)
public class HearingEntity extends BaseImmutableEntity implements Serializable {

    @Id
    @Column(name = "ID", updatable = false, nullable = false)
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @JsonIgnore
    private final Long id;

    @ManyToOne(optional = false)
    @JoinColumn(name = "COURT_CASE_ID", referencedColumnName = "id", nullable = false)
    @Setter
    private CourtCaseEntity courtCase;

    @Column(name = "SESSION_START_TIME", nullable = false)
    private final LocalDateTime sessionStartTime;

    @Column(name = "COURT_CODE", nullable = false)
    private final String courtCode;

    @Column(name = "COURT_ROOM", nullable = false)
    private final String courtRoom;

    @Column(name = "LIST_NO")
    private final String listNo;

    public CourtSession getSession() {
        return CourtSession.from(sessionStartTime);
    }

}
