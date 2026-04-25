CREATE INDEX IF NOT EXISTS idx_artist_name_fts
    ON artist
    USING GIN (to_tsvector('simple', coalesce(name, '')));

CREATE INDEX IF NOT EXISTS idx_album_title_fts
    ON album
    USING GIN (to_tsvector('simple', coalesce(title, '')));
