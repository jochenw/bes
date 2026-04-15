package com.github.jochenw.bes.core.impl;

import jakarta.inject.Inject;

import java.sql.Connection;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;
import java.util.function.Consumer;

import javax.sql.DataSource;

import com.github.jochenw.afw.core.jdbc.JdbcHelper;
import com.github.jochenw.afw.core.log.ILog;
import com.github.jochenw.afw.core.util.Objects;
import com.github.jochenw.afw.di.api.IComponentFactory;
import com.github.jochenw.afw.di.api.IComponentFactoryAware;
import com.github.jochenw.afw.di.api.LogInject;
import com.github.jochenw.bes.core.api.IBesModel;
import com.github.jochenw.bes.core.api.IBesModel.IBesObjectController;
import com.github.jochenw.bes.core.api.IBesModel.IListener;
import com.github.jochenw.bes.core.model.BesObject;


public abstract class AbstractBesObjectController<ID extends BesObject.Id,O extends BesObject<ID>> implements IBesObjectController<ID, O>, IComponentFactoryAware {
	private DefaultBesModel model;
	private @Inject DataSource connectionProvider;
	private @LogInject ILog log;
	private final List<IListener<ID,O>> listeners = new ArrayList<>();
	private @Inject JdbcHelper jdbcHelper;

	@Override
	public void init(IComponentFactory pCf) {
		model = (DefaultBesModel) pCf.requireInstance(IBesModel.class);
	}

	@Override
	public void add(IListener<ID, O> pListener) {
		final IListener<ID, O> listener = Objects.requireNonNull(pListener, "Listener");
		synchronized(listeners) {
			listeners.add(listener);
		}
	}

	@Override
	public void remove(IListener<ID, O> pListener) {
		final IListener<ID, O> listener = Objects.requireNonNull(pListener, "Listener");
		synchronized(listeners) {
			listeners.remove(listener);
		}
	}

	protected Long newId(Connection pConnection, String pSequenceName) {
		final String sql = "SELECT NEXT VALUE FOR " + pSequenceName;
		final long id = getJdbcHelper().query(pConnection, sql).countLong();
		return Long.valueOf(id);
	}
	protected DefaultBesModel getModel() { return model; }
	protected Connection newConnection() throws SQLException { return connectionProvider.getConnection(); }
	protected JdbcHelper getJdbcHelper() { return jdbcHelper; }

	protected void notifyListeners(Consumer<IListener<ID,O>> pNotification) {
		synchronized(listeners) {
			listeners.forEach(pNotification);
		}
	}

	protected boolean isNullId(BesObject.Id pId) {
		if (pId == null) {
			return true;
		}
		final Long id = pId.getIdObj();
		return id == null  ||  id.longValue() == 0;
	}
}
