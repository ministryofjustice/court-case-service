package uk.gov.justice.probation.courtcaseservice.jpa.entity;

import com.fasterxml.jackson.annotation.JsonIgnore;
import io.swagger.annotations.ApiModel;
import java.io.Serializable;
import java.util.Objects;
import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.Table;
import javax.persistence.Version;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;


@ApiModel(description = "Court")
@Entity
@Builder
@AllArgsConstructor
@NoArgsConstructor
@Data
@Table(name = "COURT")
public class CourtEntity implements Serializable {
    @Id
    @Column(name = "ID", updatable = false, nullable = false)
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @JsonIgnore
    private Long id;

    @Column(name = "NAME", nullable = false)
    private String name;

    @Column(name = "COURT_CODE", nullable = false)
    private String courtCode;

    @Version
    private int version;

    @Override
    public boolean equals(Object other) {
        if (this == other) {
            return true;
        }
        if (!(other instanceof CourtEntity)) {
            return false;
        }

        CourtEntity that = (CourtEntity) other;

        return Objects.equals(this.courtCode, that.getCourtCode());
    }

    @Override
    public int hashCode() {
        return getCourtCode().hashCode();
    }
}
