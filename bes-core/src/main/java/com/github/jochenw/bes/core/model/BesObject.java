package com.github.jochenw.bes.core.model;

public abstract class BesObject<ID extends BesObject.Id> {
	public abstract static class Id {
		private final Long id;

		protected Id(Long pId) {
			id = pId;
		}

		public long getId() { return id.longValue(); }
		public Long getIdObj() { return id; }
	}

	private final ID id;

	protected BesObject(ID pId) {
		id = pId;
	}

	protected abstract Object clone();

	public ID getId() { return id; }
}
