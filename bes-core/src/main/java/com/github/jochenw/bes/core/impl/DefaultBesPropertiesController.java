package com.github.jochenw.bes.core.impl;

import java.sql.Connection;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Properties;
import java.util.function.Consumer;

import com.github.jochenw.afw.core.util.Exceptions;
import com.github.jochenw.afw.core.util.Objects;
import com.github.jochenw.bes.core.api.IBesModel.IBesPropertiesController;
import com.github.jochenw.bes.core.model.BesProperty;
import com.github.jochenw.bes.core.model.BesPropertySet;
import com.github.jochenw.bes.core.model.BesPropertySet.Id;


public class DefaultBesPropertiesController extends AbstractBesObjectController<BesPropertySet.Id,BesPropertySet> implements IBesPropertiesController {
	private static final String TABLE = "BesPropertySets";
	private static final String FIELDS = "id, digest";
	private static final String TABLE2 = "BesProperties";
	private static final String FIELDS2 = "id, setId, pKey, pValue";

	@Override
	public void readAll(Consumer<BesPropertySet> pConsumer) {
		final String sql = "SELECT " + FIELDS + " FROM " + TABLE;
		final List<BesPropertySet> list = new ArrayList<>(51);
		try (Connection conn = newConnection()) {
			getJdbcHelper().query(conn, sql).runWithRows((rs) -> {
				while(rs.next()) {
					final Long id = rs.nextLongObj();
					if (id == null) {
						throw new NullPointerException("BesPropertySets.id");
					}
					final byte[] digest = rs.nextBytes();
					if (digest == null) {
						throw new NullPointerException("BesPropertySet.digest");
					}
					final BesPropertySet bps = new BesPropertySet(BesPropertySet.Id.of(id));
					bps.setDigest(digest);
					list.add(bps);
					if (list.size() == 50) {
						fillPropertyValues(conn, list, pConsumer);
					}
				}
				if (!list.isEmpty()) {
					fillPropertyValues(conn, list, pConsumer);
				}
			});
		} catch (SQLException se) {
			throw Exceptions.show(se);
		}
	}

	protected void fillPropertyValues(Connection pConn, List<BesPropertySet> pProperties, Consumer<BesPropertySet> pConsumer) {
		if (!pProperties.isEmpty()) {
			final Map<Long,BesPropertySet> map = new HashMap<>();
			final StringBuilder sb = new StringBuilder();
			sb.append("SELECT ");
			sb.append(FIELDS2);
			sb.append(" FROM ");
			sb.append(TABLE2);
			sb.append(" WHERE setId IN (");
			for (int i = 0;  i < pProperties.size();  i++) {
				final BesPropertySet bps = pProperties.get(i);
				final Long id = bps.getId().getIdObj();
				if (map.put(id, bps) != null) {
					throw new IllegalStateException("Duplicate property set id: " + id);
				}
				if (id > 0) {
					sb.append(", ");
				}
				sb.append(id);
			}
			sb.append(")");
			getJdbcHelper().query(pConn, sb.toString()).runWithRows((rs) -> {
				while(rs.next()) {
					final Long id = Objects.requireNonNull(rs.nextLongObj(), "BesProperties.id");
					final Long setId = Objects.requireNonNull(rs.nextLongObj(), "BesProperties.setId");
					final String key = Objects.requireNonNull(rs.nextStr(), "BesProperties.pKey");
					final String value = Objects.requireNonNull(rs.nextStr(), "BesProperties.pValue");
					final BesPropertySet bps = java.util.Objects.requireNonNull(map.get(setId), () -> "Id: " + id);
					final BesProperty bp = new BesProperty(BesProperty.Id.of(id), key, value);
					bps.setProperty(key, bp);
				}
			});
			pProperties.forEach(pConsumer);
			pProperties.clear();
		}
	}
	
	@Override
	public BesPropertySet insert(BesPropertySet pObject) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public void update(BesPropertySet pObject) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void delete(BesPropertySet pObject) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public BesPropertySet getPropertySetById(Id pId) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public BesPropertySet insert(Properties pProperties) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public void update(BesPropertySet pPropertySet, Properties pProperties) {
		// TODO Auto-generated method stub
		
	}
}
