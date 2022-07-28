package uk.gov.justice.probation.courtcaseservice.controller.model;

/*
* {
  case: {
     caseId: foo,
     hearings: [{
       hearingId: bar,
       hearingVersions: [
          {
            hearingDay: 2022-01-01,
            courtCode: B10JQ,
            defendant: {...}
          },
          ...
       ],
     }]
  }
}
*
* */

import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import uk.gov.justice.probation.courtcaseservice.jpa.entity.CourtCaseEntity;
import uk.gov.justice.probation.courtcaseservice.jpa.entity.DefendantEntity;
import uk.gov.justice.probation.courtcaseservice.jpa.entity.HearingEntity;

import java.util.List;
import java.util.stream.Collectors;

@Builder
@Data
@AllArgsConstructor
@NoArgsConstructor(access = AccessLevel.PRIVATE, force = true)
@JsonInclude(JsonInclude.Include.NON_NULL)
public class CourtCaseHistory {
    private String caseId;
    private String caseNo;
    private String urn;
    private String source;
    private List<DefendantEntity> defendants; // hearing versions refer to these defendants using defendantIds
    private List<HearingHistory> hearings; // list of hearings; each hearing pertains to 1 hearingId and holds info of it's update history (a hearing versions)

    public static CourtCaseHistory of(CourtCaseEntity courtCaseEntity, List<DefendantEntity> defendantEntities) {
        return CourtCaseHistory.builder()
            .caseId(courtCaseEntity.getCaseId())
            .caseNo(courtCaseEntity.getCaseNo())
            .source(courtCaseEntity.getSourceType().name())
            .urn(courtCaseEntity.getUrn())
            .hearings(getHearingHistory(courtCaseEntity))
            .defendants(defendantEntities)
            .build();
    }

    private static List<HearingHistory> getHearingHistory(CourtCaseEntity courtCaseEntity) {
        return courtCaseEntity.getHearings().stream().collect(Collectors.groupingBy(HearingEntity::getHearingId))
            .entrySet().stream().map(stringListEntry -> HearingHistory.of(stringListEntry.getKey(), stringListEntry.getValue()))
            .collect(Collectors.toList());
    }
}