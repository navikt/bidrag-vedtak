ALTER TABLE engangsbelop
    RENAME COLUMN referanse TO delytelse_id;
ALTER TABLE engangsbelop
    ADD COLUMN ekstern_referanse varchar(20);
ALTER TABLE engangsbelop
    ADD COLUMN referanse varchar(20);
ALTER TABLE engangsbelop
    ADD COLUMN omgjor_vedtak_id integer;
ALTER TABLE engangsbelop
    DROP COLUMN endrer_engangsbelop_id;
ALTER TABLE engangsbelop
    DROP COLUMN lopenr;