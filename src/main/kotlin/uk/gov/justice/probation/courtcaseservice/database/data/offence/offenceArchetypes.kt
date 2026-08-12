package uk.gov.justice.probation.courtcaseservice.database.data.offence


val offenceArchetypes = listOf(
  // Note: for offenceCode in PAC, we use CJS codes, not the Home Office Offence codes
  OffenceArchetype(
    title = "Robbery",
    summary = "On or in 4 April 2026 at Sunderland robbed person of alcohol, a till and contents of a value unknown.",
    act = "Contrary to section 8(1) of the Theft Act 1968.",
    offenceCode = "TH68023" // Will flag an SFO, as in the lookup table
  ),
  OffenceArchetype(
    title = "Robbery",
    summary = "Acquisition, use or possession of criminal property",
    act = "Section 329 of the Proceeds of Crime Act 2002",
    offenceCode = "03803"
  ),
  OffenceArchetype(
    title = "Theft offences",
    summary = "Theft of pedal cycle",
    act = "Section 12(5) of the Theft Act 1968",
    offenceCode = "04400"
  ),
  OffenceArchetype(
    title = "Fraud offences",
    summary = "Refusal or failure to produce book makers permit on request by a Constable.",
    act = "Section 2(3) of the Betting, Gaming and Lotteries Act 1963",
    offenceCode = "10629"
  ),
  OffenceArchetype(
    title = "Summary motoring offences",
    summary = "Drive on a horse drawn vehicle on a footpath",
    act = "Section 72 of the Highways Act 1835",
    offenceCode = "13501"
  ),
  OffenceArchetype(
    title = "Criminal damage and arson",
    summary = "On 01 July 2026 at Sunderland committed arson in that without lawful excuse destroyed by fire Flat 12, Willow Place, South High Road of a value unknown belonging to North Tyneside Council intending to destroy or damage property or being reckless as to whether property would be destroyed or damaged and being reckless as to whether the life of another would be thereby endangered",
    act = "Contrary to sections 1(2), 1(3) and 4 of the Criminal Damage Act 1971",
    offenceCode = "CD71050" // Will flag an SFO, as in the lookup table
  ),
)