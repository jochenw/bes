package com.github.jochenw.bes.core.model;


public class BesMediumBlob extends BesBean<BesMediumBlob.Id> {
	public static class Id extends BesBean.Id {
		public Id(long pId) {
			super(pId);
		}

		public Id(Long pId) {
			super(pId);
		}
	}

	public BesMediumBlob(Id pId) {
		super (pId);
	}
}
