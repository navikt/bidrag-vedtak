-- Table: periode

DROP TABLE periode;

CREATE TABLE IF NOT EXISTS periode
(
    periode_id integer NOT NULL GENERATED BY DEFAULT AS IDENTITY ( INCREMENT 1 START 1 MINVALUE 1 MAXVALUE 2147483647 CACHE 1 ),
    periode_fom date,
    periode_tom date,
    stonadsendring_id integer,
    belop float,
    valutakode character(10),
    resultatkode varchar(255),
    CONSTRAINT periode_pkey PRIMARY KEY (periode_id),
    CONSTRAINT fk_stonad_id FOREIGN KEY (stonadsendring_id)
        REFERENCES stonadsendring (stonadsendring_id) MATCH SIMPLE
        ON UPDATE NO ACTION
        ON DELETE NO ACTION,
    UNIQUE (stonadsendring_id, periode_fom)
)

    TABLESPACE pg_default;