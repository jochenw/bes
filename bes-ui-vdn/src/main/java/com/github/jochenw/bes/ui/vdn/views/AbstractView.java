package com.github.jochenw.bes.ui.vdn.views;

import com.github.jochenw.afw.core.log.ILogFactory;
import com.github.jochenw.afw.di.api.IComponentFactory;
import com.github.jochenw.bes.ui.vdn.MainView;
import com.vaadin.flow.component.orderedlayout.VerticalLayout;


public abstract class AbstractView extends VerticalLayout {
	private static final long serialVersionUID = -4726286873092656652L;
	private final MainView mainView;

	public AbstractView(MainView pMainView) {
		mainView = pMainView;
		initContent();
	}

	public MainView getMainView() { return mainView; }
	public IComponentFactory getCf() { return getMainView().getCf(); }
	public ILogFactory getLf() { return getMainView().getLf(); }

	protected abstract void initContent();
}
