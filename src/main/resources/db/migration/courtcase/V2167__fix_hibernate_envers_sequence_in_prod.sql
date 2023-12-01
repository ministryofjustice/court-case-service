DO $$
BEGIN

CREATE TABLE IF NOT EXISTS revinfo
(
    rev      int4 NOT NULL,
    revtstmp int8 NULL,
    CONSTRAINT revinfo_pkey PRIMARY KEY (rev)
);

EXECUTE format('CREATE sequence if not exists revinfo_seq INCREMENT BY 50 start with %s', (SELECT COALESCE(max(rev), 0) + 1 FROM revinfo));
END
$$
LANGUAGE plpgsql;