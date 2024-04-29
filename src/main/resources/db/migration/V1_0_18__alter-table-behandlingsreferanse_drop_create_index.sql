DROP INDEX idx_behandlingsreferanse_2;
COMMIT;
CREATE INDEX idx_behandlingsreferanse_2 ON behandlingsreferanse(vedtaksid, kilde, referanse);