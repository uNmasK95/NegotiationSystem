CREATE TABLE utilizadores
(
  username character varying(45) NOT NULL,
  password character varying(45) NOT NULL,
  CONSTRAINT pk_username PRIMARY KEY (username)
)
