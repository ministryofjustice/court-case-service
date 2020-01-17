ALTER TABLE court_case DROP COLUMN IF EXISTS court_id;
ALTER TABLE court_case ADD COLUMN IF NOT EXISTS court_code TEXT DEFAULT 'SHF';
ALTER TABLE court_case ALTER COLUMN court_code SET NOT NULL;

ALTER TABLE court_case ADD CONSTRAINT court_case_case_no_idempotent UNIQUE (case_no);
