package com.github.jochenw.bes.core.impl;

import static org.junit.jupiter.api.Assertions.*;

import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.sql.Connection;
import java.util.List;
import java.util.function.Consumer;

import javax.sql.DataSource;

import org.jspecify.annotations.NonNull;
import org.junit.jupiter.api.Assumptions;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import com.github.jochenw.afw.core.function.Functions;
import com.github.jochenw.afw.core.function.Functions.FailableConsumer;
import com.github.jochenw.afw.core.jdbc.JdbcHelper;
import com.github.jochenw.afw.core.util.Objects;
import com.github.jochenw.afw.di.api.IComponentFactory;
import com.github.jochenw.bes.core.api.IBesModel;
import com.github.jochenw.bes.core.api.IBesModel.IBesUserController;
import com.github.jochenw.bes.core.model.BesUser;

class DefaultBesUserControllerTest {
	private static IComponentFactory CF;
	
	@BeforeAll
	static void initCf() {
		FlywayDbInitializer.SKIPPING = true;
		CF = Tests.newCf();
		FlywayDbInitializer.SKIPPING = false;
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

	protected @NonNull IBesUserController getUserController() {
		final @NonNull IBesModel model = CF.requireInstance(IBesModel.class);
		return Objects.requireNonNull(model.getUserController(), "UserController");
	}

	@Test
	void testCreateUsers() {
		final IBesUserController uc = getUserController();
		assertNotNull(uc);
		final List<BesUser> users0 = uc.getAll();
		assertNotNull(users0);
		assertTrue(users0.isEmpty());
		createSampleUsers();
		final List<BesUser> users1 = uc.getAll();
		assertNotNull(users1);
		assertEquals(3, users1.size());
		assertUser(users1.get(0), 1, "jwi", "joe@somecompany.com", "Wiedmann, Jochen");
		assertUser(users1.get(1), 2, "jodoe", "john.doe@somecompany.com", "Doe, John");
		assertUser(users1.get(2), 3, "jadoe", "jane.doe@somecompany.com", "Doe, Jane");
	}

	@Test
	void testUpdateUsers() {
		testCreateUsers();
		final @NonNull IBesUserController uc = getUserController();
		final BesUser johnDoeUser = uc.getUserById(BesUser.Id.of(2));
		assertNotNull(johnDoeUser);
		johnDoeUser.setUserId("jpdoe");
		johnDoeUser.setEmail("john.p.doe@somecompany.com");
		johnDoeUser.setName("Doe, John P.");
		final BesUser updatedUser = getUserController().update(johnDoeUser);
		final Consumer<BesUser> validator = (bu) -> {
			assertUser(bu, 2, "jpdoe", "john.p.doe@somecompany.com", "Doe, John P.");
		};
		validator.accept(updatedUser);
		final BesUser jpDoeUser = uc.getUserById(BesUser.Id.of(2));
		validator.accept(jpDoeUser);
		final List<BesUser> users = uc.getAll();
		assertEquals(3, users.size());
		assertUser(users.get(0), 1, "jwi", "joe@somecompany.com", "Wiedmann, Jochen");
		validator.accept(users.get(1));
		assertUser(users.get(2), 3, "jadoe", "jane.doe@somecompany.com", "Doe, Jane");
	}

	@Test
	void testDeleteUsers() {
		testCreateUsers();
		final @NonNull IBesUserController uc = getUserController();
		final BesUser johnDoeUser = uc.getUserById(BesUser.Id.of(2));
		assertNotNull(johnDoeUser);
		uc.delete(johnDoeUser);
		final List<BesUser> users = uc.getAll();
		assertEquals(2, users.size());
		assertUser(users.get(0), 1, "jwi", "joe@somecompany.com", "Wiedmann, Jochen");
		assertUser(users.get(1), 3, "jadoe", "jane.doe@somecompany.com", "Doe, Jane");
	}
	private void assertUser(BesUser pBu, int pId, String pUserId, String pEmail, String pName) {
		final BesUser.Id id = BesUser.Id.of((long) pId);
		final Consumer<BesUser> validator = (bu) -> {
			assertNotNull(bu);
			assertEquals(bu.getId().getId(), id.getId());
			assertEquals(bu.getUserId(), pUserId);
			assertEquals(bu.getEmail(), pEmail);
			assertEquals(bu.getName(), pName);
		};
		validator.accept(pBu);
		validator.accept(getUserController().getUserById(id));
		validator.accept(getUserController().getUserByUserId(pUserId));
		validator.accept(getUserController().getUserByEmail(pEmail));
	}

	private void createSampleUsers() {
		final BesUser user0 = createUser("jwi", "joe@somecompany.com", "Wiedmann, Jochen");
		assertEquals(1, user0.getId().getId());
		final BesUser user1 = createUser("jodoe", "john.doe@somecompany.com", "Doe, John");
		assertEquals(2, user1.getId().getId());
		final BesUser user2 = createUser("jadoe", "jane.doe@somecompany.com", "Doe, Jane");
		assertEquals(3, user2.getId().getId());
	}

	private BesUser createUser(@NonNull String pUserId, @NonNull String pEmail, @NonNull String pName) {
		return getUserController().insert(BesUser.of(BesUser.Id.noId(), pUserId, pEmail, pName));
	}
}
