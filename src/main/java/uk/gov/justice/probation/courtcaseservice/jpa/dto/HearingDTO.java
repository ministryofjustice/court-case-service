package uk.gov.justice.probation.courtcaseservice.jpa.dto;

import com.fasterxml.jackson.annotation.JsonIgnore;
import jakarta.persistence.*;
import lombok.*;
import lombok.experimental.SuperBuilder;
import org.hibernate.envers.Audited;
import uk.gov.justice.probation.courtcaseservice.jpa.entity.HearingEventType;
import uk.gov.justice.probation.courtcaseservice.jpa.entity.SourceType;

import java.time.LocalDateTime;
import java.util.List;

@Entity
@Table(name = "HEARING")
@AllArgsConstructor
@NoArgsConstructor(access = AccessLevel.PROTECTED, force = true)
@Getter
@SuperBuilder
public class HearingDTO {
    @Id
    private Long id;

    @Column(name = "HEARING_ID", nullable = false)
    private String hearingId;

    @ManyToOne(optional = false, cascade = CascadeType.ALL)
    @JoinColumn(name = "FK_COURT_CASE_ID", referencedColumnName = "id", nullable = false)
    @Setter
    private CourtCaseDTO courtCase;

    @ToString.Exclude
    @JsonIgnore
    @OneToMany(mappedBy = "hearing", cascade = CascadeType.ALL, orphanRemoval = true, fetch = FetchType.LAZY)
    @OrderBy("day, time ASC")
    private final List<HearingDayDTO> hearingDays;

    @Column(name = "first_created", insertable = false, updatable = false)
    private LocalDateTime firstCreated;

    @Column(name = "HEARING_EVENT_TYPE")
    @Enumerated(EnumType.STRING)
    @Audited
    private HearingEventType hearingEventType;

    @Column(name = "HEARING_TYPE")
    private String hearingType;

    @Column(name = "LIST_NO")
    private String listNo;

    public String getCaseId() {
        return courtCase.getCaseId();
    }

    public String getCaseNo() {
        return courtCase.getCaseNo();
    }

    public SourceType getSourceType() {
        return courtCase.getSourceType();
    }
}