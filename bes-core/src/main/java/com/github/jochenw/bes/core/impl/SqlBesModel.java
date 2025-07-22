package com.github.jochenw.bes.core.impl;

import java.sql.Connection;
import java.sql.SQLException;
import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.NoSuchElementException;
import java.util.StringTokenizer;
import java.util.function.Consumer;
import java.util.regex.Pattern;

import javax.sql.DataSource;

import com.github.jochenw.afw.core.function.Functions.FailableFunction;
import com.github.jochenw.afw.core.jdbc.JdbcHelper;
import com.github.jochenw.afw.core.jdbc.JdbcHelper.Rows;
import com.github.jochenw.afw.di.util.Exceptions;
import com.github.jochenw.bes.core.api.BesExecution;
import com.github.jochenw.bes.core.api.BesFileContent;
import com.github.jochenw.bes.core.api.BesJob;
import com.github.jochenw.bes.core.api.BesUser;
import com.github.jochenw.bes.core.api.IBesModel;
import com.github.jochenw.bes.core.api.IBesUsersController;

import jakarta.inject.Inject;

public class SqlBesModel implements IBesModel {
	private @Inject DataSource dataSource;
	private @Inject JdbcHelper helper;
	private @Inject ZoneId zoneId;
	private final List<Listener> listeners = new ArrayList<>();

	protected Connection getConnection() {
		try {
			return dataSource.getConnection();
		} catch (SQLException se) {
			throw Exceptions.show(se);
		}
	}

	static final String SELECT_USERS = "SELECT id, userId, userName, userEmail FROM besUsers";
	static final String SELECT_JOBS = "SELECT id, ownerId, name FROM besJobs";
	static final String SELECT_EXECUTIONS = "SELECT id, jobId, startTime, endTime FROM besExecutions";
	static final String SELECT_FILE_CONTENT = "SELECT id, size, content FROM besFileContent";

	@Override
	public void getUsers(Consumer<BesUser> pConsumer) {
		allObjects(helper.query(getConnection(), SELECT_USERS), this::asUser, pConsumer);
	}

	protected BesUser asUser(Rows pRows) {
		final long id = pRows.nextLong();
		final String userId = pRows.nextStr();
		final String name = pRows.nextStr();
		final String email = pRows.nextStr();
		return BesUser.of(id, userId, name, email);
	}

	protected Object[] asParameters(BesUser pUser) {
		return new Object[] { pUser.getId().getIdObj(), pUser.getUserId(), pUser.getName(), pUser.getEmail() };
	}

	protected BesJob asJob(Rows pRows) {
		final long id = pRows.nextLong();
		final Long ownerId = Long.valueOf(pRows.nextLong());
		final String name = pRows.nextStr();
		return BesJob.of(id, ownerId, name);
	}

	protected BesExecution asExecution(Rows pRows) {
		final long id = pRows.nextLong();
		final Long jobId = Long.valueOf(pRows.nextLong());
		final ZonedDateTime startTime = pRows.nextZonedDateTime(zoneId);
		final ZonedDateTime endTime = pRows.nextZonedDateTime(zoneId);
		return BesExecution.of(id, startTime, endTime, jobId);
	}

	protected BesFileContent asFileContent(Rows pRows) {
		final long id = pRows.nextLong();
		final Long size = Long.valueOf(pRows.nextLong());
		final byte[] contents = pRows.nextBytes();
		return BesFileContent.of(Long.valueOf(id), size, contents);
	}

	@Override
	public BesUser getUserById(long pId) {
		final String sql = SELECT_USERS + " WHERE id=?";
	    return singleObject(helper.query(getConnection(), sql, Long.valueOf(pId)), this::asUser);
	}

	@Override
	public BesUser getUserByUserId(String pUserId) {
		final String sql = SELECT_USERS + " WHERE userId=?";
	    return singleObject(helper.query(getConnection(), sql, pUserId), this::asUser);
	}

	public <O> O singleObject(JdbcHelper.Executor pExecutor, FailableFunction<Rows,O,?> pMapper) {
		return pExecutor.callWithRows((rows) -> {
			if (rows.next()) {
				final O o = pMapper.apply(rows);
				if (rows.next()) {
					throw new IllegalStateException("Query returned more than one row.");
				}
				return o;
			} else {
				return null;
			}
		});
	}

	public <O> void allObjects(JdbcHelper.Executor pExecutor, FailableFunction<Rows,O,?> pMapper, Consumer<O> pConsumer) {
		pExecutor.runWithRows((rows) -> {
			while (rows.next()) {
				pConsumer.accept(pMapper.apply(rows));
			}
		});
		
	}

	@Override
	public BesUser getUserByEmail(String pEmail) {
		final String sql = SELECT_USERS + " WHERE userEmail=?";
	    return singleObject(helper.query(getConnection(), sql, pEmail), this::asUser);
	}

	@Override
	public void getJobs(Consumer<BesJob> pConsumer) {
		allObjects(helper.query(getConnection(), SELECT_JOBS), this::asJob, pConsumer);
	}

	@Override
	public BesJob getJobById(long pId) {
		final String sql = SELECT_JOBS + " WHERE id=?";
		return singleObject(helper.query(getConnection(), sql, Long.valueOf(pId)), this::asJob);
	}

