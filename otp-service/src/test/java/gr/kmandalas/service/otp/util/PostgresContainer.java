package gr.kmandalas.service.otp.util;

import org.testcontainers.containers.PostgreSQLContainer;

public class PostgresContainer extends PostgreSQLContainer<PostgresContainer> {

	private static final String IMAGE_VERSION = "postgres:latest";
	private static PostgresContainer container;

	private PostgresContainer() {
		super(IMAGE_VERSION);
	}

	public static PostgresContainer getInstance() {
		if (container == null) {
			container = new PostgresContainer();
		}
		return container;
	}

	@Override
	public void start() {
		super.start();
		System.setProperty("DB_URL", container.getJdbcUrl());
		System.setProperty("DB_URL_R2DBC", container.getR2dbcUrl());
		System.setProperty("DB_USERNAME", container.getUsername());
		System.setProperty("DB_PASSWORD", container.getPassword());
	}

	@Override
	public void stop() {
		super.stop();
	}

	private String getR2dbcUrl() {
		return String.format("r2dbc:postgresql://%s:%d/%s?loggerLevel=OFF", this.getHost(), this.getMappedPort(POSTGRESQL_PORT), this.getDatabaseName());
	}

}
