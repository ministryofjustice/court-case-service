-- =====================================================================
-- PCFS-47: Manual Matches Pre and Post CPR Migration
-- 
-- Purpose: Analyse the volume and distribution of manual defendant matches
--          before and after the switch to CPR on 13th February 2026.
--
-- All queries are split into two windows:
--   PRE-CPR:  13th Nov 2025 → 13th Feb 2026 (3 months before)
--   POST-CPR: 13th Feb 2026 → 13th May 2026 (3 months after)
--
-- Run individually in DBeaver by highlighting the desired query and
-- pressing Ctrl+Enter (or Cmd+Enter on Mac).
-- =====================================================================


-- =====================================================================
-- SECTION 1: Total count of manual matches (sanity check)
-- Use these to confirm overall volumes before drilling into breakdowns.
-- =====================================================================

-- PRE-CPR: Total manual matches in the 3 months before switching to CPR
SELECT 'PRE-CPR' AS period,
       COUNT(*) AS manual_match_count
FROM courtcaseservice.defendant d
LEFT JOIN courtcaseservice.offender o ON d.fk_offender_id = o.id
WHERE d.created >= '2026-02-13'::date - INTERVAL '3 months'
  AND d.created < '2026-02-13'
  AND d.manual_update = true;


-- POST-CPR: Total manual matches in the 3 months after switching to CPR
SELECT 'POST-CPR' AS period,
       COUNT(*) AS manual_match_count
FROM courtcaseservice.defendant d
LEFT JOIN courtcaseservice.offender o ON d.fk_offender_id = o.id
WHERE d.created >= '2026-02-13'
  AND d.created < '2026-02-13'::date + INTERVAL '3 months'
  AND d.manual_update = true;


-- =====================================================================
-- SECTION 2: Manual matches broken down by court
-- Shows which courts are generating the most manual matches,
-- helping identify if specific courts are driving volume changes post-CPR.
-- Note: a defendant can appear on multiple hearings, so counts reflect
-- hearing-level matches rather than unique defendants.
-- =====================================================================

-- PRE-CPR: Manual matches per court in the 3 months before switching to CPR
SELECT 'PRE-CPR' AS period,
       c.court_code,
       c.name,
       COUNT(*) AS manual_match_count
FROM courtcaseservice.defendant d
LEFT JOIN courtcaseservice.offender o ON d.fk_offender_id = o.id
JOIN courtcaseservice.hearing_defendant hdef ON hdef.defendant_id = d.defendant_id
JOIN courtcaseservice.hearing h ON hdef.fk_hearing_id = h.id
JOIN courtcaseservice.hearing_day hd ON hd.fk_hearing_id = h.id
JOIN courtcaseservice.court c ON c.court_code = hd.court_code
WHERE d.created >= '2026-02-13'::date - INTERVAL '3 months'
  AND d.created < '2026-02-13'
  AND d.manual_update = true
GROUP BY c.court_code, c.name
ORDER BY COUNT(*) DESC;


-- POST-CPR: Manual matches per court in the 3 months after switching to CPR
SELECT 'POST-CPR' AS period,
       c.court_code,
       c.name,
       COUNT(*) AS manual_match_count
FROM courtcaseservice.defendant d
LEFT JOIN courtcaseservice.offender o ON d.fk_offender_id = o.id
JOIN courtcaseservice.hearing_defendant hdef ON hdef.defendant_id = d.defendant_id
JOIN courtcaseservice.hearing h ON hdef.fk_hearing_id = h.id
JOIN courtcaseservice.hearing_day hd ON hd.fk_hearing_id = h.id
JOIN courtcaseservice.court c ON c.court_code = hd.court_code
WHERE d.created >= '2026-02-13'
  AND d.created < '2026-02-13'::date + INTERVAL '3 months'
  AND d.manual_update = true
GROUP BY c.court_code, c.name
ORDER BY COUNT(*) DESC;


-- =====================================================================
-- SECTION 3: Manual matches broken down by source type (Common Platform vs Libra)
-- Shows the proportion of manual matches originating from each source system.
-- A significant shift in distribution post-CPR may indicate matching
-- quality differences between source systems.
--
-- Note: A monthly breakdown is not possible as the defendant record does
-- not store a dedicated "matched_at" timestamp. The `updated` column is
-- not reliable for this purpose as it is updated whenever the defendant
-- record changes for any reason.
-- =====================================================================

-- PRE-CPR: Manual matches by source type in the 3 months before switching to CPR
SELECT 'PRE-CPR' AS period,
       cc.source_type,
       COUNT(*) AS manual_match_count,
       ROUND(COUNT(*) * 100.0 / SUM(COUNT(*)) OVER (), 2) AS percentage