	@Override
	public void getExecutions(Consumer<BesExecution> pConsumer) {
		allObjects(helper.query(getConnection(), SELECT_EXECUTIONS), this::asExecution, pConsumer);
	}

	@Override
	public BesExecution getExecution(long pId) {
		final String sql = SELECT_EXECUTIONS + " WHERE id=?";
		return singleObject(helper.query(getConnection(), sql, Long.valueOf(pId)), this::asExecution);
	}

	@Override
	public BesFileContent getContentById(long pId) {
		final String sql = SELECT_FILE_CONTENT + " WHERE id=?";
		return singleObject(helper.query(getConnection(), sql, Long.valueOf(pId)), this::asFileContent);
	}

	@Override
	public void add(Listener pListener) {
		synchronized(listeners) {
			listeners.add(pListener);
		}
	}

	@Override
	public void remove(Listener pListener) throws NoSuchElementException {
		synchronized(listeners) {
			listeners.add(pListener);
		}
	}

	protected void note(Consumer<Listener> pNotification) {
		synchronized(listeners) {
			listeners.forEach(pNotification);
		}
	}

	private static final Pattern FIRST_WORD_PATTERN = Pattern.compile("^(\\S+)\\s(.*)$");
	static final String SELECT_NEW_ID_USERS = asNewIdStatement(SELECT_USERS);
	static final String INSERT_USERS = asInsertStatement(SELECT_USERS);
	static final String UPDATE_USERS = asUpdateStatement(SELECT_USERS);

	protected long newId(String pSelectSequenceSql) {
		return helper.query(getConnection(), pSelectSequenceSql).countLong();
	}


	protected static String asInsertStatement(String pSelectStatement) {
		final List<String> columnNames = new ArrayList<>();
		final String tableName = parseSelectStatement(columnNames::add, pSelectStatement);
		final StringBuilder sb = new StringBuilder();
		sb.append("INSERT INTO ");
		sb.append(tableName);
		sb.append(" (");
		for (int i = 0;  i < columnNames.size();  i++) {
			if (i > 0) {
				sb.append(", ");
			}
			sb.append(columnNames.get(i));
		}
		sb.append(") VALUES (");
		for (int i = 0;  i < columnNames.size();  i++) {
			if (i > 0) {
				sb.append(", ");
			}
			sb.append("?");
		}
		sb.append(")");
		return sb.toString();
	}

	protected static String asUpdateStatement(String pSelectStatement) {
		final List<String> columnNames = new ArrayList<>();
		final String tableName = parseSelectStatement(columnNames::add, pSelectStatement);
		final StringBuilder sb = new StringBuilder();
		sb.append("UPDATE ");
		sb.append(tableName);
		sb.append(" SET ");
		for (int i = 1;  i < columnNames.size();  i++) {
			if (i > 1) {
				sb.append(", ");
			}
			sb.append(columnNames.get(i));
			sb.append("=?");
		}
		sb.append(" WHERE ");
		sb.append(columnNames.get(0));
		sb.append("=?");
		return sb.toString();
	}

	protected static String asNewIdStatement(String pSelectStatement) {
		final String tableName = parseSelectStatement((s) -> {}, pSelectStatement);
		return "SELECT NEXT VALUE FOR " + tableName + "Seq";
	}
	
	protected static String parseSelectStatement(Consumer<String> pColumnNameConsumer, String pSelectStatement) {
		boolean selectFound = false;
		boolean columnNameFound = false;
		boolean fromFound = false;
		for (StringTokenizer st = new StringTokenizer(pSelectStatement, " ");  st.hasMoreTokens();  ) {
			final String word = st.nextToken().trim();
			if (selectFound) {
				if (fromFound) {
					if (columnNameFound) {
						return word;
					} else {
						break;
					}
				} else if ("FROM".equals(word)) {
					fromFound = true;
				} else {
					final String columnName;
					if (word.endsWith(",")) {
						columnName = word.substring(0, word.length()-1);
					} else {
						columnName = word;
					}
					if (pColumnNameConsumer != null) {
						pColumnNameConsumer.accept(columnName);
						columnNameFound = true;
					}
				}
			} else {
				if ("SELECT".equals(word)) {
					selectFound = true;
				} else {
					break;
				}
			}
		}
		throw new IllegalStateException("Unable to parse SELECT statement: " + pSelectStatement);
	}
	@Override
	public IBesUsersController getUsersController() { return usersController; }
	
	final IBesUsersController usersController = new IBesUsersController() {
		@Override
		public BesUser insert(BesUser pUser) {
			final long id = newId(SELECT_NEW_ID_USERS);
			final BesUser bu = BesUser.of(id, pUser);
			final JdbcHelper.Executor executor = helper.query(getConnection(), INSERT_USERS, asParameters(bu));
			executor.run();
			note((l) -> l.userInserted(bu));
			return bu;
		}

		@Override
		public void update(BesUser pUser) throws NoSuchElementException {
			// TODO Auto-generated method stub
			
		}

		@Override
		public void delete(BesUser pUser) throws NoSuchElementException {
			// TODO Auto-generated method stub
			
		}
		
	};
}
