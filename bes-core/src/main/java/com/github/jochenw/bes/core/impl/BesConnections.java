package com.github.jochenw.bes.core.impl;

import java.lang.reflect.Field;
import java.sql.DriverManager;

import javax.sql.DataSource;

import com.github.jochenw.afw.core.jdbc.ConnectionProvider;
import com.github.jochenw.afw.core.log.ILog;
import com.github.jochenw.afw.core.rflct.ISetter;
import com.github.jochenw.afw.core.util.Strings;
import com.github.jochenw.afw.di.api.LogInject;
import com.github.jochenw.afw.di.api.PropInject;
import com.github.jochenw.afw.di.util.Exceptions;

import jakarta.annotation.PostConstruct;

public class BesConnections {
	private @LogInject ILog log;
	private @PropInject(id="jdbc.driver") String driver;
	private @PropInject(id="jdbc.dataSourceClass") String dataSourceClass;
	private @PropInject(id="jdbc.dialect") String dialect;
	private @PropInject(id="jdbc.url") String url;
	private @PropInject(id="jdbc.user") String user;
	private @PropInject(id="jdbc.password") String password;
	private @PropInject(id="jdbc.admin.driver", nullable=true) String adminDriver;
	private @PropInject(id="jdbc.admin.dataSourceClass", nullable=true) String adminDataSourceClass;
	private @PropInject(id="jdbc.admin.url", nullable=true) String adminUrl;
	private @PropInject(id="jdbc.admin.user", nullable=true) String adminUser;
	private @PropInject(id="jdbc.admin.password", nullable=true) String adminPassword;

	private ConnectionProvider connectionProvider, adminConnectionProvider;
	private DataSource dataSource, adminDataSource;

	public String getDataSourceClass() { return dataSourceClass; }
	public String getDriver() { return driver; }
	public String getDialect() { return dialect; }
	public String getUrl() { return url; }
	public String getUser() { return user; }
	public String getPassword() { return password; }
	public String getAdminDriver() { return Strings.notEmpty(adminDriver, driver); }
	public String getAdminUrl() { return Strings.notEmpty(adminUrl, url); }
	public String getAdminUser() { return Strings.notEmpty(adminUser, user); }
	public String getAdminPassword() { return Strings.notEmpty(adminPassword, password); }
	public String getAdminDataSourceClass() { return Strings.notEmpty(adminDataSourceClass, dataSourceClass); }
	public ConnectionProvider getConnectionProvider() { return connectionProvider; }
	public ConnectionProvider getAdminConnectionProvider() { return adminConnectionProvider; }
	public synchronized DataSource getDataSource() {
		if (dataSource == null) {
			dataSource = newDataSource(dataSourceClass, url, user, password);
		}
		return dataSource;
	}
	public synchronized DataSource getAdminDataSource() {
		if (adminDataSource == null) {
			adminDataSource = newDataSource(getAdminDataSourceClass(), getAdminUrl(), getAdminUser(), getAdminPassword());
		}
		return adminDataSource;
	}

	@PostConstruct
	public void init() {
		connectionProvider = newConnectionProvider(driver, url, user, password);
		adminConnectionProvider = newConnectionProvider(getAdminDriver(),
				                                        getAdminUrl(),
				                                        getAdminUser(), getAdminPassword());
	}

	@SuppressWarnings("unchecked")
	protected DataSource newDataSource(String pDataSourceClass, String pUrl, String pUser, String pPassword) {
		final Class<? extends DataSource> dataSourceClass;
		try {
			dataSourceClass = (Class<? extends DataSource>) Class.forName(pDataSourceClass);
		} catch (Throwable t) {
			throw Exceptions.show(t);
		}
		final DataSource ds;
		try {
			ds = dataSourceClass.getConstructor().newInstance();
		} catch (Throwable t) {
			throw Exceptions.show(t);
		}
		set(ds, "url", pUrl);
		set(ds, "user", pUser);
		set(ds, "password", pPassword);
		return ds;
	}

	protected void set(Object pInstance, String pFieldName, Object pValue) {
		@SuppressWarnings("unchecked")
		Class<Object> cl = (Class<Object>) pInstance.getClass();
		Field field = null;
		while (field == null) {
			try {
				field = cl.getDeclaredField(pFieldName);
				break;
			} catch (NoSuchFieldException nsfe) {
				final Class<Object> parentClass = cl.getSuperclass();
				if (parentClass == null  ||  parentClass == Object.class) {
					throw new IllegalStateException("Field " + pFieldName + " not found in class " + pInstance.getClass().getName());
				}
			}
		}
		ISetter.of(field).set(pInstance, pValue);
	}
	protected ConnectionProvider newConnectionProvider(String pDriver, String pUrl, String pUser, String pPassword) {
		final String loggedPassword = log.isTraceEnabled() ? pPassword : "<NOT_LOGGED>";
		log.entering("newConnectionProvider", pDriver, pUrl, pUser, loggedPassword);
		if (pDriver.length() == 0) {
			throw new IllegalArgumentException("The database driver name is empty.");
		}
		if (pUrl.length() == 0) {
			throw new IllegalArgumentException("The database url is empty.");
		}
		if (pUser.length() == 0) {
			throw new IllegalArgumentException("The database user is empty.");
		}
		try {
			Thread.currentThread().getContextClassLoader().loadClass(pDriver);
		} catch (Throwable t) {
			log.error("newConnectionProvider", t);
			throw Exceptions.show(t);
		}
		final ConnectionProvider cp = () -> {
			try {
				return DriverManager.getConnection(pUrl, pUser, pPassword);
			} catch (Throwable t) {
				log.error("open", t);
				throw Exceptions.show(t);
			}
		};
		log.exiting("newConnectionProvider");
		return cp;
	}
}
