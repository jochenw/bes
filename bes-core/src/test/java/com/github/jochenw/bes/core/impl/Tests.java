package com.github.jochenw.bes.core.impl;

import java.time.ZoneId;

import javax.sql.DataSource;

import com.github.jochenw.afw.core.jdbc.JdbcHelper;
import com.github.jochenw.afw.core.util.Objects;
import com.github.jochenw.afw.di.api.IComponentFactory;
import com.github.jochenw.afw.di.api.IModule;
import com.github.jochenw.bes.core.api.BesApplication;

public class Tests {
	public static IComponentFactory newCf() {
		return newCf(null);
	}
	public static IComponentFactory newCf(IModule pModule) {
		final IModule module = (b) -> {
			b.bind(FlywayDbInitializer.class).asEagerSingleton();
			b.bind(DataSource.class, "admin").to((cf) -> {
				return cf.requireInstance(BesApplication.class).newDataSource(cf, "jdbc.admin"); 
			});
			b.bind(JdbcHelper.class).toInstance(new JdbcHelper());
			b.bind(ZoneId.class, "db").toInstance(JdbcHelper.UTC);
			b.bind(ZoneId.class).toInstance(Objects.requireNonNull(ZoneId.of("Europe/Berlin")));
		};
		BesApplication.setInstance(module.extend(pModule), "bes-factory.properties", "bes-test.properties");
		return BesApplication.getInstance().getComponentFactory();
	}
}
