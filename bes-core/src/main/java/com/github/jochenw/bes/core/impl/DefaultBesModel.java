package com.github.jochenw.bes.core.impl;

import com.github.jochenw.afw.di.api.IComponentFactory;
import com.github.jochenw.bes.core.api.IBesModel;

import jakarta.inject.Inject;


public class DefaultBesModel implements IBesModel {
	private @Inject IComponentFactory componentFactory;
	private DefaultBesUserController besUserController;
	private DefaultBesPropertiesController besPropertiesController;
	
	@Override
	public IBesPropertiesController getPropertiesController() {
		synchronized(this) {
			if (besPropertiesController == null) {
				besPropertiesController = (DefaultBesPropertiesController) componentFactory.requireInstance(IBesPropertiesController.class);
			}
			return besPropertiesController;
		}
	}

	@Override
	public IBesUserController getUserController() {
		synchronized(this) {
			if (besUserController == null) {
				besUserController = (DefaultBesUserController) componentFactory.requireInstance(IBesUserController.class);
			}
			return besUserController;
		}
	}
}
