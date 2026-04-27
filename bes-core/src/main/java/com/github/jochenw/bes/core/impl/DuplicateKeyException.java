package com.github.jochenw.bes.core.impl;

public class DuplicateKeyException extends RuntimeException {
	private static final long serialVersionUID = 6030236083768226043L;
	private final String attribute, value;

	public DuplicateKeyException(String pAttribute, String pValue, String pMessage) {
		super(pMessage);
		attribute = pAttribute;
		value = pValue;
	}

	public DuplicateKeyException(String pAttribute, String pValue, Throwable pCause) {
		super(pCause);
		attribute = pAttribute;
		value = pValue;
	}

	public DuplicateKeyException(String pAttribute, String pValue, String pMessage, Throwable pCause) {
		super(pMessage, pCause);
		attribute = pAttribute;
		value = pValue;
	}

	public String getAttribute() { return attribute; }
	public String getValue() { return value; }
}
