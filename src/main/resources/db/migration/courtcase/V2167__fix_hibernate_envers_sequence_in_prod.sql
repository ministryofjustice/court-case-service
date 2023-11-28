DO $$
BEGIN
EXECUTE format('CREATE sequence if not exists revinfo_seq INCREMENT BY 50 start with %s', (SELECT max(rev) + 1 FROM revinfo));
END
$$
LANGUAGE plpgsql;