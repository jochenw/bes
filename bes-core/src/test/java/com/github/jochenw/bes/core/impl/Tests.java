package com.github.jochenw.bes.core.impl;

import static org.junit.jupiter.api.Assertions.assertTrue;

import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.sql.Connection;
import java.sql.SQLException;
import java.time.ZoneId;

import javax.sql.DataSource;

import org.junit.jupiter.api.Assumptions;

import com.github.jochenw.afw.core.function.Functions;
import com.github.jochenw.afw.core.function.Functions.FailableConsumer;
import com.github.jochenw.afw.core.jdbc.JdbcHelper;
import com.github.jochenw.afw.core.util.Exceptions;
import com.github.jochenw.afw.core.util.Objects;
import com.github.jochenw.afw.di.api.IComponentFactory;
import com.github.jochenw.afw.di.api.IModule;
import com.github.jochenw.bes.core.api.BesApplication;

public class Tests {
	public static IComponentFactory newCf(boolean pSkipDbInitializer) {
		return newCf(null, pSkipDbInitializer);
	}
	public static IComponentFactory newCf(IModule pModule, boolean pSkipDbInitializer) {
		if (pSkipDbInitializer) {
			FlywayDbInitializer.SKIPPING = true;
		}
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
		final IComponentFactory cf = BesApplication.getInstance().getComponentFactory();
		if (pSkipDbInitializer) {
			FlywayDbInitializer.SKIPPING = false;
		}
		return cf;
	}

	public static void initDb(IComponentFactory CF) {
		final DataSource adminDataSource = CF.getInstance(DataSource.class, "admin");
		Assumptions.assumeTrue(adminDataSource != null);
		final Path path = Paths.get("src/main/resources/com/github/jochenw/bes/core/schema/mariadb/v0__CreateDatabase.sql");
		assertTrue(Files.isRegularFile(path));
		try (Connection connection = adminDataSource.getConnection()) {
			final JdbcHelper jh = CF.requireInstance(JdbcHelper.class);
			final FailableConsumer<String,?> statementExecutor = (s) -> {
				System.out.println("Admin SQL Statement: " + s);
				jh.query(connection, s).run();
			};
			Functions.accept(statementExecutor, "DROP DATABASE IF EXISTS bes");
			jh.read(path, statementExecutor);
		} catch (SQLException se) {
			throw Exceptions.show(se);
		}
		CF.requireInstance(FlywayDbInitializer.class).run();
	}
}
