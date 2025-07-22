package com.github.jochenw.bes.core.api;

import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

import com.github.jochenw.afw.core.util.Objects;
import com.github.jochenw.bes.core.api.IBesModel.BesBean;

public class BesParameterSet extends BesBean<BesParameterSet.Id> {
	public static class Id extends IBesModel.Id {
		Id(Long pId) {
			super(pId);
		}
	}
	private final Map<String,String> parameters;

	private BesParameterSet(BesParameterSet.Id pId, Map<String,String> pParameters) {
		super(pId);
		parameters = pParameters;
	}

	public Map<String, String> getParameters() { return parameters; }
	public String getValue(String pKey) { return parameters.get(pKey); }
	public String requireValue(String pKey) { return parameters.get(pKey); }
	
	public BesParameterSet of(Long pId, Map<String,String> pParameters) {
		final Map<String,String> map0 = Objects.requireNonNull(pParameters, "Parameters");
		final Map<String,String> map1 = new HashMap<>(map0);
		final Map<String,String> unmodifiableMap = Collections.unmodifiableMap(map1);
		return new BesParameterSet(new BesParameterSet.Id(pId), unmodifiableMap);
	}

	public BesParameterSet of(Map<String,String> pParameters) {
		return of(-1l, pParameters);
	}

	public BesParameterSet of(long pId, BesParameterSet pParameterSet) {
		return of(pId, pParameterSet.getParameters());
	}
}
