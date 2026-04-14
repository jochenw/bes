package com.github.jochenw.bes.core.impl;

import com.github.jochenw.afw.di.api.IComponentFactory;
import com.github.jochenw.afw.di.api.IComponentFactoryAware;
import com.github.jochenw.bes.core.api.IBesModel;

public class DefaultBesModel implements IBesModel, IComponentFactoryAware {
	private DefaultBesUserController besUserController;

	@Override
	public void init(IComponentFactory pCf) {
		besUserController = (DefaultBesUserController) pCf.requireInstance(IBesUserController.class);
	}


	@Override
	public IBesUserController getUserController() { return besUserController; }
}
