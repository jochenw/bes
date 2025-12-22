package com.github.jochenw.bes.core.model;

public class BesUser extends BesBean<BesUser.Id> {
	public static class Id extends BesBean.Id {
		public Id(Long pId) {
			super(pId);
		}
	}

	private String userId, name, email;

	public BesUser(BesUser.Id pId) {
		super(pId);
	}

	public String getUserId() { return userId; }
	public void setUserId(String pUserId) { userId = pUserId; }
	public String getName() { return name; }
	public void setName(String pName) { name = pName; }
	public String getEmail() { return email; }
	public void setEmail(String pEmail) { email = pEmail; }
}
