package com.github.jochenw.bes.core.impl;

import javax.sql.DataSource;

import org.flywaydb.core.Flyway;

import jakarta.annotation.PostConstruct;
import jakarta.inject.Inject;

public class FlywayDbInitializer {
	private @Inject DataSource dataSource;

	@PostConstruct
	public void run() {
		final Flyway flyway = Flyway.configure()
			.dataSource(dataSource)
			.baselineOnMigrate(true)
			.dryRunOutput(System.out)
			.locations("classpath:com/github/jochenw/bes/core/schema/mariadb")
			.load();
		flyway.migrate();
	}
}
