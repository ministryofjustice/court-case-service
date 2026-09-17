CREATE TABLE offence_mappa_mapping (
                                                      id              SERIAL4         NOT NULL,
                                                      offence_code    TEXT            NOT NULL UNIQUE,
                                                      mappa_flag      BOOLEAN         NOT NULL,

                                                      CONSTRAINT offence_mappa_mapping_pkey PRIMARY KEY (id)
);
