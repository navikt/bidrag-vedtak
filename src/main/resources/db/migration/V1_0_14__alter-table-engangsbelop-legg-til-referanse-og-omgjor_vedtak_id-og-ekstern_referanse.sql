ALTER TABLE engangsbeløp
    RENAME COLUMN referanse TO delytelse_id;
ALTER TABLE engangsbeløp
    ADD COLUMN ekstern_referanse varchar(20);
ALTER TABLE engangsbeløp
    ADD COLUMN referanse varchar(20);
ALTER TABLE engangsbeløp
    ADD COLUMN omgjor_vedtak_id integer;
ALTER TABLE engangsbeløp
    DROP COLUMN endrer_engangsbeløp_id;
ALTER TABLE engangsbeløp
    DROP COLUMN lopenr;