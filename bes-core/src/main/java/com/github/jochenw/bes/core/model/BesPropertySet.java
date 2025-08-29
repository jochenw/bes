package com.github.jochenw.bes.core.model;

import java.util.Map;

import com.github.jochenw.bes.core.model.BesBean.Id;

public class BesPropertySet extends BesBean<Id> {
	public static class Id extends BesBean.Id {
		public Id(long pId) {
			super(pId);
		}

		public Id(Long pId) {
			super(pId);
		}
	}

	private byte[] sha256Sum;
	private Map<String,String> map;

	public BesPropertySet(com.github.jochenw.bes.core.model.BesBean.Id pId) {
		super(pId);
	}

	public byte[] getSha256Sum() { return sha256Sum; }
	public void setSha256Sum(byte[] pSha256Sum) { sha256Sum = pSha256Sum; }
	public Map<String, String> getMap() { return map; }
	public void setMap(Map<String, String> pMap) { map = pMap; }
}
