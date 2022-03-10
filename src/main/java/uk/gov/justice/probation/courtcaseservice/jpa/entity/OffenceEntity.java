package uk.gov.justice.probation.courtcaseservice.jpa.entity;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.ToString;
import lombok.experimental.SuperBuilder;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.JoinColumn;
import javax.persistence.ManyToOne;
import javax.persistence.OrderColumn;
import javax.persistence.Table;
import java.io.Serializable;

@Entity
@Table(name = "OFFENCE")
@AllArgsConstructor
@SuperBuilder
@NoArgsConstructor(access = AccessLevel.PROTECTED, force = true)
@Getter
@ToString(exclude = "hearingDefendant")
@EqualsAndHashCode(callSuper = true, exclude = "hearingDefendant")
public class OffenceEntity extends BaseImmutableEntity implements Serializable  {

    @Id
    @Column(name = "ID", updatable = false, nullable = false)
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @JsonIgnore
    private final Long id;

    @ManyToOne
    @JoinColumn(name = "FK_HEARING_DEFENDANT_ID", referencedColumnName = "id")
    @Setter
    private HearingDefendantEntity hearingDefendant;

    @Column(name = "TITLE", nullable = false)
    private final String title;

    @Column(name = "SUMMARY", nullable = false)
    private final String summary;

    @Column(name = "ACT")
    private final String act;

    @OrderColumn
    @Column(name = "SEQUENCE", nullable = false)
    @JsonProperty
    private final Integer sequence;

    @Column(name = "LIST_NO", nullable = false)
    private final Integer listNo;
}
