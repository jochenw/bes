package com.github.jochenw.bes.core.impl;

import java.sql.Connection;
import java.sql.DriverManager;
import java.util.ArrayList;
import java.util.List;

import javax.sql.DataSource;

import org.flywaydb.core.internal.jdbc.DriverDataSource;

import com.github.jochenw.afw.core.props.IPropertyFactory;
import com.github.jochenw.afw.di.util.Exceptions;

public class BesDbConnectionProvider {
	public static class ConnectionConfiguration {
		private final String driver, dialect, url, user, password;

		public ConnectionConfiguration(String pDriver, String pDialect, String pUrl, String pUser, String pPassword) {
			driver = pDriver;
			dialect = pDialect;
			url = pUrl;
			user = pUser;
			password = pPassword;
		}

		public String getDriver() { return driver; }
		public String getDialect() { return dialect; }
		public String getUrl() { return url; }
		public String getUser() { return user; }
		public String getPassword() { return password; }
	}
	public ConnectionConfiguration getConfiguration(IPropertyFactory pPropertyFactory, List<String> pPrefixes) {
		final String driver = requireProperty(pPropertyFactory, pPrefixes, "driver");
		try {
			Class.forName(driver);
		} catch (Throwable t) {
			throw new IllegalStateException("Failed to load driver class: " + driver); 
		}
		final String dialect = requireProperty(pPropertyFactory, pPrefixes, "dialect");
		final String url = requireProperty(pPropertyFactory, pPrefixes, "url");
		final String user = requireProperty(pPropertyFactory, pPrefixes, "userName");
		final String password = requireProperty(pPropertyFactory, pPrefixes, "password");
		return new ConnectionConfiguration(driver, dialect, url, user, password);
	}
	public Connection getConnection(ConnectionConfiguration pConfiguration) {
		try {
			return DriverManager.getConnection(pConfiguration.getUrl(), pConfiguration.getUser(), pConfiguration.getPassword());
		} catch (Throwable t) {
			throw Exceptions.show(t);
		}
	}
	
	protected String requireProperty(IPropertyFactory pPropertyFactory, List<String> pPrefixes, String pKey) {
		final List<String> keys = new ArrayList<>();
		for (String prefix : pPrefixes) {
			String key = prefix;
			if (!key.endsWith(".")) {
				key += ".";
			}
			key += pKey;
			keys.add(key);
			final String value = pPropertyFactory.getPropertyValue(key);
			if (value != null  &&  value.length() > 0) {
				return value;
			}
		}
		if (keys.size() == 1) {
			throw new IllegalStateException("Property not found: " + keys.get(0));
		} else {
			throw new IllegalStateException("Neither of the following properties found: "
					+ String.join(", ", keys));
		}
	}
}