FROM courtcaseservice.defendant d
LEFT JOIN courtcaseservice.offender o ON d.fk_offender_id = o.id
JOIN courtcaseservice.hearing_defendant hdef ON hdef.defendant_id = d.defendant_id
JOIN courtcaseservice.hearing h ON hdef.fk_hearing_id = h.id
JOIN courtcaseservice.hearing_day hd ON hd.fk_hearing_id = h.id
JOIN courtcaseservice.court c ON c.court_code = hd.court_code
JOIN courtcaseservice.court_case cc ON h.fk_court_case_id = cc.id
WHERE d.created >= '2026-02-13'::date - INTERVAL '3 months'
  AND d.created < '2026-02-13'
  AND d.manual_update = true
GROUP BY cc.source_type
ORDER BY COUNT(*) DESC;


-- POST-CPR: Manual matches by source type in the 3 months after switching to CPR
SELECT 'POST-CPR' AS period,
       cc.source_type,
       COUNT(*) AS manual_match_count,
       ROUND(COUNT(*) * 100.0 / SUM(COUNT(*)) OVER (), 2) AS percentage
FROM courtcaseservice.defendant d
LEFT JOIN courtcaseservice.offender o ON d.fk_offender_id = o.id
JOIN courtcaseservice.hearing_defendant hdef ON hdef.defendant_id = d.defendant_id
JOIN courtcaseservice.hearing h ON hdef.fk_hearing_id = h.id
JOIN courtcaseservice.hearing_day hd ON hd.fk_hearing_id = h.id
JOIN courtcaseservice.court c ON c.court_code = hd.court_code
JOIN courtcaseservice.court_case cc ON h.fk_court_case_id = cc.id
WHERE d.created >= '2026-02-13'
  AND d.created < '2026-02-13'::date + INTERVAL '3 months'
  AND d.manual_update = true
GROUP BY cc.source_type
ORDER BY COUNT(*) DESC;


-- =====================================================================
-- SECTION 4: Manual matches broken down by source type and breach status
-- Adds breach status to the source type breakdown to determine whether
-- the CPR migration has improved or worsened matching for defendants
-- who are on a breach. A reduction in manual matches for breached
-- defendants post-CPR would indicate an improvement in auto-matching.
-- =====================================================================

-- PRE-CPR: Manual matches by source type and breach status in the 3 months before switching to CPR
SELECT 'PRE-CPR' AS period,
       cc.source_type,
       o.breach,
       COUNT(*) AS manual_match_count,
       ROUND(COUNT(*) * 100.0 / SUM(COUNT(*)) OVER (), 2) AS percentage
FROM courtcaseservice.defendant d
LEFT JOIN courtcaseservice.offender o ON d.fk_offender_id = o.id
JOIN courtcaseservice.hearing_defendant hdef ON hdef.defendant_id = d.defendant_id
JOIN courtcaseservice.hearing h ON hdef.fk_hearing_id = h.id
JOIN courtcaseservice.hearing_day hd ON hd.fk_hearing_id = h.id
JOIN courtcaseservice.court c ON c.court_code = hd.court_code
JOIN courtcaseservice.court_case cc ON h.fk_court_case_id = cc.id
WHERE d.created >= '2026-02-13'::date - INTERVAL '3 months'
  AND d.created < '2026-02-13'
  AND d.manual_update = true
GROUP BY cc.source_type, o.breach
ORDER BY COUNT(*) DESC;


-- POST-CPR: Manual matches by source type and breach status in the 3 months after switching to CPR
SELECT 'POST-CPR' AS period,
       cc.source_type,
       o.breach,
       COUNT(*) AS manual_match_count,
       ROUND(COUNT(*) * 100.0 / SUM(COUNT(*)) OVER (), 2) AS percentage
FROM courtcaseservice.defendant d
LEFT JOIN courtcaseservice.offender o ON d.fk_offender_id = o.id
JOIN courtcaseservice.hearing_defendant hdef ON hdef.defendant_id = d.defendant_id
JOIN courtcaseservice.hearing h ON hdef.fk_hearing_id = h.id
JOIN courtcaseservice.hearing_day hd ON hd.fk_hearing_id = h.id
JOIN courtcaseservice.court c ON c.court_code = hd.court_code
JOIN courtcaseservice.court_case cc ON h.fk_court_case_id = cc.id
WHERE d.created >= '2026-02-13'
  AND d.created < '2026-02-13'::date + INTERVAL '3 months'
  AND d.manual_update = true
GROUP BY cc.source_type, o.breach
ORDER BY COUNT(*) DESC;