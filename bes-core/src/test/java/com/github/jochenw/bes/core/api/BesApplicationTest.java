package com.github.jochenw.bes.core.api;

import static org.junit.jupiter.api.Assertions.*;

import org.junit.jupiter.api.Test;

import com.github.jochenw.afw.di.api.IComponentFactory;
import com.github.jochenw.afw.di.api.IModule;
import com.github.jochenw.bes.core.impl.FlywayDbInitializer;
import com.github.jochenw.bes.core.impl.Tests;

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
			b.bind(Startable.class).to((cf) -> startable);
		};
		final IComponentFactory cf = Tests.newCf(module, false);
		assertNotNull(cf);
		assertNotNull(cf.requireInstance(FlywayDbInitializer.class));
		assertSame(startable, cf.getInstance(Startable.class));
		assertTrue(startable.isStarted());
	}

}
