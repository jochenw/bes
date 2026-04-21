package com.github.jochenw.bes.core.api;

import java.io.OutputStreamWriter;
import java.io.PrintWriter;
import java.nio.charset.StandardCharsets;
import java.sql.SQLException;
import java.util.Properties;

import javax.sql.DataSource;

import org.mariadb.jdbc.MariaDbDataSource;

import com.github.jochenw.afw.core.app.Application;
import com.github.jochenw.afw.core.data.Data;
import com.github.jochenw.afw.core.log.ILog;
import com.github.jochenw.afw.core.log.ILog.Level;
import com.github.jochenw.afw.core.log.ILogFactory;
import com.github.jochenw.afw.core.props.IPropertyFactory;
import com.github.jochenw.afw.core.util.Exceptions;
import com.github.jochenw.afw.di.api.IComponentFactory;
import com.github.jochenw.afw.di.api.IModule;
import com.github.jochenw.bes.core.api.IBesModel.IBesPropertiesController;
import com.github.jochenw.bes.core.api.IBesModel.IBesUserController;
import com.github.jochenw.bes.core.impl.DefaultBesModel;
import com.github.jochenw.bes.core.impl.DefaultBesPropertiesController;
import com.github.jochenw.bes.core.impl.DefaultBesUserController;
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
		final BesApplication besApplication = new BesApplication();
		besApplication.init(pModule, uris);
		synchronized(BesApplication.class) {
			THE_INSTANCE = besApplication;
		}
	}

	private Application application;
	private IComponentFactory componentFactory;
	private IPropertyFactory propertyFactory;
	private ILogFactory logFactory;

	protected void init(IModule pModule, String[] pUris) {
		final IModule module = (b) -> {
			b.bind(DataSource.class).to(this::newDataSource).asSingleton();
			b.bind(FlywayDbInitializer.class).asSingleton();
			b.bind(BesApplication.class).toInstance(BesApplication.this);
			b.bind(IBesModel.class).toClass(DefaultBesModel.class);
			b.bind(IBesUserController.class).toClass(DefaultBesUserController.class);
			b.bind(IBesPropertiesController.class).toClass(DefaultBesPropertiesController.class);
		};
		application = Application.of(module.extend(pModule), Level.TRACE, pUris);
		final IComponentFactory cf = application.getComponentFactory();
		componentFactory = cf;
		cf.init(this);
		propertyFactory = cf.requireInstance(IPropertyFactory.class);
		logFactory = cf.requireInstance(ILogFactory.class);
	}

	
	
	public Application getApplication() { return application; }
	public IComponentFactory getComponentFactory() { return componentFactory; }
	public IPropertyFactory getPropertyFactory() { return propertyFactory; }
	public ILogFactory getLogFactory() { return logFactory; }

	protected DataSource newDataSource(IComponentFactory pCf) {
		return newDataSource(pCf, "jdbc");
	}

	public DataSource newDataSource(IComponentFactory pCf, String pPrefix) {
		final ILogFactory lf = pCf.requireInstance(ILogFactory.class);
		final ILog log = lf.getLog(BesApplication.class);
		final Properties properties = pCf.requireInstance(Properties.class);
		final String dialect = Data.requireString(properties, pPrefix +".dialect");
	    log.infof("newDataSource", "Database type is %s", dialect);
		if ("mariadb".equals(dialect)) {
			final MariaDbDataSource ds = new MariaDbDataSource();
			final String url = Data.requireString(properties, pPrefix + ".url");
			final String user = Data.requireString(properties, pPrefix + ".user");
			final String pwd = Data.requireString(properties, pPrefix + ".pwd");
			log.infof("newDataSource", "MariaDB URL is %s", url);
			log.infof("newDataSource", "MariaDB user is %s", user);
			log.infof("newDataSource", "MariaDB password is not being logged.");
			try {
				final PrintWriter pw = new PrintWriter(new OutputStreamWriter(System.out, StandardCharsets.UTF_8));
				ds.setLogWriter(pw);
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
