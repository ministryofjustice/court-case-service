BEGIN;

DROP TABLE IF EXISTS court_case_new;

CREATE TABLE court_case_new AS SELECT * FROM court_case cc WHERE cc.id IN (SELECT max(id) FROM court_case cc2 GROUP BY cc2.case_id);

ALTER TABLE court_case_new ADD PRIMARY KEY (id);

ALTER TABLE court_case_new
    ALTER COLUMN id
        ADD GENERATED BY DEFAULT AS IDENTITY;

ALTER TABLE ONLY court_case_new ALTER COLUMN deleted SET DEFAULT false;

ALTER TABLE ONLY court_case_new ALTER COLUMN created set DEFAULT NOW();

CREATE INDEX case_id_new_idx ON court_case_new USING btree (case_id);

ALTER TABLE hearing  ADD COLUMN fk_court_case_id_2 int8;

UPDATE hearing SET fk_court_case_id_2 = ccn.id FROM hearing h, court_case cc, court_case_new ccn
WHERE h.fk_court_case_id = cc.id AND cc.case_id = ccn.case_id AND hearing.id = h.id;

ALTER TABLE hearing
    RENAME COLUMN fk_court_case_id TO fk_court_case_id_remove;

ALTER TABLE hearing
    ALTER COLUMN fk_court_case_id_remove DROP NOT NULL;


ALTER TABLE hearing
    RENAME COLUMN fk_court_case_id_2 TO fk_court_case_id;


ALTER TABLE court_case RENAME TO court_case_old;

ALTER TABLE court_case_new RENAME TO court_case;

COMMIT;