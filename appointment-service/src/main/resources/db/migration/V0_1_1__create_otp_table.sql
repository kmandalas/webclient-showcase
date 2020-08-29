CREATE TABLE otp (
    id              SERIAL CONSTRAINT id PRIMARY KEY,
    pin             INT NOT NULL,
    customer_id     BIGINT NOT NULL,
    created_on      TIMESTAMPTZ NOT NULL,
    status          VARCHAR(255) NOT NULL
);
