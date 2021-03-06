CREATE TABLE TMP_TECH (
_cached_page_id VARCHAR,
_template integer,
_type VARCHAR,
tekst VARCHAR,
tytul VARCHAR,
url VARCHAR,
wstep VARCHAR
);

CREATE TABLE TMP_SPORT (
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

CREATE TABLE TMP_HEALTH(
_cached_page_id VARCHAR,
_template integer,
_type VARCHAR,
tekst VARCHAR,
tytul VARCHAR,
url VARCHAR,
wstep VARCHAR
);

COPY TMP_TECH FROM 'D:\items_tech.csv' DELIMITER ',' CSV HEADER;
COPY TMP_POLITICS FROM 'D:\items_politics.csv' DELIMITER ',' CSV HEADER;
COPY TMP_HEALTH FROM 'D:\items_health.csv' DELIMITER ',' CSV HEADER;
COPY TMP_SPORT FROM 'D:\items_sport.csv' DELIMITER ',' CSV HEADER;

COPY TMP_TECH FROM '/home/data/items_tech.csv' DELIMITER ',' CSV HEADER;
COPY TMP_POLITICS FROM '/home/data/items_politics.csv' DELIMITER ',' CSV HEADER;
COPY TMP_HEALTH FROM '/home/data/items_health.csv' DELIMITER ',' CSV HEADER;
COPY TMP_SPORT FROM '/home/data/items_sport.csv' DELIMITER ',' CSV HEADER;

INSERT INTO articles (title, intro, text, category ) 
	select tytul, wstep, tekst, 'TECH' from TMP_TECH
	where url ~ '.*,wiadomosc\.html.*' and (wstep is not null and wstep!='') or (tekst is not null and tekst!='');

INSERT INTO articles (title, intro, text, category ) 
	select tytul, intro, tekst, 'POLITICS' from TMP_POLITICS
	where url ~ '.*,wiadomosc\.html.*' and (intro is not null and intro!='') or (tekst is not null and tekst!='');
	
INSERT INTO articles (title, intro, text, category ) 
	select tytul, wstep, tekst, 'HEALTH' from TMP_HEALTH
	where ((wstep is not null and wstep!='') or (tekst is not null and tekst!=''));
	
INSERT INTO articles (title, intro, text, category ) 
	select tytul, wstep, tekst, 'SPORT' from TMP_SPORT
	where url ~ '.*,wiadomosc\.html.*' and (wstep is not null and wstep!='') or (tekst is not null and tekst!='');

delete from articles where text~'Programy do pobrania:';

DELETE FROM articles WHERE ctid NOT IN (SELECT max(ctid) FROM articles GROUP BY title);
