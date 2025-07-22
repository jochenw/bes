package com.github.jochenw.bes.ui.vdn.srvlt;

import java.io.IOException;
import java.net.URL;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.time.ZonedDateTime;
import java.util.Properties;

import com.github.jochenw.afw.core.inject.AfwCoreOnTheFlyBinder;
import com.github.jochenw.afw.core.log.ILog;
import com.github.jochenw.afw.core.log.ILog.Level;
import com.github.jochenw.afw.core.log.ILogFactory;
import com.github.jochenw.afw.core.log.log4j.Log4j2LogFactory;
import com.github.jochenw.afw.core.log.simple.SimpleLogFactory;
import com.github.jochenw.afw.core.props.DefaultPropertyFactory;
import com.github.jochenw.afw.core.props.IPropertyFactory;
import com.github.jochenw.afw.core.util.Exceptions;
import com.github.jochenw.afw.core.util.Streams;
import com.github.jochenw.afw.di.api.Application;
import com.github.jochenw.afw.di.api.IComponentFactory;
import com.github.jochenw.afw.di.api.ILifecycleController;
import com.github.jochenw.afw.di.api.Module;
import com.github.jochenw.bes.core.api.BesCore;

import jakarta.servlet.ServletContext;
import jakarta.servlet.ServletContextEvent;
import jakarta.servlet.ServletContextListener;
import jakarta.servlet.annotation.WebListener;


@WebListener
public class BesSrvInitializer implements ServletContextListener {
	public static final String KEY_DATA = BesSrvInitializer.class.getName() + ".DATA";

	private static class Data {
		private final IComponentFactory componentFactory;

		public Data(IComponentFactory pComponentFactory) {
			componentFactory = pComponentFactory;
		}
		
	}

	@Override
	public void contextInitialized(ServletContextEvent pSce) {
		ServletContextListener.super.contextInitialized(pSce);
		final ServletContext sc = pSce.getServletContext();
		final Data data = newData(sc);
		log.info("contextInitialized", "Application core started.");
		sc.setAttribute(KEY_DATA, data);
	}

	@Override
	public void contextDestroyed(ServletContextEvent pSce) {
		log.entering("contextDestroyed");
		ServletContextListener.super.contextDestroyed(pSce);
		final ServletContext sc = pSce.getServletContext();
		final Data data = requireData(sc);
		final ILifecycleController lc = data.componentFactory.requireInstance(ILifecycleController.class);
		log.info("contextDestroyed", "Initiating shutdown of application core.");
		lc.shutdown();
		log.exiting("contextDestroyed");
	}

	private ILog log;

	protected Data newData(ServletContext pCtx) {
		String path = pCtx.getContextPath();
		if ("ROOT".equals(path) ||  "/".equals(path)) {
			path = "";
		}
		final String resourcedirStr = System.getProperty("bes.rsrc.dir");
		final Path resourceDir;
		if (resourcedirStr != null  &&  resourcedirStr.length() > 0) {
			final Path p = Paths.get(resourcedirStr);
			if (!Files.isDirectory(p)) {
				System.out.println("Warning: Configured resource directory not found: " + p);
				resourceDir = null;
			} else {
				resourceDir = p;
			}
		} else {
			resourceDir = null;
		}
		final ClassLoader cl = Thread.currentThread().getContextClassLoader();
		final String pth = (path == null  ||  path.length() == 0) ? null : path;
		final URL log4jXmlUrl = findResourceByPathAndUri(resourceDir, cl, pth, "log4j2.xml");
		final ILogFactory lf;
		if (log4jXmlUrl == null) {
			lf = SimpleLogFactory.ofSystemOut(Level.TRACE);
			log = lf.getLog(BesSrvInitializer.class);
			System.err.println("Warning: Log4j2 config file not found, logging to System.out with trace level.");
		} else {
			lf = Log4j2LogFactory.of(log4jXmlUrl);
			log = lf.getLog(BesSrvInitializer.class);
			log.info("newData", "Log4j2 configured from {}", log4jXmlUrl);
		}
		log.info("newData", "Logging initialized at {}", ZonedDateTime.now());
		final String factoryPropertiesUri = "bes-factory.properties";
		final URL factoryPropertiesUrl = findResourceByPathAndUri(resourceDir, cl, pth, factoryPropertiesUri);
		if (factoryPropertiesUrl == null) {
			log.error("newData", "Factory properties not found: {}", factoryPropertiesUri);
			throw new IllegalStateException("Factory properties not found: " + factoryPropertiesUri);
		}
		log.info("newData", "Loading factory properties from {}", factoryPropertiesUrl);
		final Properties factoryProperties = Streams.load(factoryPropertiesUrl);
		final String instancePropertiesUri = "bes.properties";
		final URL instancePropertiesUrl = findResourceByPathAndUri(resourceDir, cl, pth, instancePropertiesUri);
		if (instancePropertiesUrl == null) {
			log.error("newData", "Instance properties not found: {}", instancePropertiesUri);
			throw new IllegalStateException("Instance properties not found: " + instancePropertiesUri);
		}
		log.info("newData", "Loading instance properties from {}", instancePropertiesUrl);
		final Properties instanceProperties = Streams.load(instancePropertiesUrl);
		final Properties combinedProperties = new Properties(factoryProperties);
		combinedProperties.putAll(instanceProperties);
		final Module module = BesCore.BES_CORE_MODULE.extend((b) -> {
			b.bind(ILogFactory.class).toInstance(lf);
			b.bind(IPropertyFactory.class).toInstance(new DefaultPropertyFactory(factoryProperties, instanceProperties));
			b.bind(Properties.class).toInstance(combinedProperties);
			b.bind(Properties.class, "factory").toInstance(factoryProperties);
			b.bind(Properties.class, "instance").toInstance(instanceProperties);
		});
		final Application application = AfwCoreOnTheFlyBinder.applicationOf(module, "jakarta");
		
		return new Data(application.getComponentFactory());
	}

	private URL findResourceByPathAndUri(final Path pResourceDir, final ClassLoader pCl, final String pPath,
			String pUri) {
		if (pPath != null) {
			final StringBuilder sb = new StringBuilder();
			sb.append(pPath);
			if (!pPath.endsWith("/")) {
				sb.append("/");
			}
			if (pUri.startsWith("/")) {
				sb.append(pUri, 1, pUri.length());
			} else {
				sb.append(pUri);
			}
			final String uri = sb.toString();
			final URL url = findResourceByUri(pResourceDir, pCl, uri);
			if (url != null) {
				return url;
			}
		}
		return findResourceByUri(pResourceDir, pCl, pUri);
	}

	private URL findResourceByUri(final Path pResourceDir, final ClassLoader pCl, String pUri) {
		if (pResourceDir != null) {
			final Path p = pResourceDir.resolve(pUri);
			if (Files.isRegularFile(p)) {
				try {
					return p.toUri().toURL();
				} catch (IOException e) {
					throw Exceptions.show(e);
				}
			}
		}
		final URL url = pCl.getResource(pUri);
		if (log != null) {
			log.trace("findResourceByUri: uri={} -> {}", pUri, url);
		}
		if (url == null  &&  pUri.startsWith("/")) {
			return findResourceByUri(pResourceDir, pCl, pUri.substring(1));
		}
		return url;
	}

	private static Data requireData(ServletContext pSc) {
		final Data data = (Data) pSc.getAttribute(KEY_DATA);
		if (data == null) {
			throw new IllegalStateException("Application data is not available.");
		}
		return data;
	}

	public static IComponentFactory getComponentFactory(ServletContext pSc) {
		return requireData(pSc).componentFactory;
	}
}
