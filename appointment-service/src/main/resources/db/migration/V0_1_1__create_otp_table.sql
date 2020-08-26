CREATE TABLE otp (
    id              SERIAL CONSTRAINT id PRIMARY KEY,
    customer_id     VARCHAR(255) NOT NULL,
    value           INT NOT NULL,
    created_on      TIMESTAMP NOT NULL,
    status          VARCHAR(255) NOT NULL
);
