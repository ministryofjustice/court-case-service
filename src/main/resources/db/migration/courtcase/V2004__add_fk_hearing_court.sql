BEGIN;
    ALTER TABLE HEARING ADD CONSTRAINT fk_hearing_court
                    FOREIGN KEY (court_code)
                    REFERENCES court (court_code);
    ALTER TABLE COURT_CASE DROP CONSTRAINT fk_court_case_court;
COMMIT;
