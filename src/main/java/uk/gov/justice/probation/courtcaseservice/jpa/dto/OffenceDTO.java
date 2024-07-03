package uk.gov.justice.probation.courtcaseservice.jpa.dto;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonProperty;
import jakarta.persistence.*;
import lombok.*;
import lombok.experimental.SuperBuilder;
import uk.gov.justice.probation.courtcaseservice.jpa.entity.PleaEntity;
import uk.gov.justice.probation.courtcaseservice.jpa.entity.VerdictEntity;

import java.math.BigDecimal;

@Entity
@Table(name = "OFFENCE")
@AllArgsConstructor
@SuperBuilder
@NoArgsConstructor(access = AccessLevel.PROTECTED, force = true)
@Getter
public class OffenceDTO {
    @Id
    @Column(name = "ID", updatable = false, nullable = false)
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @JsonIgnore
    private final Long id;

    @Column(name = "TITLE", nullable = false)
    private final String title;

    @Column(name = "SUMMARY", nullable = false, columnDefinition = "TEXT")
    private final String summary;

    @Column(name = "ACT")
    private final String act;

    @OrderColumn
    @Column(name = "SEQUENCE", nullable = false)
    @JsonProperty
    private final Integer sequence;

    @Column(name = "LIST_NO", nullable = false)
    private final Integer listNo;

    @Column(name = "OFFENCE_CODE")
    private final String offenceCode;

    @Column(name = "SHORT_TERM_CUSTODY_PREDICTOR_SCORE")
    @Setter
    private BigDecimal shortTermCustodyPredictorScore;

    @OneToOne(cascade = {CascadeType.ALL})
    @JoinColumn(name = "plea_id", referencedColumnName = "id")
    private PleaEntity plea;

    @OneToOne(cascade = {CascadeType.ALL})
    @JoinColumn(name = "verdict_id", referencedColumnName = "id")
    private VerdictEntity verdict;

    @ManyToOne
    @JoinColumn(name = "FK_HEARING_DEFENDANT_ID", referencedColumnName = "id")
    @Setter
    @JsonIgnore
    @ToString.Exclude
    private HearingDefendantDTO hearingDefendantDTO;
}
