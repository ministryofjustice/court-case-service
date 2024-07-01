package uk.gov.justice.probation.courtcaseservice.jpa.repository;


import com.fasterxml.jackson.annotation.JsonIgnore;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

import java.util.List;

@Entity
@Table(name = "HEARING")
public class HearingDTO {
    @Id
    private Long id;

    public void setId(Long id) {
        this.id = id;
    }

    public Long getId() {
        return id;
    }

    @Column(name = "HEARING_ID", nullable = false)
    private String hearingId;

    @ToString.Exclude
    @JsonIgnore
    @OneToMany(mappedBy = "hearing", orphanRemoval = true, cascade = CascadeType.ALL, fetch = FetchType.LAZY)
    @Getter
    @Setter
    private final List<HearingDefendantDTO> hearingDefendants = List.of();
}
