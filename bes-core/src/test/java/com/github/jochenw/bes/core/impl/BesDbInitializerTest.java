package com.github.jochenw.bes.core.impl;

import static org.junit.jupiter.api.Assertions.*;

import java.net.URL;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Properties;

import org.junit.jupiter.api.Test;

import com.github.jochenw.afw.core.inject.AfwCoreOnTheFlyBinder;
import com.github.jochenw.afw.core.log.ILog.Level;
import com.github.jochenw.afw.core.log.ILogFactory;
import com.github.jochenw.afw.core.log.simple.SimpleLogFactory;
import com.github.jochenw.afw.core.props.DefaultPropertyFactory;
import com.github.jochenw.afw.core.props.IPropertyFactory;
import com.github.jochenw.afw.core.util.Streams;
import com.github.jochenw.afw.di.api.Application;
import com.github.jochenw.afw.di.api.IComponentFactory;
import com.github.jochenw.afw.di.api.Module;

class BesDbInitializerTest {
	public static IComponentFactory getComponentFactory(Class<?> pTestClass, Module pModule) {
		final ILogFactory lf = SimpleLogFactory.ofSystemOut(Level.TRACE);
		final Path path = Paths.get("src/test/resources/bes-test.properties");
		if (!Files.isRegularFile(path)) {
			throw new IllegalStateException("Test properties not found: " + path);
		}
		final URL factoryPropertiesUrl = Thread.currentThread().getContextClassLoader().getResource("bes-factory.properties");
		if (factoryPropertiesUrl == null) {
			throw new IllegalStateException("Bes factory properties not found.");
		}
		final Properties factoryProperties = Streams.load(factoryPropertiesUrl);
		final Properties instanceProperties = Streams.load(path);
		final IPropertyFactory pf = new DefaultPropertyFactory(factoryProperties, instanceProperties);
		final Module module = (b) -> {
			b.bind(ILogFactory.class).toInstance(lf);
			b.bind(IPropertyFactory.class).toInstance(pf);
			b.bind(BesDbInitializer.class);
			b.bind(BesDbConnectionProvider.class);
		};
		final Application application;
		if (pModule == null) {
			application = AfwCoreOnTheFlyBinder.applicationOf(module, "jakarta");
		} else {
			application = AfwCoreOnTheFlyBinder.applicationOf(module.extend(pModule), "jakarta");
		}
		return application.getComponentFactory();
	}
	public static IComponentFactory getComponentFactory(Class<?> pTestClass) {
		return getComponentFactory(pTestClass, null);
	}

	@Test
	void testInitDb() {
		final BesDbInitializer initializer = getComponentFactory(BesDbInitializerTest.class).requireInstance(BesDbInitializer.class);
		assertTrue(initializer.isExecuted());
	}

}
