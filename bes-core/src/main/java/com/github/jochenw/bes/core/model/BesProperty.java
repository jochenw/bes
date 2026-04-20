package com.github.jochenw.bes.core.model;


public class BesProperty {
	private final BesPropertySet.Id setId;
	private final String key, value;

	public BesProperty(BesPropertySet.Id pSetId, String pKey, String pValue) {
		setId = pSetId;
		key = pKey;
		value = pValue;
	}

	public BesPropertySet.Id getSetId() { return setId; }
	public String getKey() { return key; }
	public String getValue() { return value; }

	@Override
	protected Object clone() {
		return new BesProperty(getSetId(), getKey(), getValue());
	}
}
