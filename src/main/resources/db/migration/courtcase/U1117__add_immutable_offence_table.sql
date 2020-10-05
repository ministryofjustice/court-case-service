BEGIN;
-- NOTE: Undo migrations are only supported in the paid version of Flyway so we'll have to convert this to a versioned
-- migration if we want it applied. It was created as a fallback so we can deploy quickly should we need it.
CREATE TABLE immutable_court_case_bk AS TABLE court_case;
CREATE TABLE immutable_offence_bk AS TABLE offence;

DROP TABLE offence;
DROP TABLE court_case;
ALTER TABLE offence_bk RENAME TO offence;
ALTER TABLE court_case_bk RENAME TO court_case;

COMMIT;
