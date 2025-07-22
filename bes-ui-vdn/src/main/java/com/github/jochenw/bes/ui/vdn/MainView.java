package com.github.jochenw.bes.ui.vdn;

import com.github.jochenw.afw.core.log.ILog;
import com.github.jochenw.afw.core.log.ILogFactory;
import com.github.jochenw.afw.core.util.Objects;
import com.github.jochenw.afw.di.api.IComponentFactory;
import com.github.jochenw.afw.vdn.Components;
import com.github.jochenw.bes.ui.vdn.srvlt.BesSrvInitializer;
import com.github.jochenw.bes.ui.vdn.views.UsersView;
import com.vaadin.flow.component.orderedlayout.VerticalLayout;
import com.vaadin.flow.component.tabs.TabSheet;
import com.vaadin.flow.router.Route;
import com.vaadin.flow.server.VaadinServlet;

@Route
public class MainView extends VerticalLayout {
	private static final long serialVersionUID = 428329137934022922L;
	private final IComponentFactory componentFactory;
	private final ILogFactory logFactory;
	private final TabSheet tabSheet;
	private final UsersView usersView;
	private final ILog log;

    public MainView() {
    	componentFactory = Objects.requireNonNull(BesSrvInitializer.getComponentFactory(VaadinServlet.getCurrent().getServletContext()));
    	logFactory = Objects.requireNonNull(componentFactory.requireInstance(ILogFactory.class));
    	log = Objects.requireNonNull(logFactory.getLog(MainView.class));
    	log.info("<init>", "Created");
    	tabSheet = new TabSheet();
    	tabSheet.setSizeFull();
    	add(tabSheet);
    	log.info("<init>", "Creating UsersView");
    	usersView = new UsersView(this);
    	tabSheet.add("Users", usersView);
    	log.info("<init>", "Done.");
    }

    public IComponentFactory getCf() { return componentFactory; }
    public ILogFactory getLf() { return Objects.requireNonNull(logFactory); }
}
