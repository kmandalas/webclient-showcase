CREATE TABLE otp (
	id             SERIAL CONSTRAINT id PRIMARY KEY,
	pin            INT          NOT NULL,
	customer_id    BIGINT       NOT NULL,
	msisdn         VARCHAR(100) NOT NULL,
	application_id VARCHAR(10)  NOT NULL,
	attempt_count  SMALLINT     NOT NULL,
	created_on     TIMESTAMPTZ  NOT NULL,
	expires        TIMESTAMPTZ  NOT NULL,
	status         VARCHAR(255) NOT NULL
);
