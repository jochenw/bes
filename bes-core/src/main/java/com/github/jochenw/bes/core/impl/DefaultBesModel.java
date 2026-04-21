package com.github.jochenw.bes.core.impl;

import com.github.jochenw.bes.core.api.IBesModel;

import jakarta.inject.Inject;


public class DefaultBesModel implements IBesModel {
	private @Inject IBesUserController besUserController;
	private @Inject IBesPropertiesController besPropertiesController;

	
	@Override
	public IBesPropertiesController getPropertiesController() { return besPropertiesController; }

	@Override
	public IBesUserController getUserController() { return besUserController; }
}
