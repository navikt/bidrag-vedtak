ALTER TABLE stønadsendring
    ADD COLUMN ekstern_referanse varchar(20);
ALTER TABLE stønadsendring
    ADD COLUMN omgjor_vedtak_id integer;