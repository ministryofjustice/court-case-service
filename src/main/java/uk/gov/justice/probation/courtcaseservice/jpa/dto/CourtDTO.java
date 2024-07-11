package uk.gov.justice.probation.courtcaseservice.jpa.dto;

import com.fasterxml.jackson.annotation.JsonIgnore;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.experimental.SuperBuilder;
import uk.gov.justice.probation.courtcaseservice.jpa.entity.CourtEntity;

import java.util.Objects;

@Schema(description = "Court")
@Entity
@SuperBuilder
@AllArgsConstructor
@NoArgsConstructor
@Getter
@Setter
@Table(name = "COURT")
public class CourtDTO {
    @Id
    @Column(name = "ID", updatable = false, nullable = false)
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @JsonIgnore
    private Long id;

    @Column(name = "NAME", nullable = false)
    private String name;

    @Column(name = "COURT_CODE", nullable = false)
    private String courtCode;

    @Override
    public boolean equals(Object other) {
        if (this == other) {
            return true;
        }
        if (!(other instanceof CourtEntity that)) {
            return false;
        }

        return Objects.equals(this.courtCode, that.getCourtCode());
    }

    @Override
    public int hashCode() {
        return getCourtCode().hashCode();
    }
}