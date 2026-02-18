package com.github.jochenw.bes.ui.vdn;

import java.util.Properties;

import com.github.jochenw.afw.core.inject.AfwCoreOnTheFlyBinder;
import com.github.jochenw.afw.core.log.ILog.Level;
import com.github.jochenw.afw.core.log.ILogFactory;
import com.github.jochenw.afw.core.log.simple.SimpleLogFactory;
import com.github.jochenw.afw.core.props.DefaultPropertyFactory;
import com.github.jochenw.afw.core.props.IPropertyFactory;
import com.github.jochenw.afw.di.api.IComponentFactory;
import com.github.jochenw.bes.core.api.IBesModel;
import com.github.jochenw.bes.core.api.IBesModel.IBesBeanController;
import com.github.jochenw.bes.core.api.IBesModel.IBesUserController;
import com.github.jochenw.bes.core.model.BesUser;
import com.github.jochenw.bes.ui.vdn.util.Grids;
import com.github.jochenw.bes.ui.vdn.util.Grids.GridBuilder;
import com.vaadin.flow.component.Component;
import com.vaadin.flow.component.grid.Grid;
import com.vaadin.flow.component.orderedlayout.VerticalLayout;
import com.vaadin.flow.component.tabs.TabSheet;
import com.vaadin.flow.router.Route;

@Route
public class MainView extends VerticalLayout {
	private static final long serialVersionUID = 4636205559147789038L;

	public MainView() {
		final TabSheet tabSheet = new TabSheet();
		add(tabSheet);
		tabSheet.add("Users", newBesUsersView());
	}

	protected Component newBesUsersView() {
		final VerticalLayout vl = new VerticalLayout();
		final IComponentFactory cf = AfwCoreOnTheFlyBinder.applicationOf(newBindingModule(), "jakarta").getComponentFactory();
		cf.init(this);
		final IBesModel model = cf.requireInstance(IBesModel.class);
		final IBesUserController userCntrllr = model.getUserController();
		final GridBuilder<BesUser> gridBuilder = Grids.builder(BesUser.class)
				.column("Id", "Id", Long.class, (BesUser bu) -> bu.getId().getLongValue()).end()
				.column("uId", "User Id", String.class, BesUser::getUserId).end()
				.column("name", "Name", String.class, BesUser::getName).end()
				.column("email", "Email", String.class, BesUser::getEmail).end();
		final Grid<BesUser> usersGrid = gridBuilder.grid();
		return usersGrid;
	}

	protected com.github.jochenw.afw.di.api.Module newBindingModule() {
		final ILogFactory slf = SimpleLogFactory.ofSystemOut(Level.TRACE);
		final IPropertyFactory pf = new DefaultPropertyFactory(new Properties());
		return (b) -> {
			b.bind(ILogFactory.class).toInstance(slf);
			b.bind(IPropertyFactory.class).toInstance(pf);
		};
	}
}

