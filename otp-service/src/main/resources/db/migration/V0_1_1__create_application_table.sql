CREATE TABLE application (
	app_key           	VARCHAR(10) PRIMARY KEY,
	attempts_allowed 	SMALLINT NOT NULL DEFAULT 2,
	name     			VARCHAR(50) NOT NULL,
	ttl 				SMALLINT NOT NULL DEFAULT 2,
	is_default			BOOLEAN NOT NULL DEFAULT FALSE
);
