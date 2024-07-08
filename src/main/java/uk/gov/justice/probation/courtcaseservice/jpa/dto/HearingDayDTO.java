package uk.gov.justice.probation.courtcaseservice.jpa.dto;

import com.fasterxml.jackson.annotation.JsonIgnore;
import jakarta.persistence.*;
import lombok.*;
import lombok.experimental.SuperBuilder;
import uk.gov.justice.probation.courtcaseservice.jpa.entity.CourtSession;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;

@Entity
@Table(name = "HEARING_DAY")
@AllArgsConstructor
@NoArgsConstructor(access = AccessLevel.PROTECTED, force = true)
@SuperBuilder
@Getter
public class HearingDayDTO {

    @Id
    @Column(name = "ID", updatable = false, nullable = false)
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @JsonIgnore
    private final Long id;

    @ManyToOne(optional = false, cascade = CascadeType.ALL)
    @JoinColumn(name = "FK_HEARING_ID", referencedColumnName = "id", nullable = false)
    @Setter
    private HearingDTO hearing;

    @Column(name = "HEARING_DAY", nullable = false)
    private final LocalDate day;

    @Column(name = "HEARING_TIME", nullable = false)
    private final LocalTime time;

    @Column(name = "COURT_CODE", nullable = false)
    private final String courtCode;

    @Column(name = "COURT_ROOM", nullable = false)
    private final String courtRoom;

    public CourtSession getSession() {
        return CourtSession.from(time);
    }

    public LocalDateTime getSessionStartTime() {
        return LocalDateTime.of(day, time);
    }

    public String loggableString(){
        return String.format("%s|%s|%sT%s", courtCode, courtRoom, day, time);
    }
}
