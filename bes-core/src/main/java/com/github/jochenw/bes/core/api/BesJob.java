package com.github.jochenw.bes.core.api;

import com.github.jochenw.afw.core.util.Objects;
import com.github.jochenw.afw.core.util.Strings;
import com.github.jochenw.bes.core.api.IBesModel.BesBean;

public class BesJob extends BesBean<BesJob.Id> {
	public static class Id extends IBesModel.Id {
		Id(Long pId) {
			super(pId);
		}
	}
	private final BesUser.Id ownerId;
	private final String name;
	protected BesJob(BesJob.Id pId, BesUser.Id pOwnerId, String pName) {
		super(pId);
		ownerId = pOwnerId;
		name = pName;
	}
	public BesUser.Id getOwnerId() { return ownerId; }
	public String getName() { return name; }
	
	public static BesJob of(long pId, Long pOwnerId, String pName) {
		return new BesJob(new BesJob.Id(Long.valueOf(pId)),
				          new BesUser.Id(Objects.requireNonNull(pOwnerId, "OwnerId")),
				          Strings.requireNonNull(pName, "Name"));
	}
	public static BesJob of(Long pOwnerId, String pName) {
		return of(-1l, Objects.requireNonNull(pOwnerId, "OwnerId"), Strings.requireNonNull(pName, "Name"));
	}
	public static BesJob of(long pId, BesJob pJob) {
		return of(pId, pJob.getOwnerId().getIdObj(), pJob.getName());
	}
}
