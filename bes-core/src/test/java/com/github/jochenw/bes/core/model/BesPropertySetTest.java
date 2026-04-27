package com.github.jochenw.bes.core.model;

import static org.junit.jupiter.api.Assertions.*;

import org.junit.jupiter.api.Test;

class BesPropertySetTest {
	@Test
	void testCreatePropertySet() {
		final BesPropertySet bps = newPropertySet(1l, "propA", "Some Property value",
				                                  "propB", "Another Property value",
				                                  "foo", "bar");
		// Validate contents
		assertEquals(3, bps.getPropertyMap().size());
		assertEquals("Some Property value", bps.getProperty("propA"));
		assertEquals("Another Property value", bps.getProperty("propB"));
		assertEquals("bar", bps.getProperty("foo"));
		assertNull(bps.getProperty("PropA"));
	}

	@Test
	void testDigest() {
		final BesPropertySet bps0 = newPropertySet(1l, "propA", "Some Property value",
                "propB", "Another Property value",
                "foo", "bar");
		final byte[] digest0 = BesPropertySet.getDigest(bps0);
		// Same properties, different order, should produce the same digest.
		final BesPropertySet bps1 = newPropertySet(2l, "propA", "Some Property value",
				"foo", "bar",
                "propB", "Another Property value");
		final byte[] digest1 = BesPropertySet.getDigest(bps1);
		assertTrue(isEqual(digest0, digest1));
		// Same properties, different case, should produce a different digest.
		final BesPropertySet bps2 = newPropertySet(3l, "PropA", "Some Property value",
                "propB", "Another Property value",
                "foo", "bar");
		final byte[] digest2 = BesPropertySet.getDigest(bps2);
		assertFalse(isEqual(digest0, digest2));
	}

	protected boolean isEqual(byte[] pExpect, byte[] pActual) {
		if (pExpect.length != pActual.length) {
			return false;
		} else {
			for (int i = 0;  i < pExpect.length;  i++) {
				if (pExpect[i] != pActual[i]) {
					return false;
				}
			}
			return true;
		}
	}
	BesPropertySet newPropertySet(long pId, String... pValues) {
		final BesPropertySet.Id id = BesPropertySet.Id.of(pId);
		final BesPropertySet bps = new BesPropertySet(id);
		for (int i = 0;  i < pValues.length;  i += 2) {
			final String k = pValues[i];
			final String v = pValues[i+1];
			bps.setProperty(k,  v);
		}
		return bps;
	}
}
