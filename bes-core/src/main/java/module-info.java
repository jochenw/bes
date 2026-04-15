module com.github.jochenw.bes.core {
	exports com.github.jochenw.bes.core.api;
	exports com.github.jochenw.bes.core.model;

	requires transitive com.github.jochenw.afw.di;
	requires transitive com.github.jochenw.afw.core;
	requires flyway.core;
	requires transitive jakarta.annotation;
	requires transitive jakarta.inject;
	requires org.mariadb.jdbc;
}