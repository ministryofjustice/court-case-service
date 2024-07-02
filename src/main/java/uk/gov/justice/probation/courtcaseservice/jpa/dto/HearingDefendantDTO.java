package uk.gov.justice.probation.courtcaseservice.jpa.dto;

import com.fasterxml.jackson.annotation.JsonIgnore;
import jakarta.persistence.*;
import lombok.*;
import org.hibernate.envers.NotAudited;

import java.util.List;

@Entity
@Table(name = "HEARING_DEFENDANT")
@AllArgsConstructor
@NoArgsConstructor(access = AccessLevel.PROTECTED, force = true)
@Getter
public class HearingDefendantDTO {
    @Id
    @Column(name = "id", updatable = false, nullable = false)
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @JsonIgnore
    private Long id;

    @Column(name = "DEFENDANT_ID", nullable = false)
    private final String defendantId;

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
