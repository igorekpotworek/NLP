ALTER TABLE ARTYKULY_WIADOMOSCI ADD COLUMN WEKTOR TSVECTOR;

UPDATE ARTYKULY_WIADOMOSCI SET WEKTOR=to_tsvector('public.polish', tytul || ' ' || wstep || ' ' || tekst);

CREATE INDEX ARTYKULY_GIN ON ARTYKULY_WIADOMOSCI USING GIN (WEKTOR);

CREATE TRIGGER TS_ARTUKULY BEFORE INSERT OR UPDATE ON ARTYKULY_WIADOMOSCI 
FOR EACH ROW EXECUTE PROCEDURE tsvector_update_trigger(WEKTOR,'public.polish',tytul, wstep, tekst);

select * from ARTYKULY_WIADOMOSCI where WEKTOR @@ to_tsquery('public.polish', 'moderacji') limit 1;



ALTER TABLE articles ADD COLUMN vector TSVECTOR;

UPDATE articles SET vector=to_tsvector('public.polish', title || ' ' || intro || ' ' || text);

CREATE INDEX articles_gin ON articles USING GIN (vector);

CREATE TRIGGER TS_ARTICLES BEFORE INSERT OR UPDATE ON articles 
FOR EACH ROW EXECUTE PROCEDURE tsvector_update_trigger(vector,'public.polish',title, intro, text);