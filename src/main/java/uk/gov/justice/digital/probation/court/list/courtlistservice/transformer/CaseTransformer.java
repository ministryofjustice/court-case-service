package uk.gov.justice.digital.probation.court.list.courtlistservice.transformer;

import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;
import uk.gov.justice.digital.probation.court.list.courtlistservice.data.api.Case;
import uk.gov.justice.digital.probation.court.list.courtlistservice.data.api.Defendant;
import uk.gov.justice.digital.probation.court.list.courtlistservice.data.entity.CaseType;
import uk.gov.justice.digital.probation.court.list.courtlistservice.data.entity.CasesType;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.Collections;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

@Component
public class CaseTransformer {
    private final DefendantAddressTransformer defendantAddressTransformer;
    private final OffenceTransformer offenceTransformer;

    public CaseTransformer(DefendantAddressTransformer defendantAddressTransformer, OffenceTransformer offenceTransformer) {
        this.defendantAddressTransformer = defendantAddressTransformer;
        this.offenceTransformer = offenceTransformer;
    }

    public List<Case> toCases(CasesType cases) {
        return cases.getCase().stream().map(this::toCase).collect(Collectors.toList());
    }

    private Case toCase(CaseType _case) {
        return Case
                .builder()
                .id(_case.getCId())
                .asn(_case.getAsn())
                .caseNumber(_case.getCaseno())
                .caseSequence(_case.getCseq())
                .informant(_case.getInf())
                .estimatedDuration(_case.getEstdur())
                .urn(_case.getUrn())
                .listingNumber(_case.getListno())
                .markers(toMarkers(_case.getMarker()))
                .defendant(toDefendant(_case))
                .bailConditions(_case.getBailcond())
                .additionalInformation(_case.getAddinfo())
                .offences(Optional.ofNullable(_case.getOffences()).map(offenceTransformer::toOffences).orElse(null))
                .build();
    }

    private List<String> toMarkers(String marker) {
        return Optional.ofNullable(marker).map(m -> split(marker)).orElse(Collections.emptyList());
    }

    private List<String> split(String marker) {
        return IntStream
                .range(0, marker.length() - 1)
                .filter(n -> (n%2)==0)
                .mapToObj(markerIndex -> marker.substring(markerIndex, markerIndex+2))
                .collect(Collectors.toList());

    }
    private Defendant toDefendant(CaseType _case) {
        return Defendant
                .builder()
                .name(_case.getDefName())
                .dateOfBirth(DateTimeHelper.asDate(_case.getDefDob()))
                .gender(_case.getDefSex())
                .address(Optional.ofNullable(_case.getDefAddr()).map(defendantAddressTransformer::toAddress).orElse(null))
                .build();
    }


}
