package com.github.jochenw.bes.core.model;

public class BesJob extends BesBean<BesJob.Id> {

	public static class Id extends BesBean.Id {
		public Id(Long pId) {
			super(pId);
		}
	}

	private BesUser.Id owner;
	private String name;
	private String type;

	public BesJob(Id pId) {
		super(pId);
	}

	public BesUser.Id getOwner() { return owner; }
	public void setOwner(BesUser.Id pOwner) { owner = pOwner; }
	public String getName() { return name; }
	public void setName(String pName) { name = pName; }
	public String getType() { return type; }
	public void setType(String pType) { type = pType; }
}
