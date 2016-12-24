drop user if exists acoes;
drop database if exists acoes;
create user acoes;
create database acoes with owner acoes;

\connect acoes acoes;

CREATE TABLE acoes
(
  empresa character varying(45) NOT NULL,
  utilizador character varying(45) NOT NULL,
  quantidade integer NOT NULL,
  CONSTRAINT pk_empresa_utilizador PRIMARY KEY (empresa, utilizador)
)
