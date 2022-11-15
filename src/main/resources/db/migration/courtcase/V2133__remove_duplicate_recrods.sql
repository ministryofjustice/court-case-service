BEGIN;

DROP TABLE IF EXISTS court_case_new;

create table court_case_new as select * from court_case cc where cc.id  in (select max(id) from court_case cc2 group by cc2.case_id);

ALTER TABLE court_case_new ADD PRIMARY KEY (id);

CREATE INDEX case_id_new_idx ON court_case_new USING btree (case_id);

ALTER TABLE hearing  ADD COLUMN fk_court_case_id_2 int8;

update hearing set fk_court_case_id_2 = ccn.id from hearing h, court_case cc, court_case_new ccn
where h.fk_court_case_id = cc.id and cc.case_id = ccn.case_id and hearing.id = h.id;

ALTER TABLE hearing
    RENAME COLUMN fk_court_case_id TO fk_court_case_id_remove;

ALTER TABLE hearing
    ALTER COLUMN fk_court_case_id_remove DROP NOT NULL;


ALTER TABLE hearing
    RENAME COLUMN fk_court_case_id_2 TO fk_court_case_id;


ALTER TABLE court_case RENAME TO court_case_old;

ALTER TABLE court_case_new RENAME TO court_case;

COMMIT;