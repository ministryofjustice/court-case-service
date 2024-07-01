package uk.gov.justice.probation.courtcaseservice.jpa.repository;

import com.fasterxml.jackson.annotation.JsonIgnore;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;
import lombok.ToString;
import org.hibernate.envers.NotAudited;

import java.util.List;

@Entity
@Table(name = "HEARING_DEFENDANT")
public class HearingDefendantDTO {
    @Id
    @Column(name = "id", updatable = false, nullable = false)
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @JsonIgnore
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
    @Getter
    @Setter
    private HearingDTO hearing;
}
