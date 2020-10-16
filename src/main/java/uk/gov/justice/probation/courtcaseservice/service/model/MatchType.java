package uk.gov.justice.probation.courtcaseservice.service.model;

public enum MatchType {
    NAME_DOB,
    /** Matches to all the parameters supplied but at least one from the one of the aliases associated to the offender */
    NAME_DOB_ALIAS,
    HMPPS_KEY,
    EXTERNAL_KEY,
    NAME,
    PARTIAL_NAME,
    PARTIAL_NAME_DOB_LENIENT
}
