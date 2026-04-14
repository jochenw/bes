package com.github.jochenw.bes.core.model;

import com.github.jochenw.afw.core.util.Objects;

public class BesUser extends BesObject<BesUser.Id> {
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

	private String userId, email, name;

	
	protected BesUser(Id pId) {
		super(pId);
	}

	@Override
	protected Object clone() {
		return of(super.getId(), userId, email, name);
	}

	public static BesUser of(BesUser.Id pId, String pUserId, String pEmail, String pName) {
		final BesUser.Id id = Objects.requireNonNull(pId, "Id");
		final String userId = Objects.requireNonNull(pUserId, "UserId");
		final String email = Objects.requireNonNull(pEmail, "Email");
		final BesUser bu = new BesUser(id);
		bu.userId = userId;
		bu.email = email;
		bu.name = pName;
		return bu;
	}
	public static BesUser of(BesUser.Id pId, BesUser pUser) {
		return of(pId, pUser.getUserId(), pUser.getEmail(), pUser.getName());
	}
	
	public String getUserId() { return userId; }
	public void setUserId(String userId) { this.userId = userId; }
	public String getEmail() { return email; }
	public void setEmail(String email) { this.email = email; }
	public String getName() { return name; }
	public void setName(String name) { this.name = name; }
}
