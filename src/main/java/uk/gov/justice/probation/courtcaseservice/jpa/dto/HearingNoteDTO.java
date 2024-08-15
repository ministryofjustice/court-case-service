package uk.gov.justice.probation.courtcaseservice.jpa.dto;


import com.fasterxml.jackson.annotation.JsonIgnore;
import jakarta.persistence.*;
import lombok.Setter;
import lombok.ToString;
import org.hibernate.envers.NotAudited;

@Entity
@Table(name = "HEARING_NOTES")
public class HearingNoteDTO {
    @Id
    private Long id;

    public void setId(Long id) {
        this.id = id;
    }

    public Long getId() {
        return id;
    }

    @ToString.Exclude
    @NotAudited
    private String note;

    @ManyToOne
    @JoinColumn(name = "FK_HEARING_DEFENDANT_ID", referencedColumnName = "id")
    @Setter
    @JsonIgnore
    @ToString.Exclude
    private HearingDefendantDTO hearingDefendantDTO;
}