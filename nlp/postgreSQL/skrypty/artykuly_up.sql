CREATE TABLE ARTYKULY_WIADOMOSCI (
id SERIAL,
tytul varchar,
wstep varchar,
tekst varchar);

CREATE TABLE ARTYKULY_SPORT (
id SERIAL,
tytul varchar,
wstep varchar,
tekst varchar);

CREATE TABLE TMP_WIADOMOSCI (
_cached_page_id VARCHAR,
_template integer,
_type VARCHAR,
intro VARCHAR,
tekst VARCHAR,
tytul VARCHAR,
url VARCHAR);

CREATE TABLE TMP_SPORT (
_cached_page_id VARCHAR,
_template integer,
_type VARCHAR,
wstep VARCHAR,
tekst VARCHAR,
tytul VARCHAR,
url VARCHAR);


CREATE TABLE articles (
id SERIAL,
title varchar,
intro varchar,
text varchar);


