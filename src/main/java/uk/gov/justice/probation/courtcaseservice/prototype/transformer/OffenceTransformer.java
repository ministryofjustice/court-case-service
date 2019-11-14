package uk.gov.justice.probation.courtcaseservice.prototype.transformer;

import org.springframework.stereotype.Component;
import uk.gov.justice.probation.courtcaseservice.prototype.data.api.Offence;
import uk.gov.justice.probation.courtcaseservice.prototype.data.entity.OffenceType;
import uk.gov.justice.probation.courtcaseservice.prototype.data.entity.OffencesType;

import java.util.List;

import static java.util.stream.Collectors.toList;

@Component
public class OffenceTransformer {
    public List<Offence> toOffences(OffencesType offences) {
        return offences.getOffence().stream().map(this::toOffence).collect(toList());
    }

    private Offence toOffence(OffenceType offence) {
        return Offence
                .builder()
                .adjournedDate(DateTimeHelper.asDate(offence.getAdjdate()))
                .adjournedReason(offence.getAdjreason())
                .code(offence.getCode())
                .convictionDate(DateTimeHelper.asDate(offence.getConvdate()))
                .contraryToActAndSection(offence.getAs())
                .plea(offence.getPlea())
                .sequence(offence.getOseq())
                .pleaDate(DateTimeHelper.asDate(offence.getPleadate()))
                .summary(offence.getSum())
                .title(offence.getTitle())
                .adjournedReason(offence.getAdjreason())
                .adjournedDate(DateTimeHelper.asDate(offence.getAdjdate()))
                .statementOfFact(offence.getSof())
                .build();
    }
}
