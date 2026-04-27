CREATE TABLE offence_sfo_mapping (
                                                      id              SERIAL4         NOT NULL,
                                                      offence_code    TEXT            NOT NULL UNIQUE,
                                                      sfo_flag        BOOLEAN         NOT NULL,

                                                      CONSTRAINT offence_sfo_mapping_pkey PRIMARY KEY (id)
);