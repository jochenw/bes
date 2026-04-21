package com.github.jochenw.bes.core.impl;

import static org.junit.jupiter.api.Assertions.*;

import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.sql.Connection;
import java.util.function.Consumer;

import javax.sql.DataSource;

import org.junit.jupiter.api.Assumptions;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import com.github.jochenw.afw.core.function.Functions;
import com.github.jochenw.afw.core.function.Functions.FailableConsumer;
import com.github.jochenw.afw.core.jdbc.JdbcHelper;
import com.github.jochenw.afw.di.api.IComponentFactory;

class DefaultBesUserControllerTest {
	private static IComponentFactory CF;
	
	@BeforeAll
	static void initCf() {
		CF = Tests.newCf();
	}

	private DataSource adminDataSource;

	@BeforeEach
	void initDb() throws Exception {
		adminDataSource = CF.getInstance(DataSource.class, "admin");
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
		}
		CF.requireInstance(FlywayDbInitializer.class).run();
	}

	@Test
	void test() {
		
	}

}
