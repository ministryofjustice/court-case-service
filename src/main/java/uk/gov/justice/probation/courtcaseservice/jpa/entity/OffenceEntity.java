package uk.gov.justice.probation.courtcaseservice.jpa.entity;

import com.fasterxml.jackson.annotation.JsonBackReference;
import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonProperty.Access;
import java.io.Serializable;
import java.util.Objects;
import javax.persistence.CascadeType;
import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.JoinColumn;
import javax.persistence.JoinColumns;
import javax.persistence.ManyToOne;
import javax.persistence.OrderColumn;
import javax.persistence.PreRemove;
import javax.persistence.Table;
import javax.persistence.Version;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.ToString;

@Entity
@Table(name = "OFFENCE")
@AllArgsConstructor
@Builder
@NoArgsConstructor
@Getter
@Setter
@ToString(exclude = "courtCase")
public class OffenceEntity implements Serializable  {

    @Id
    @Column(name = "ID", updatable = false, nullable = false)
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @JsonIgnore
    private Long id;

    @ManyToOne(cascade = CascadeType.ALL)
    @JoinColumns({
        @JoinColumn(name = "CASE_NO", referencedColumnName = "case_no"),
        @JoinColumn(name = "COURT_CODE", referencedColumnName = "court_code"),
    })
    @JsonProperty(access = Access.WRITE_ONLY)
    @JsonBackReference
    private CourtCaseEntity courtCase;

    @Column(name = "OFFENCE_TITLE")
    private String offenceTitle;

    @Column(name = "OFFENCE_SUMMARY")
    private String offenceSummary;

    @Column(name = "ACT")
    private String act;

    @OrderColumn
    @Column(name = "SEQUENCE_NUMBER", nullable = false)
    @JsonProperty
    private Integer sequenceNumber;

    @Version
    private int version;

    @PreRemove
    public void preRemove() {
        setCourtCase(null);
    }

    @Override
    public boolean equals(Object other) {
        if (this == other) {
            return true;
        }
        if (!(other instanceof OffenceEntity)) {
            return false;
        }

        OffenceEntity that = (OffenceEntity) other;
        if (Objects.equals(courtCase, that.getCourtCase())) {
            return false;
        }
        return Objects.equals(sequenceNumber, that.getSequenceNumber());
    }

    @Override
    public int hashCode() {
        int result = getCourtCase().hashCode();
        result = 31 * result +  ((getSequenceNumber() != null)  ? getSequenceNumber().hashCode() : 1);
        return result;
    }
}
