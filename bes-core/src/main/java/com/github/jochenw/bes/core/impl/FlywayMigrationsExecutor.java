package com.github.jochenw.bes.core.impl;

import javax.sql.DataSource;

import org.flywaydb.core.Flyway;
import org.flywaydb.core.api.callback.Callback;
import org.flywaydb.core.api.callback.Context;
import org.flywaydb.core.api.callback.Event;
import org.flywaydb.core.api.configuration.ClassicConfiguration;
import org.flywaydb.core.api.configuration.Configuration;
import org.flywaydb.core.api.logging.LogFactory;
import org.flywaydb.core.api.output.MigrateResult;
import org.flywaydb.core.internal.jdbc.DriverDataSource;
import org.slf4j.LoggerFactory;
import org.slf4j.impl.SimpleLogger;
import org.slf4j.impl.SimpleLoggerFactory;

import com.github.jochenw.afw.core.log.ILog;
import com.github.jochenw.afw.di.api.LogInject;
import com.github.jochenw.afw.di.util.Exceptions;

import jakarta.annotation.PostConstruct;
import jakarta.inject.Inject;

public class FlywayMigrationsExecutor {
	private @LogInject ILog log;
	private @Inject BesConnections besConnections;

	@PostConstruct
	public void run() {
		log.entering("run");
		final Flyway flyway = Flyway.configure()
				.configuration(getConfiguration())
				.load();
		final MigrateResult migResult = flyway.migrate();
		if (migResult.success) {
			log.info("run", "Flyway migrations executed successfully.");
		} else {
			final Exception exception = migResult.exceptionObject;
			throw Exceptions.show(exception);
		}
		log.exiting("run");
	}

	protected DataSource getDataSource() {
		return besConnections.getAdminDataSource();
	}

	protected Configuration getConfiguration() {
		final ClassicConfiguration cfg = new ClassicConfiguration();
		cfg.setClassLoader(Thread.currentThread().getContextClassLoader());
		cfg.setDataSource(getDataSource());
		cfg.setSqlMigrationPrefix("v");
		cfg.setBaselineOnMigrate(Boolean.TRUE);
		cfg.setSchemas(new String[] {"bes"});
		cfg.setCallbacks(new Callback() {
			@Override
			public boolean supports(Event pEvnt, Context pCtx) {
				if ("beforeEachMigrateStatement".equals(pEvnt.getId())) {
					return true;
				}
				System.out.println("supports: " + pEvnt.getId());
				return false;
			}

			@Override
			public boolean canHandleInTransaction(Event pEvnt, Context pCtx) {
				System.out.println("canHandleInTransaction: " + pEvnt.getId());
				return false;
			}

			@Override
			public void handle(Event pEvnt, Context pCtx) {
				if ("beforeEachMigrateStatement".equals(pEvnt.getId())) {
					System.out.println("beforeEachMigrateStatement: " + pCtx.getStatement().getSql());
				} else {
					System.out.println("handle: " + pEvnt.getId());
				}
			}

			@Override
			public String getCallbackName() {
				return "BesLogSqlCb";
			}
		});
		cfg.setLoggers("slf4j");
		final String dialectLocation = "classpath:db/schema/" + besConnections.getDialect();
		final String sharedLocation = "classpath:db/schema";
		cfg.setLocationsAsStrings(dialectLocation, sharedLocation);
		return cfg;
	}
}
