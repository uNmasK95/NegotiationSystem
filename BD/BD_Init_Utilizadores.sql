drop user if exists utilizadores;
drop database if exists utilizadores;
create user utilizadores;
create database utilizadores with owner utilizadores;

\connect utilizadores utilizadores;

CREATE TABLE utilizadores
(
  username character varying(45) NOT NULL,
  password character varying(45) NOT NULL,
  CONSTRAINT pk_username PRIMARY KEY (username)
)
