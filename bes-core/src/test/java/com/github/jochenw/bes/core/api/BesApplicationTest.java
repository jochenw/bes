package com.github.jochenw.bes.core.api;

import static org.junit.jupiter.api.Assertions.*;

import org.junit.jupiter.api.Test;

import com.github.jochenw.afw.di.api.IComponentFactory;
import com.github.jochenw.afw.di.api.IModule;
import com.github.jochenw.bes.core.impl.FlywayDbInitializer;

import jakarta.annotation.PostConstruct;

class BesApplicationTest {
	private static class Startable {
		private boolean started;

		@PostConstruct
		public void run() {
			started = true;
		}

		public boolean isStarted() { return started; }
	}

	@Test
	void test() {
		final Startable startable = new Startable();
		assertFalse(startable.isStarted());
		assertNull(BesApplication.getInstance());
		final IModule module = (b) -> {
			b.bind(Startable.class).toInstance(startable);
		};
		BesApplication.setInstance(module, "bes-factory.properties", "bes-test.properties");
		final IComponentFactory cf = BesApplication.getInstance().getComponentFactory();
		assertNotNull(cf);
		assertNotNull(cf.requireInstance(FlywayDbInitializer.class));
		assertTrue(startable.isStarted());
		assertSame(startable, cf.getInstance(Startable.class));
	}

}
