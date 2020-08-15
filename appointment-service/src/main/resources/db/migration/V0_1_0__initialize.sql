CREATE TABLE appointment (
	id SERIAL PRIMARY KEY,
	created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
	status VARCHAR(10) NOT NULL,
	appointment_date DATE NOT NULL,
	appointment_start_time TIMESTAMP NOT NULL,
	appointment_end_time TIMESTAMP NOT NULL,
	customer_email VARCHAR(100) NOT NULL,
	notification_phone_number VARCHAR(100) NOT NULL,
	service_address VARCHAR(100) NOT NULL
);
