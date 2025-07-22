package com.github.jochenw.bes.core.api;

import java.time.ZoneId;

import javax.sql.DataSource;

import com.github.jochenw.afw.core.jdbc.JdbcHelper;
import com.github.jochenw.afw.core.util.Objects;
import com.github.jochenw.afw.di.api.Application;
import com.github.jochenw.afw.di.api.Module;
import com.github.jochenw.afw.di.api.Scopes;
import com.github.jochenw.bes.core.impl.BesConnections;
import com.github.jochenw.bes.core.impl.FlywayMigrationsExecutor;
import com.github.jochenw.bes.core.impl.SqlBesModel;

public class BesCore {
	private static Application THE_APPLICATION;

	public static synchronized Application getApplication() {
		if (THE_APPLICATION == null) {
			throw new IllegalStateException("The application has not yet been initialized.");
		}
		return THE_APPLICATION;
	}

	public static synchronized void setApplication(Application pApplication) {
		THE_APPLICATION = Objects.requireNonNull(pApplication, "Application");
	}

	public static Module BES_CORE_MODULE = (b) -> {
		b.bind(BesConnections.class);
		b.bind(FlywayMigrationsExecutor.class);
		b.bind(IBesModel.class).to(SqlBesModel.class).in(Scopes.SINGLETON);
		b.bind(JdbcHelper.class).toInstance(new JdbcHelper());
		b.bind(ZoneId.class).toInstance(ZoneId.of("Europe/Berlin"));
		b.bind(DataSource.class).toFunction((cf) -> cf.requireInstance(BesConnections.class).getDataSource());
		b.addFinalizer((cf) -> cf.requireInstance(FlywayMigrationsExecutor.class));
	};
}
