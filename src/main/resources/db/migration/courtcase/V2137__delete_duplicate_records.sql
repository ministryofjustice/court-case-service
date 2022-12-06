BEGIN;

DELETE FROM hearing h WHERE h.id  NOT IN (SELECT max(id) FROM hearing h2 GROUP BY  h2.hearing_id);

DELETE FROM defendant d WHERE d.defendant_id  NOT IN (SELECT hd.defendant_id FROM hearing_defendant hd);

COMMIT;