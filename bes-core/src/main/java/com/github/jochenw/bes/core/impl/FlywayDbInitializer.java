package com.github.jochenw.bes.core.impl;

import java.sql.Connection;

import javax.sql.DataSource;

import org.flywaydb.core.Flyway;
import org.flywaydb.core.api.configuration.ClassicConfiguration;
import org.flywaydb.core.api.configuration.FluentConfiguration;

import com.github.jochenw.afw.core.util.Exceptions;

import jakarta.annotation.PostConstruct;
import jakarta.inject.Inject;

public class FlywayDbInitializer {
	private @Inject DataSource dataSource;

	@PostConstruct
	public void run() {
		// Test, whether we can connect to the database.
		try (Connection conn = dataSource.getConnection()) {
			
		} catch (Exception e) {
			throw Exceptions.show(e);
		}
		final Flyway flyway = Flyway.configure()
			.dataSource(dataSource)
			.baselineOnMigrate(true)
			.locations("classpath:com/github/jochenw/bes/core/schema/mariadb")
			.loggers("slf4j")
			.validateMigrationNaming(false)
			.load();
		flyway.migrate();
	}
}
