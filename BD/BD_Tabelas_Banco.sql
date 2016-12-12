CREATE TABLE utilizadores
(
  username character varying(45) NOT NULL,
  saldo numeric(11,2) NOT NULL DEFAULT 0,
  CONSTRAINT pk_username PRIMARY KEY (username)
)
