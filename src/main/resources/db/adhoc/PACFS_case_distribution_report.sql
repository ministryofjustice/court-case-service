SELECT
    c.court_code,
    c."name" AS court_name,

    -- January
    COUNT(*) FILTER (WHERE date_trunc('month', hd.hearing_day) = DATE '2026-01-01') AS January_2026_cases,
    COUNT(DISTINCT hd.hearing_day::date) FILTER (WHERE date_trunc('month', hd.hearing_day) = DATE '2026-01-01') AS January_2026_days,
    ROUND(
            COUNT(*) FILTER (WHERE date_trunc('month', hd.hearing_day) = DATE '2026-01-01')::numeric /
        NULLIF(COUNT(DISTINCT hd.hearing_day::date) FILTER (WHERE date_trunc('month', hd.hearing_day) = DATE '2026-01-01'), 0),
            1
    ) AS January_2026_avg,

    -- February
    COUNT(*) FILTER (WHERE date_trunc('month', hd.hearing_day) = DATE '2026-02-01') AS February_2026_cases,
    COUNT(DISTINCT hd.hearing_day::date) FILTER (WHERE date_trunc('month', hd.hearing_day) = DATE '2026-02-01') AS February_2026_days,
    ROUND(
            COUNT(*) FILTER (WHERE date_trunc('month', hd.hearing_day) = DATE '2026-02-01')::numeric /
        NULLIF(COUNT(DISTINCT hd.hearing_day::date) FILTER (WHERE date_trunc('month', hd.hearing_day) = DATE '2026-02-01'), 0),
            1
    ) AS February_2026_avg,

    -- March
    COUNT(*) FILTER (WHERE date_trunc('month', hd.hearing_day) = DATE '2026-03-01') AS March_2026_cases,
    COUNT(DISTINCT hd.hearing_day::date) FILTER (WHERE date_trunc('month', hd.hearing_day) = DATE '2026-03-01') AS March_2026_days,
    ROUND(
            COUNT(*) FILTER (WHERE date_trunc('month', hd.hearing_day) = DATE '2026-03-01')::numeric /
        NULLIF(COUNT(DISTINCT hd.hearing_day::date) FILTER (WHERE date_trunc('month', hd.hearing_day) = DATE '2026-03-01'), 0),
            1
    ) AS March_2026_avg,

    -- April
    COUNT(*) FILTER (WHERE date_trunc('month', hd.hearing_day) = DATE '2026-04-01') AS April_2026_cases,
    COUNT(DISTINCT hd.hearing_day::date) FILTER (WHERE date_trunc('month', hd.hearing_day) = DATE '2026-04-01') AS April_2026_days,
    ROUND(
            COUNT(*) FILTER (WHERE date_trunc('month', hd.hearing_day) = DATE '2026-04-01')::numeric /
        NULLIF(COUNT(DISTINCT hd.hearing_day::date) FILTER (WHERE date_trunc('month', hd.hearing_day) = DATE '2026-04-01'), 0),
            1
    ) AS April_2026_avg,

    -- May
    COUNT(*) FILTER (WHERE date_trunc('month', hd.hearing_day) = DATE '2026-05-01') AS May_2026_cases,
    COUNT(DISTINCT hd.hearing_day::date) FILTER (WHERE date_trunc('month', hd.hearing_day) = DATE '2026-05-01') AS May_2026_days,
    ROUND(
            COUNT(*) FILTER (WHERE date_trunc('month', hd.hearing_day) = DATE '2026-05-01')::numeric /
        NULLIF(COUNT(DISTINCT hd.hearing_day::date) FILTER (WHERE date_trunc('month', hd.hearing_day) = DATE '2026-05-01'), 0),
            1
    ) AS May_2026_avg,

    -- June
    COUNT(*) FILTER (WHERE date_trunc('month', hd.hearing_day) = DATE '2026-06-01') AS June_2026_cases,
    COUNT(DISTINCT hd.hearing_day::date) FILTER (WHERE date_trunc('month', hd.hearing_day) = DATE '2026-06-01') AS June_2026_days,
    ROUND(
            COUNT(*) FILTER (WHERE date_trunc('month', hd.hearing_day) = DATE '2026-06-01')::numeric /
        NULLIF(COUNT(DISTINCT hd.hearing_day::date) FILTER (WHERE date_trunc('month', hd.hearing_day) = DATE '2026-06-01'), 0),
            1
    ) AS June_2026_avg,

    -- July
    COUNT(*) FILTER (WHERE date_trunc('month', hd.hearing_day) = DATE '2026-07-01') AS July_2026_cases,
    COUNT(DISTINCT hd.hearing_day::date) FILTER (WHERE date_trunc('month', hd.hearing_day) = DATE '2026-07-01') AS July_2026_days,
    ROUND(
            COUNT(*) FILTER (WHERE date_trunc('month', hd.hearing_day) = DATE '2026-07-01')::numeric /
        NULLIF(COUNT(DISTINCT hd.hearing_day::date) FILTER (WHERE date_trunc('month', hd.hearing_day) = DATE '2026-07-01'), 0),
            1
    ) AS July_2026_avg,

    -- August
    COUNT(*) FILTER (WHERE date_trunc('month', hd.hearing_day) = DATE '2026-08-01') AS August_2026_cases,
    COUNT(DISTINCT hd.hearing_day::date) FILTER (WHERE date_trunc('month', hd.hearing_day) = DATE '2026-08-01') AS August_2026_days,
    ROUND(
            COUNT(*) FILTER (WHERE date_trunc('month', hd.hearing_day) = DATE '2026-08-01')::numeric /
        NULLIF(COUNT(DISTINCT hd.hearing_day::date) FILTER (WHERE date_trunc('month', hd.hearing_day) = DATE '2026-08-01'), 0),
            1
    ) AS August_2026_avg,

    -- September
    COUNT(*) FILTER (WHERE date_trunc('month', hd.hearing_day) = DATE '2026-09-01') AS September_2026_cases,
    COUNT(DISTINCT hd.hearing_day::date) FILTER (WHERE date_trunc('month', hd.hearing_day) = DATE '2026-09-01') AS September_2026_days,
    ROUND(
            COUNT(*) FILTER (WHERE date_trunc('month', hd.hearing_day) = DATE '2026-09-01')::numeric /
        NULLIF(COUNT(DISTINCT hd.hearing_day::date) FILTER (WHERE date_trunc('month', hd.hearing_day) = DATE '2026-09-01'), 0),
            1
    ) AS September_2026_avg,

    -- October
    COUNT(*) FILTER (WHERE date_trunc('month', hd.hearing_day) = DATE '2026-10-01') AS October_2026_cases,
    COUNT(DISTINCT hd.hearing_day::date) FILTER (WHERE date_trunc('month', hd.hearing_day) = DATE '2026-10-01') AS October_2026_days,
    ROUND(
            COUNT(*) FILTER (WHERE date_trunc('month', hd.hearing_day) = DATE '2026-10-01')::numeric /
        NULLIF(COUNT(DISTINCT hd.hearing_day::date) FILTER (WHERE date_trunc('month', hd.hearing_day) = DATE '2026-10-01'), 0),
            1
    ) AS October_2026_avg,

    -- November
    COUNT(*) FILTER (WHERE date_trunc('month', hd.hearing_day) = DATE '2026-11-01') AS November_2026_cases,
    COUNT(DISTINCT hd.hearing_day::date) FILTER (WHERE date_trunc('month', hd.hearing_day) = DATE '2026-11-01') AS November_2026_days,
    ROUND(
            COUNT(*) FILTER (WHERE date_trunc('month', hd.hearing_day) = DATE '2026-11-01')::numeric /
        NULLIF(COUNT(DISTINCT hd.hearing_day::date) FILTER (WHERE date_trunc('month', hd.hearing_day) = DATE '2026-11-01'), 0),
            1
    ) AS November_2026_avg,

    -- December
    COUNT(*) FILTER (WHERE date_trunc('month', hd.hearing_day) = DATE '2026-12-01') AS December_2026_cases,
    COUNT(DISTINCT hd.hearing_day::date) FILTER (WHERE date_trunc('month', hd.hearing_day) = DATE '2026-12-01') AS December_2026_days,
    ROUND(
            COUNT(*) FILTER (WHERE date_trunc('month', hd.hearing_day) = DATE '2026-12-01')::numeric /
        NULLIF(COUNT(DISTINCT hd.hearing_day::date) FILTER (WHERE date_trunc('month', hd.hearing_day) = DATE '2026-12-01'), 0),
            1
    ) AS December_2026_avg

FROM courtcaseservice.court c
         JOIN courtcaseservice.hearing_day hd
              ON c.court_code = hd.court_code
         JOIN courtcaseservice.hearing h
              ON hd.fk_hearing_id = h.id

WHERE hd.hearing_day >= DATE '2026-01-01'
  AND hd.hearing_day <  DATE '2027-01-01'
  AND lower(c.name) not LIKE '%crown court%'

GROUP BY
    c.court_code,
    c."name"

ORDER BY
    c."name" ASC;