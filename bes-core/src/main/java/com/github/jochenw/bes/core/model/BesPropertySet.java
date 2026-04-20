package com.github.jochenw.bes.core.model;

import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.text.Collator;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Properties;
import java.util.function.BiConsumer;
import java.util.function.Function;
import java.util.stream.Stream;

import com.github.jochenw.afw.core.util.Exceptions;
import com.github.jochenw.afw.core.util.Objects;
import com.github.jochenw.afw.core.util.Reflection;


public class BesPropertySet extends BesObject<BesPropertySet.Id> {
	public static class Id extends BesObject.Id {
		protected Id(Long pId) {
			super(pId);
		}

		public static Id of(long pId) { return new Id(Long.valueOf(pId)); }
		public static Id of(Long pId) {
			final Long id = Objects.requireNonNull(pId);
			return new Id(id);
		}
		public static Id noId() { return new Id(null); }
	}

	private final Map<String, BesProperty> properties = new HashMap<>();
	private byte[] digest;

	public BesPropertySet(Id pId) {
		super(pId);
	}

	public byte[] getDigest() { return digest; }
	public void setDigest(byte[] pDigest) { digest = pDigest; }

	public Properties getProperties() {
		final Properties props = new Properties();
		properties.forEach((k,p) -> props.put(k, p.getValue()));
		return props;
	}
	public Map<String, BesProperty> getPropertyMap() {
		return Collections.unmodifiableMap(properties);
	}

	public void setProperties(Properties pProperties) {
		final Map<String, BesProperty> newMap = new HashMap<>();
		pProperties.forEach((k,v) -> {
			BesProperty bp = updateProperty((String) k, (String) v);
			newMap.put((String) k, bp);
		});
		properties.clear();
		properties.putAll(newMap);
	}

	private BesProperty updateProperty(String pKey, String pValue) {
		BesProperty bp = properties.get(pKey);
		boolean create;
		if (bp == null) {
			create = true;
		} else {
			final BesProperty.Id id = bp.getId();
			if (BesObject.Id.isNullId(id)) {
				create = true;
			} else {
				create = bp.getValue().equals(pValue);
			}
		}
		if (create) {
			return new BesProperty(BesProperty.Id.noId(), pKey, pValue);
		}
		return bp;
	}

	public void setProperty(String pKey, String pValue) {
		setProperty(pKey, updateProperty(pKey, pValue));
	}
	public void setProperty(String pKey, BesProperty pProperty) {
		properties.put(pKey, pProperty);
	}
	public String getProperty(String pKey) {
		final BesProperty bps = properties.get(pKey);
		if (bps == null) {
			return null;
		} else {
			return bps.getValue();
		}
	}
	public void forEach(BiConsumer<String,String> pConsumer) {
		properties.forEach((k,bp) -> pConsumer.accept(k, bp.getValue()));
	}

	@Override
	protected Object clone() {
		final BesPropertySet bps = new BesPropertySet(getId());
		bps.properties.putAll(properties);
		return bps;
	}

	public static byte[] getDigest(BesPropertySet pBps) {
		return getDigest(pBps.properties);
	}
	public static byte[] getDigest(Map<String, BesProperty> pProperties) {
		final Function<Comparator<String>, Stream<String>> streamProducer = (comparator) -> {
			final List<String> keys = new ArrayList<>(pProperties.keySet());
			keys.sort(comparator);
			final List<String> list = new ArrayList<>();
			keys.forEach((k) -> {
				final BesProperty bp = pProperties.get(k);
				list.add(k);
				list.add(bp.getValue());
			});
			return list.stream();
		};
		return getDigest(streamProducer);
	}
	public static byte[] getDigest(Properties pProperties) {
		final Function<Comparator<String>, Stream<String>> streamProducer = (comparator) -> {
			final List<Object> oKeys = new ArrayList<Object>(pProperties.keySet());
			final List<String> keys = Reflection.cast(oKeys);
			keys.sort(comparator);
			final List<String> list = new ArrayList<>();
			keys.forEach((k) -> {
				list.add(k);
				list.add(pProperties.getProperty(k));
			});
			return list.stream();
		};
		return getDigest(streamProducer);
	}
	private static byte[] getDigest(Function<Comparator<String>, Stream<String>> pStreamProducer) {
		// The use of Locale.GERMANY is arbitrarily. We could use basically any other Locale
		// here. The point here is to have a reproducible, and guaranteed sort order.
		final Comparator<String> comparator = Collator.getInstance(Locale.GERMANY)::compare;
		final Stream<String> stream = pStreamProducer.apply(comparator);
		final MessageDigest md;
		try {
			md = MessageDigest.getInstance("SHA-256");
		} catch (NoSuchAlgorithmException e) {
			throw Exceptions.show(e);
		}
		stream.forEach((s) -> {
			if (s != null) {
				md.update(s.getBytes(StandardCharsets.UTF_8));
			}
		});
		final byte[] bytes = md.digest();
		if (bytes.length != 32) {
			throw new IllegalStateException("Expected 32 bytes, got " + bytes.length);
		}
		return bytes;
	}
}
