CREATE TABLE TMP_TECH (
_cached_page_id VARCHAR,
_template integer,
_type VARCHAR,
tekst VARCHAR,
tytul VARCHAR,
url VARCHAR,
wstep VARCHAR
);

CREATE TABLE TMP_POLITICS (
_cached_page_id VARCHAR,
_template integer,
_type VARCHAR,
intro VARCHAR,
tekst VARCHAR,
tytul VARCHAR,
url VARCHAR
);

CREATE TABLE TMP_FINANCE (
_cached_page_id VARCHAR,
_template integer,
_type VARCHAR,
intro VARCHAR,
tekst VARCHAR,
tytul VARCHAR,
url VARCHAR
);

CREATE TABLE TMP_HEALTH(
_cached_page_id VARCHAR,
_template integer,
_type VARCHAR,
tekst VARCHAR,
tytul VARCHAR,
url VARCHAR,
wstep VARCHAR
);


CREATE TABLE articles (
id SERIAL,
title varchar,
intro varchar,
text varchar,
category varchar);

CREATE INDEX ON articles (category);


