-- ALTER TABLE engangsbeløp
--     DROP CONSTRAINT engangsbeløp_vedtak_id_lopenr_key;

ALTER TABLE engangsbeløp
    ADD UNIQUE (vedtak_id, referanse) ;
