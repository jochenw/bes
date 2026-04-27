package com.github.jochenw.bes.core.impl;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.Map;
import java.util.Properties;
import java.util.function.Consumer;

import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import com.github.jochenw.afw.di.api.IComponentFactory;
import com.github.jochenw.bes.core.api.IBesModel.IBesPropertiesController;
import com.github.jochenw.bes.core.model.BesObject;
import com.github.jochenw.bes.core.model.BesProperty;
import com.github.jochenw.bes.core.model.BesPropertySet;

class DefaultBesPropertiesControllerTest {
	private static IComponentFactory CF;
	
	@BeforeAll
	static void initCf() {
		CF = Tests.newCf(true);
	}

	@BeforeEach
	void initDb() throws Exception {
		Tests.initDb(CF);
	}

	protected IBesPropertiesController getPropertiesController() {
		return CF.requireInstance(IBesPropertiesController.class);
	}

	@Test
	void testCreate() {
		final Properties props = new Properties();
		props.put("foo", "bar");
		props.put("answer", "42");
		props.put("whatever", "works");
		final IBesPropertiesController pc = getPropertiesController();
		final BesPropertySet bps = pc.insert(props);
		assertEquals(1l, bps.getId().getId());
		assertPropertySet(bps, BesPropertySet.Id.of(1l), props);
	}

	protected void assertPropertySet(BesPropertySet pBps, BesPropertySet.Id pId, Properties pProperties) {
		final Consumer<BesPropertySet> validator = (bps) -> {
			assertEquals(pId.getId(), bps.getId().getId());
			final Map<String,BesProperty> map = pBps.getPropertyMap();
			assertNotNull(map);
			assertEquals(pProperties.size(), map.size());
			pProperties.forEach((k,v) -> {
				final String key = (String) k;
				final BesProperty bp = map.get(key);
				assertNotNull(bp);
				if (BesObject.Id.isNullId(pId)) {
					assertTrue(BesObject.Id.isNullId(bp.getSetId()));
				} else {
					assertFalse(BesObject.Id.isNullId(bp.getSetId()));
					assertEquals(pId.getId(), bp.getSetId().getId());
				}
				assertEquals(k, bp.getKey());
				assertEquals(v, bp.getValue());
			});
			final Properties bpProps = bps.getProperties();
			assertNotNull(bpProps);
			assertEquals(pProperties.size(), bpProps.size());
			pProperties.forEach((k,v) -> {
				final String key = (String) k;
				assertEquals(v, bpProps.getProperty(key));
			});
		};
		validator.accept(pBps);
		final BesPropertySet bps = getPropertiesController().getPropertySetById(pId);
		validator.accept(bps);
	}
}
