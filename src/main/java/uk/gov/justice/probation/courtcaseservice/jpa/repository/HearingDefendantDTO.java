package uk.gov.justice.probation.courtcaseservice.jpa.repository;

import jakarta.persistence.*;
import lombok.Setter;
import lombok.ToString;
import org.hibernate.envers.NotAudited;
import uk.gov.justice.probation.courtcaseservice.jpa.entity.HearingEntity;
import uk.gov.justice.probation.courtcaseservice.jpa.entity.HearingNoteEntity;

import java.util.List;

@Entity
@Table(name = "HEARING_DEFENDANT")
public class HearingDefendantDTO {
    @Id
    private Long id;

    public void setId(Long id) {
        this.id = id;
    }

    public Long getId() {
        return id;
    }

    @ToString.Exclude
    @OneToMany(mappedBy = "hearingDefendantDTO", cascade = CascadeType.ALL, orphanRemoval=true, fetch = FetchType.LAZY)
    @NotAudited
    private List<HearingNoteDTO> notes;

    @ToString.Exclude
    @ManyToOne()
    @JoinColumn(name = "FK_HEARING_ID", referencedColumnName = "id")
    @Setter
    private HearingDTO hearing;
}
