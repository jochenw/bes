package com.github.jochenw.bes.core.model;

import com.github.jochenw.afw.core.util.Objects;


public class BesProperty extends BesObject<BesProperty.Id> {
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

	private final String key, value;

	public BesProperty(BesProperty.Id pId, String pKey, String pValue) {
		super(pId);
		key = pKey;
		value = pValue;
	}

	public String getKey() { return key; }
	public String getValue() { return value; }

	@Override
	protected Object clone() {
		return new BesProperty(getId(), getKey(), getValue());
	}
}
