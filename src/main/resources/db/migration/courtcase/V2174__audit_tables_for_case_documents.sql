CREATE TABLE case_defendant_aud (
     id int8 NOT NULL,
     rev int4 NOT NULL,
     revtype int2 NULL,
     fk_court_case_id int8 NULL,
     fk_case_defendant_id int8 NULL,
     CONSTRAINT case_defendant_aud_pkey PRIMARY KEY (rev, id)
);

CREATE TABLE IF NOT EXISTS case_defendant_documents_aud (
   id int8 NOT NULL,
   rev int4 NOT NULL,
   revtype int2 NULL,
   document_id varchar(255) NULL,
   document_name varchar(255) NULL,
   fk_case_defendant_id int8 NULL,
   CONSTRAINT case_defendant_documents_aud_pkey PRIMARY KEY (rev, id)
);

ALTER TABLE case_defendant_aud ADD CONSTRAINT fk_case_defendant_documents_aud_rev FOREIGN KEY (rev) REFERENCES revinfo(rev);

ALTER TABLE case_defendant_documents_aud ADD CONSTRAINT fk_case_defendant_documents_aud_rev FOREIGN KEY (rev) REFERENCES revinfo(rev);