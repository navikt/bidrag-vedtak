ALTER TABLE vedtak
ADD COLUMN IF NOT EXISTS unik_referanse TEXT;

CREATE UNIQUE INDEX idx_vedtak_unik_referanse ON vedtak(unik_referanse)
WHERE unik_referanse IS NOT NULL;