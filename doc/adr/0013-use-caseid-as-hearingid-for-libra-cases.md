# 13. For Libra cases, use the caseId as the hearingId

Date: 2022-05-18

## Status

Accepted

## Context

`court-case-service` was originally built solely to handle cases from Libra. Later, we made significant changes to support Common Platform data as well. Relatively late in the day we discovered that we were handling Common Platform data incorrectly, and to resolve this we would have to introduce the concept of a `hearing` which does not exist as a separate entity to a case in Libra. This work started with ticket [PIC-2023](https://dsdmoj.atlassian.net/browse/PIC-2023). At this point the Common Platform integration had been substantially completed and we did not want to break existing functionality whilst these further changes took place. This meant that in the interim both Libra and Common Platform cases had all of their `hearingId` set to the same value as their `caseId`. For Common Platform, this was always intended as a temporary measure and these id's are now completely independent values. However, as there is no separate concept of a `hearingId` for Libra cases we continue using the `caseId` for both ids. 

Note - Libra cases also don't have a separate concept of `defendantId` but this relationship was well understood from the outset so we took the decision to generate a unique uuid as `defendantId` for Libra cases. The only reason we have not taken this approach again was to allow the application to continue operating whilst we made the above changes, so we have knowingly introduced inconsistency, hence this ADR.

## Decision

Use `caseId` for Libra case `hearingId`.

## Consequences

- There is inconsistency between how we treat `hearingId` and `defendantId` relative to `caseId` for Libra cases
- At some point we may want to resolve this by either using unique UUIDs for both, or by using the same id for all 3 ids
