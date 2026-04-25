CREATE TABLE artist (
                        id BIGSERIAL PRIMARY KEY,
                        name TEXT NOT NULL,
                        external_id TEXT UNIQUE,
                        manually_edited BOOLEAN DEFAULT FALSE
);

CREATE TABLE album (
                       id SERIAL PRIMARY KEY,
                       title TEXT NOT NULL,
                       artist_id BIGINT REFERENCES artist(id),
                       external_id TEXT UNIQUE
);