package uk.gov.justice.probation.courtcaseservice.controller.model;

/*
* {
  case: {
     caseId: foo,
     hearings: {
       hearingId: bar,
       hearingVersions: [
          {
            hearingDay: 2022-01-01,
            courtCode: B10JQ,
            defendant: {...}
          },
          ...
       ],
     }
  }
}
*
* */

import java.time.LocalDate;
import java.util.List;

public class CourtCaseHistory {
    private String caseId;
    private String urn;
    private String source;
    private List<Defendant> defendants; // hearing versions refer to these defendants using defendantIds
    private List<Hearing> hearings; // list of hearings; each hearing pertains to 1 hearingId and holds info of it's update history (a hearing versions)
}

class Hearing { // pertains to 1 hearingId on a case
    private String hearingId;
    private List<HearingVersion> hearingVersions;
}

class HearingVersion { // A version pertains to a hearing update
    private LocalDate hearingDay;
    private String courtCode;
    private List<String> defandantIds; // Keeping defendants here avoid having duplicate defendant data in each and every version
}

