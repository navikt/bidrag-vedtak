-- Table: periode

-- DROP TABLE periodegrunnlag;

CREATE TABLE IF NOT EXISTS periodegrunnlag
(
    periodeid integer NOT NULL,
    grunnlagsid integer NOT NULL,
    CONSTRAINT periodegrunnlag_pkey PRIMARY KEY (periodeid, grunnlagsid),
    CONSTRAINT fk_periode_id FOREIGN KEY (periodeid)
        REFERENCES periode (periodeid) MATCH SIMPLE
        ON UPDATE NO ACTION
        ON DELETE NO ACTION,
    CONSTRAINT fk_grunnlag_id FOREIGN KEY (grunnlagsid)
        REFERENCES grunnlag (grunnlagsid) MATCH SIMPLE
        ON UPDATE NO ACTION
        ON DELETE NO ACTION,
    UNIQUE (periodeid, grunnlagsid)
)

    TABLESPACE pg_default;

CREATE INDEX idx_periodegrunnlag_1 ON periodegrunnlag(periodeid);

CREATE INDEX idx_periodegrunnlag_2 ON periodegrunnlag(periodeid, grunnlagsid);