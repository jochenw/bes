package com.github.jochenw.bes.core.model;

public abstract class BesBean<I extends BesBean.Id> {
	private final I id;
	public static class Id {
		private final Long id;
		protected Id(Long pId) {
			id = pId;
		}
		protected Id(long pId) {
			this (Long.valueOf(pId));
		}

		public long getId() { return id; }
	}

	protected BesBean(I pId) {
		id = pId;
	}

	public I getId() { return id; }
}
