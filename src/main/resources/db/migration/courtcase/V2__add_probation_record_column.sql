ALTER TABLE court_case ADD COLUMN probation_record TEXT;
ALTER TABLE court_case ALTER COLUMN probation_record SET NOT NULL;
