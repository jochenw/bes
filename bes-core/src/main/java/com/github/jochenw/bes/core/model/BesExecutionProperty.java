package com.github.jochenw.bes.core.model;


public class BesExecutionProperty extends BesBean<BesExecutionProperty.Id> {
	public static class Id extends BesBean.Id {
		public Id(long pId) {
			super(pId);
		}

		public Id(Long pId) {
			super(pId);
		}
	}

	public BesExecutionProperty(Id pId) {
		super(pId);
	}

	private BesExecution.Id executionId;
	private String key, value;

	public String getKey() { return key; }
	public void setKey(String pKey) { key = pKey; }
	public String getValue() { return value; }
	public void setValue(String pValue) { value = pValue;}
	public BesExecution.Id getExecutionId() { return executionId; }
	public void setExecutionId(BesExecution.Id pExecutionId) { executionId = pExecutionId; }
}
