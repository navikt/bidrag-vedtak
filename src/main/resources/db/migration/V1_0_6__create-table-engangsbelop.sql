-- Table: engangsbelop

-- DROP TABLE engangsbelop;

CREATE TABLE IF NOT EXISTS engangsbelop
(
    engangsbelop_id integer NOT NULL GENERATED BY DEFAULT AS IDENTITY ( INCREMENT 1 START 1 MINVALUE 1 MAXVALUE 2147483647 CACHE 1 ),
    vedtak_id integer NOT NULL,
    lopenr integer,
    endrer_engangsbelop_id integer,
    type varchar(50) NOT NULL,
    sak_id varchar(20) NOT NULL,
    skyldner_id varchar(20) NOT NULL,
    kravhaver_id varchar(20) NOT NULL,
    mottaker_id varchar(20) NOT NULL,
    belop float,
    valutakode varchar(10),
    resultatkode varchar(255) NOT NULL,
    referanse varchar(32),
    innkreving varchar(20) NOT NULL,
    CONSTRAINT engangsbelop_pkey PRIMARY KEY (engangsbelop_id),
    CONSTRAINT engangsbelop_fk_vedtak_id FOREIGN KEY (vedtak_id)
        REFERENCES vedtak (vedtak_id) MATCH SIMPLE
        ON UPDATE NO ACTION
        ON DELETE NO ACTION,
    UNIQUE (vedtak_id, lopenr)
)

    TABLESPACE pg_default;

CREATE INDEX idx_engangsbelop_1 ON engangsbelop(vedtak_id);