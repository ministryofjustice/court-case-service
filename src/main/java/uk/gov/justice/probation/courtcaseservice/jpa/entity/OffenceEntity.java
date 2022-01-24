package uk.gov.justice.probation.courtcaseservice.jpa.entity;

import com.fasterxml.jackson.annotation.JsonBackReference;
import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonProperty.Access;
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
@ToString(exclude = "courtCase")
@EqualsAndHashCode(callSuper = true)
public class OffenceEntity extends BaseImmutableEntity implements Serializable  {

    @Id
    @Column(name = "ID", updatable = false, nullable = false)
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @JsonIgnore
    private final Long id;

    @Column(name = "OFFENCE_TITLE")
    private final String offenceTitle;

    @Column(name = "OFFENCE_SUMMARY")
    private final String offenceSummary;

    @Column(name = "ACT")
    private final String act;

    @OrderColumn
    @Column(name = "SEQUENCE_NUMBER", nullable = false)
    @JsonProperty
    private final Integer sequenceNumber;
}
