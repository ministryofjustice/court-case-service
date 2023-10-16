package uk.gov.justice.probation.courtcaseservice.jpa.entity;

import com.fasterxml.jackson.annotation.JsonIgnore;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import lombok.Setter;
import lombok.experimental.SuperBuilder;
import org.hibernate.annotations.JdbcTypeCode;
import org.hibernate.annotations.Type;
import org.hibernate.type.SqlTypes;
import uk.gov.justice.probation.courtcaseservice.service.model.MatchType;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import java.util.List;

@Schema(description = "Offender Match")
@Entity
@EqualsAndHashCode(callSuper=false)
@AllArgsConstructor
@RequiredArgsConstructor
@SuperBuilder
@Getter
@Table(name = "OFFENDER_MATCH")
public class OffenderMatchEntity extends BaseEntity {

    @Id
    @Column(name = "ID", updatable = false, nullable = false)
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @JsonIgnore
    @EqualsAndHashCode.Exclude
    private Long id;

    @Column(name = "CRN", nullable = false)
    @EqualsAndHashCode.Include
    private String crn;

    @Column(name = "PNC")
    @EqualsAndHashCode.Include
    private String pnc;

    @Column(name = "CRO")
    @EqualsAndHashCode.Include
    private String cro;

    @Column(name = "MATCH_TYPE", nullable = false)
    @Enumerated(EnumType.STRING)
    @EqualsAndHashCode.Include
    private MatchType matchType;

    @Setter
    @Column(name = "CONFIRMED", nullable = false)
    @EqualsAndHashCode.Exclude
    private Boolean confirmed;

    @Setter
    @Column(name = "REJECTED", nullable = false)
    @EqualsAndHashCode.Exclude
    private Boolean rejected;

    @JoinColumn(name = "GROUP_ID", referencedColumnName = "ID", nullable = false)
    @ManyToOne(optional = false)
    @Setter
    @EqualsAndHashCode.Include
    @JsonIgnore
    private GroupedOffenderMatchesEntity group;

    @JdbcTypeCode(SqlTypes.JSON)
    @Column(columnDefinition = "jsonb", name = "ALIASES")
    private List<OffenderAliasEntity> aliases;

    @Column(name = "MATCH_PROBABILITY")
    private Double matchProbability;

    public void update(OffenderMatchEntity updates) {
        pnc = updates.getPnc();
        cro = updates.getCro();
        matchType = updates.getMatchType();
        aliases = updates.getAliases();
        matchProbability = updates.getMatchProbability();
    }
}
