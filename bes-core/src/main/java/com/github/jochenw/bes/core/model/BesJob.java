package com.github.jochenw.bes.core.model;

public class BesJob extends BesBean<BesJob.Id> {
	public static class Id extends BesBean.Id {
		public Id(long pId) {
			super(pId);
		}

		public Id(Long pId) {
			super(pId);
		}
	}

	private BesUser.Id ownerId;
	private String name;

	public BesJob(Id pId) {
		super(pId);
	}

	public BesUser.Id getOwnerId() { return ownerId; }
	public void setOwnerId(BesUser.Id pOwner) { ownerId = pOwner; }
	public String getName() { return name; }
	public void setName(String pName) { name = pName; }
}
