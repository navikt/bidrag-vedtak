ALTER TABLE engangsbelop
    DROP CONSTRAINT engangsbelop_vedtak_id_lopenr_key;

ALTER TABLE engangsbelop
    ADD UNIQUE (vedtak_id, referanse) ;
