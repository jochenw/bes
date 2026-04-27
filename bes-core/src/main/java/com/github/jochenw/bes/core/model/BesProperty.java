package com.github.jochenw.bes.core.model;


public class BesProperty {
	private final BesPropertySet.Id setId;
	private final String key, value;

	private BesProperty(BesPropertySet.Id pSetId, String pKey, String pValue) {
		setId = pSetId;
		key = pKey;
		value = pValue;
	}

	public BesPropertySet.Id getSetId() { return setId; }
	public String getKey() { return key; }
	public String getValue() { return value; }

	public static BesProperty of(BesPropertySet.Id pId, boolean pNullIdValid, String pKey, String pValue) {
		if (BesObject.Id.isNullId(pId)  &&  !pNullIdValid) {
			throw new IllegalStateException("Property setId is null.");
		}
		return new BesProperty(pId, pKey, pValue);
	}
	
	@Override
	protected Object clone() {
		return new BesProperty(getSetId(), getKey(), getValue());
	}
}
