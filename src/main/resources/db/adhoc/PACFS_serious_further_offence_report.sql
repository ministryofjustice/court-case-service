-- =====================================================================
-- PIC-5367: Serious Further Offence Report
--
-- Purpose: Daily report to be done week of 11th May 2026 to identify any SFOs that have been committed by defendants on probation.
--
--
-- Run individually in DBeaver by highlighting the desired query and
-- pressing Ctrl+Enter (or Cmd+Enter on Mac).
-- =====================================================================

select  o.summary offence_summary,
        o.title offence_title,
        o.act offence_act,
        o.offence_code,
        c.court_code,
        c.name court_name,
        hd.hearing_day,
        d.defendant_name,
        d.date_of_birth,
        cc.source_type,
        cc.urn,
        off.probation_status
FROM courtcaseservice.court c
JOIN courtcaseservice.hearing_day hd          ON c.court_code = hd.court_code
JOIN courtcaseservice.hearing h               ON hd.fk_hearing_id = h.id
JOIN courtcaseservice.hearing_defendant hdef  ON hdef.fk_hearing_id = h.id
JOIN courtcaseservice.defendant d             ON hdef.defendant_id = d.defendant_id
JOIN courtcaseservice.offender off            ON off.id = d.fk_offender_id
JOIN courtcaseservice.court_case cc           ON h.fk_court_case_id = cc.id
JOIN courtcaseservice.offence o               ON hdef.id = o.fk_hearing_defendant_id
JOIN courtcaseservice.offence_sfo_mapping osm ON o.offence_code = osm.offence_code
where hd.hearing_day = '2026-05-08'
AND osm.sfo_flag IS true
AND lower(c.name) LIKE '%magistrates%'
order by c.name