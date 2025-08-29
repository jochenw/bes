package com.github.jochenw.bes.core.impl;

import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.List;
import java.util.StringTokenizer;
import java.util.function.BooleanSupplier;

import javax.sql.DataSource;

import org.flywaydb.core.Flyway;
import org.flywaydb.core.api.ResourceProvider;
import org.flywaydb.core.api.callback.Callback;
import org.flywaydb.core.api.callback.Context;
import org.flywaydb.core.api.callback.Event;
import org.flywaydb.core.api.configuration.Configuration;
import org.flywaydb.core.api.output.BaselineResult;
import org.flywaydb.core.api.output.MigrateResult;
import org.flywaydb.core.internal.jdbc.DriverDataSource;

import com.github.jochenw.afw.core.log.ILog;
import com.github.jochenw.afw.core.props.IPropertyFactory;
import com.github.jochenw.afw.di.api.LogInject;
import com.github.jochenw.bes.core.impl.BesDbConnectionProvider.ConnectionConfiguration;

import jakarta.annotation.PostConstruct;
import jakarta.inject.Inject;

/** An object, which initializes the database using Flyway.
 */
public class BesDbInitializer {
	private @LogInject ILog log;
	private @Inject BesDbConnectionProvider dbConnectionProvider;
	private @Inject IPropertyFactory propertyFactory;
	private boolean executed;

	@PostConstruct
	public void initDb() {
		final String prefixStr = propertyFactory.getPropertyValue("db.admin.prefixes");
		log.entering("initDb: ", prefixStr);
		if (prefixStr == null) {
			throw new IllegalStateException("Property not found: db.admin.prefixes");
		}
		final List<String> prefixes = new ArrayList<>();
		for (StringTokenizer st = new StringTokenizer(prefixStr,",");  st.hasMoreTokens();  ) {
			final String prefix = st.nextToken().trim();
			if (prefix != null  &&  prefix.length() > 0) {
				prefixes.add(prefix);
			}
		}
		if (prefixes.isEmpty()) {
			throw new IllegalStateException("Invalid value for property db.admin.prefixes: " + prefixStr);
		}
		final ConnectionConfiguration connectionConfiguration = dbConnectionProvider.getConfiguration(propertyFactory, prefixes);
		final String driver = connectionConfiguration.getDriver();
		final String url = connectionConfiguration.getUrl();
		final String userName = connectionConfiguration.getUser();
		final String password = connectionConfiguration.getPassword();
		log.info("initDb:", driver, url, userName, "<PasswordNotLogged>");
		final Flyway flyway = Flyway.configure()
				.driver(connectionConfiguration.getDriver())
				.dataSource(url, userName, password)
				.encoding(StandardCharsets.UTF_8)
				.baselineVersion("0")
				.locations("classpath:com/github/jochenw/bes/core/model/schema/" + connectionConfiguration.getDialect())
				.resourceProvider(null)
				.load();
		final BaselineResult baseLineResult = flyway.baseline();
		if (!baseLineResult.successfullyBaselined) {
			throw new IllegalStateException("Flyway migration failed: " + String.join(", ", baseLineResult.warnings));
		}
	final MigrateResult result = flyway.migrate();
		if (!result.success) {
			throw new IllegalStateException("Flyway migration failed", result.exceptionObject);
		}
		log.exiting("initDb");
		executed = true;
	}

	protected Callback newLogCallback() {
		return new Callback() {

			@Override
			public boolean supports(Event pEvent, Context pContext) {
				System.out.println("Callback.supports: Event=" + pEvent + ", Context=" + pContext);
				return true;
			}

			@Override
			public boolean canHandleInTransaction(Event pEvent, Context pContext) {
				System.out.println("Callback.canHandleInTransaction: Event=" + pEvent + ", Context=" + pContext);
				return true;
			}

			@Override
			public void handle(Event pEvent, Context pContext) {
				System.out.println("Callback.handle: Event=" + pEvent + ", Context=" + pContext);
			}

			@Override
			public String getCallbackName() {
				return "BesFlywayLogCallback";
			}
			
		};
	}

	public boolean isExecuted() {
		return executed;
	}
}
