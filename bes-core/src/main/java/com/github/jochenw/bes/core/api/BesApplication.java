package com.github.jochenw.bes.core.api;

import java.sql.SQLException;
import java.util.Properties;

import javax.sql.DataSource;

import org.mariadb.jdbc.MariaDbPoolDataSource;

import com.github.jochenw.afw.core.app.Application;
import com.github.jochenw.afw.core.data.Data;
import com.github.jochenw.afw.core.log.ILog;
import com.github.jochenw.afw.core.log.ILog.Level;
import com.github.jochenw.afw.core.log.ILogFactory;
import com.github.jochenw.afw.core.props.IPropertyFactory;
import com.github.jochenw.afw.core.util.Exceptions;
import com.github.jochenw.afw.di.api.IComponentFactory;
import com.github.jochenw.afw.di.api.IModule;
import com.github.jochenw.bes.core.impl.FlywayDbInitializer;


public class BesApplication {
	private static BesApplication THE_INSTANCE;

	public static BesApplication getInstance() {
		synchronized(BesApplication.class) {
			return THE_INSTANCE;
		}
	}
	public static void setInstance(IModule pModule, String... pUris) {
		final String[] uris;
		if (pUris == null  ||  pUris.length == 0) {
			uris = new String[] {"bes-factory.properties", "bes.properties"};
		} else {
			uris = pUris;
		}
		final BesApplication besApplication = new BesApplication(pModule, uris);
		synchronized(BesApplication.class) {
			THE_INSTANCE = besApplication;
		}
	}

	private final Application application;
	private final ILogFactory logFactory;
	private final IPropertyFactory propertyFactory;
	private final ILog log;

	private BesApplication(IModule pModule, String[] pUris) {
		final IModule module = newModule(pModule);
		application = Application.of(module, Level.TRACE, pUris);
		logFactory = getComponentFactory().requireInstance(ILogFactory.class);
		propertyFactory = getComponentFactory().requireInstance(IPropertyFactory.class);
		log = logFactory.getLog(BesApplication.class);
	}

	public IComponentFactory getComponentFactory() {
		return application.getComponentFactory();
	}

	public ILogFactory getLogFactory() {
		return logFactory;
	}

	public IPropertyFactory getPropertyFactory() {
		return propertyFactory;
	}

	protected IModule newModule(IModule pModule) {
		final IModule module = (b) -> {
			b.bind(DataSource.class).to(BesApplication.this::newDataSource);
			b.bind(FlywayDbInitializer.class).asSingleton();
		};
		return module.extend(pModule);
	}

	protected DataSource newDataSource(IComponentFactory pComponentFactory) {
		final Properties properties = pComponentFactory.requireInstance(Properties.class);
		final String dialect = Data.requireString(properties, "jdbc.dialect");
	    log.info("newDataSource", "Database type is {}", dialect);
		if ("mariadb".equals(dialect)) {
			final MariaDbPoolDataSource ds = new MariaDbPoolDataSource();
			final String url = Data.requireString(properties, "jdbc.url");
			final String user = Data.requireString(properties, "jdbc.user");
			final String pwd = Data.requireString(properties, "jdbc.pwd");
			log.info("newDataSource", "MariaDB URL is {}", url);
			log.info("newDataSource", "MariaDB user is {}", user);
			log.info("newDataSource", "MariaDB password is not being logged.");
			try {
				ds.setUrl(url);
				ds.setUser(user);
				ds.setPassword(pwd);
			} catch(SQLException se) {
				throw Exceptions.show(se);
			}
			return (DataSource) ds;
		} else {
			throw new IllegalStateException("Invalid value for property jdbc.type:"
					+ " Expected mariadb, got " + dialect);
		}
	}
}
