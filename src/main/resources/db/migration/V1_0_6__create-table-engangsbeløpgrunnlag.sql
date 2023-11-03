-- Table: engangsbeløpgrunnlag

-- DROP TABLE engangsbeløpgrunnlag;

CREATE TABLE IF NOT EXISTS engangsbeløpgrunnlag
(
    engangsbeløpsid integer NOT NULL,
    grunnlagsid integer NOT NULL,
    CONSTRAINT engangsbeløpgrunnlag_pkey PRIMARY KEY (engangsbeløpsid, grunnlagsid),
    CONSTRAINT engangsbeløpgrunnlag_id_fk FOREIGN KEY (engangsbeløpsid)
        REFERENCES engangsbeløp (engangsbeløpsid) MATCH SIMPLE
        ON UPDATE NO ACTION
        ON DELETE NO ACTION,
    CONSTRAINT fk_grunnlag_id FOREIGN KEY (grunnlagsid)
        REFERENCES grunnlag (grunnlagsid) MATCH SIMPLE
        ON UPDATE NO ACTION
        ON DELETE NO ACTION,
    UNIQUE (engangsbeløpsid, grunnlagsid)
)

    TABLESPACE pg_default;

CREATE INDEX idx_engangsbeløpgrunnlag_1 ON engangsbeløpgrunnlag(engangsbeløpsid);

CREATE INDEX idx_engangsbeløpgrunnlag_2 ON engangsbeløpgrunnlag(engangsbeløpsid, grunnlagsid);