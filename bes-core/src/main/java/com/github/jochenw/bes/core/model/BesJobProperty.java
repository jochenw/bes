package com.github.jochenw.bes.core.model;


public class BesJobProperty extends BesBean<BesJobProperty.Id> {
	public static class Id extends BesBean.Id {
		public Id(long pId) {
			super(pId);
		}

		public Id(Long pId) {
			super(pId);
		}
	}

	public BesJobProperty(Id pId) {
		super(pId);
	}

	private BesJob.Id jobId;
	private String key, value;

	public String getKey() { return key; }
	public void setKey(String pKey) { key = pKey; }
	public String getValue() { return value; }
	public void setValue(String pValue) { value = pValue;}
	public BesJob.Id getJobId() { return jobId; }
	public void setJobId(BesJob.Id pJobId) { jobId = pJobId; }
}
