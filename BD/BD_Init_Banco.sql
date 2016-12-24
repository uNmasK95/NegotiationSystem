drop user if exists banco;
drop database if exists banco;
create user banco;
create database banco with owner banco;

\connect banco banco;

CREATE TABLE utilizadores
(
  username character varying(45) NOT NULL,
  saldo numeric(11,2) NOT NULL DEFAULT 0,
  CONSTRAINT pk_username PRIMARY KEY (username)
)
