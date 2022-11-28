ALTER TABLE stonadsendring
    DROP CONSTRAINT stonadsendring_vedtak_id_stonad_type_skyldner_id_kravhaver__key ;

ALTER TABLE stonadsendring
    ADD UNIQUE (vedtak_id, stonad_type, skyldner_id, kravhaver_id, sak_id) ;