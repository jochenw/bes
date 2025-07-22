package com.github.jochenw.bes.core.api;


public class BesUser extends IBesModel.BesBean<BesUser.Id> {
	public static class Id extends IBesModel.Id {
		Id(Long pId) {
			super(pId);
		}
	}
	private final String userId, name, email;
	
	private BesUser(long pId, String pUserId, String pName, String pEmail) {
		super(new BesUser.Id(Long.valueOf(pId)));
		userId = pUserId;
		name = pName;
		email = pEmail;
	}

	public String getUserId() { return userId; }
	public String getName() { return name; }
	public String getEmail() { return email; }

	public static BesUser of(long pId, String pUserId, String pName, String pEmail) {
		return new BesUser(pId, pUserId, pName, pEmail);
	}
	public static BesUser of(String pUserId, String pName, String pEmail) {
		return new BesUser(-1l, pUserId, pName, pEmail);
	}
	public static BesUser of(long id, BesUser pUser) {
		return new BesUser(id, pUser.getUserId(), pUser.getName(), pUser.getEmail());
	}
}
