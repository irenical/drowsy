create table people (id serial primary key, name text, birth timestamptz);
CREATE OR REPLACE FUNCTION create_person(name text, birth timestamptz) RETURNS void AS $$ BEGIN INSERT INTO people(name,birth) VALUES (name, birth); END; $$ LANGUAGE plpgsql;
insert into people(name) values('Boda');
insert into people(name) values('Buda');
