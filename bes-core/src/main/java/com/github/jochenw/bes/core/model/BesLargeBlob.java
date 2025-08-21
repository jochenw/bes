package com.github.jochenw.bes.core.model;


public class BesLargeBlob extends BesBean<BesLargeBlob.Id> {
	public static class Id extends BesBean.Id {
		public Id(long pId) {
			super(pId);
		}

		public Id(Long pId) {
			super(pId);
		}
	}

	public BesLargeBlob(Id pId) {
		super (pId);
	}
}
