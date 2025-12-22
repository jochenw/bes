package com.github.jochenw.bes.core.model;

public abstract class BesBean<I extends BesBean.Id> {
	public abstract static class Id {
		private final Long id;
		protected Id(Long pId) {
			id = pId;
		}
		public long longValue() {
			return id.longValue();
		}
		public Long getLongValue() {
			return id;
		}
	}

	private I id;

	protected BesBean(I pId) {
		id = pId;
	}

	public I getId() { return id; }
	void setId(I pId) { id = pId; }
}
