package com.github.jochenw.bes.core.impl;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Properties;
import java.util.function.Consumer;

import com.github.jochenw.afw.core.function.Functions.FailableConsumer;
import com.github.jochenw.afw.core.jdbc.JdbcHelper;
import com.github.jochenw.afw.core.util.Exceptions;
import com.github.jochenw.afw.core.util.Holder;
import com.github.jochenw.afw.core.util.Objects;
import com.github.jochenw.bes.core.api.IBesModel.IBesPropertiesController;
import com.github.jochenw.bes.core.model.BesObject;
import com.github.jochenw.bes.core.model.BesProperty;
import com.github.jochenw.bes.core.model.BesPropertySet;
import com.github.jochenw.bes.core.model.BesPropertySet.Id;


public class DefaultBesPropertiesController extends AbstractBesObjectController<BesPropertySet.Id,BesPropertySet> implements IBesPropertiesController {
	private static final String TABLE = "PropertySets";
	private static final String FIELDS = "id, digest";
	private static final String TABLE2 = "Properties";
	private static final String FIELDS2 = "setId, pKey, pValue";

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
				if (i > 0) {
					sb.append(", ");
				}
				sb.append(id);
			}
			sb.append(")");
			getJdbcHelper().query(pConn, sb.toString()).runWithRows((rs) -> {
				while(rs.next()) {
					final Long setId = Objects.requireNonNull(rs.nextLongObj(), "BesProperties.setId");
					final String key = Objects.requireNonNull(rs.nextStr(), "BesProperties.pKey");
					final String value = Objects.requireNonNull(rs.nextStr(), "BesProperties.pValue");
					final BesPropertySet bps = map.get(setId);
					if (bps == null) {
						throw new NullPointerException("BesPropertySet not found for id: " + setId);
					}
					bps.setProperty(key, value);
				}
			});
			if (pConsumer != null) {
				pProperties.forEach(pConsumer);
			}
			pProperties.clear();
		}
	}

	protected BesPropertySet findExisting(BesPropertySet pBps) {
		final byte[] digest;
		if (pBps.getDigest() == null) {
			digest = BesPropertySet.getDigest(pBps);
			pBps.setDigest(digest);
		} else {
			digest = pBps.getDigest();
		}
		
		try (Connection conn = newConnection()) {
			final List<BesPropertySet> list = new ArrayList<>();
			final String sql = "SELECT id FROM " + TABLE + " WHERE digest=?";
			getJdbcHelper().query(conn, sql, digest).run((rs) -> {
				while (rs.next()) {
					final long idValue = rs.getLong(1);
					if (rs.wasNull()) {
						throw new NullPointerException("BesPropertySet.Id");
					}
					final BesPropertySet.Id id = BesPropertySet.Id.of(idValue);
					final BesPropertySet bps = new BesPropertySet(id);
					bps.setDigest(digest);
					list.add(bps);
				}
			});
			final Holder<BesPropertySet> result = Holder.of();
			fillPropertyValues(conn, list, (bps) -> {
				if (result.get() == null  &&  BesPropertySet.same(result.get(), pBps)) {
					result.set(bps);
				}
			});
			return result.get();
		} catch (SQLException se) {
			throw Exceptions.show(se);
		}
		
	}
	@Override
	public BesPropertySet insert(BesPropertySet pBps) {
		final BesPropertySet existing = findExisting(pBps);
		if (existing != null) {
			return existing;
		}
		
		final Long setIdObj = newId("PropertySetsSeq");
		final BesPropertySet.Id setId = BesPropertySet.Id.of(setIdObj);
		return insert(pBps, setId);
	}

	private BesPropertySet insert(BesPropertySet pBps, final BesPropertySet.Id pSetId) {
		final Long setIdObj = pSetId.getIdObj();
		final byte[] digest = BesPropertySet.getDigest(pBps);
		try (Connection conn = newConnection()) {
			final String sql = "INSERT INTO " + TABLE + " (" + FIELDS + ") VALUES (?, ?)";
			getJdbcHelper().query(conn, sql, pSetId.getIdObj(), digest).run();
			final String sql2 = "INSERT INTO " + TABLE2 + " (" + FIELDS2 + ") VALUES (?, ?, ?)";
			try (PreparedStatement stmt = conn.prepareStatement(sql2)) {
				pBps.forEach((k,v) -> {
					try {
						stmt.setLong(1, setIdObj.longValue());
						stmt.setString(2, k);
						stmt.setString(3,  v);
						stmt.executeUpdate();
					} catch (SQLException se) {
						throw Exceptions.show(se);
					}
				});
			}
			final BesPropertySet bps = new BesPropertySet(pSetId);
			bps.getModifiablePropertyMap().putAll(pBps.getPropertyMap());
			return bps;
		} catch (SQLException se) {
			throw Exceptions.show(se);
		}
	}

	@Override
	public BesPropertySet update(BesPropertySet pBps) {
		if (BesObject.Id.isNullId(pBps.getId())) {
			throw new IllegalStateException("A new property set must be inserted.");
		}
		final BesPropertySet existing = getPropertySetById(pBps.getId());
		if (BesPropertySet.same(existing, pBps)) {
			return pBps;
		} else {
			final BesPropertySet bps = new BesPropertySet(BesPropertySet.Id.noId());
			bps.getPropertyMap().putAll(pBps.getPropertyMap());
			return insert(pBps, bps.getId());
		}
	}

	@Override
	public void delete(BesPropertySet pBps) {
		if (BesObject.Id.isNullId(pBps.getId())) {
			throw new IllegalStateException("A new property set cannot be deleted.");
		}
		try (Connection conn = newConnection()) {
			final JdbcHelper jh = getJdbcHelper();
			jh.transaction(conn, (FailableConsumer<Connection, ?>) (cnn) -> {
				final Long id = pBps.getId().getIdObj();
				final String sql2 = "DELETE FROM" + TABLE2 + " WHERE setId=?";
				jh.query(cnn, sql2, id).run();
				final String sql = "DELETE FROM " + TABLE + " WHERE id=?";
				final int numberOfDeletedSets = jh.query(cnn, sql, id).run();
				if (numberOfDeletedSets == 0) {
					throw new IllegalStateException("Property set not found: " + id);
				}
			});
		} catch (SQLException se) {
			throw Exceptions.show(se);
		}
	}

	@Override
	public BesPropertySet getPropertySetById(Id pId) {
		final Id id = Objects.requireNonNull(pId, "Id");
		if (BesObject.Id.isNullId(id)) {
			throw new IllegalStateException("The property set to load must not have a null id.");
		}
		try (Connection conn = newConnection()) {
			final String sql = "SELECT id FROM " + TABLE + " WHERE id=?";
			return getJdbcHelper().query(conn, sql, id.getIdObj()).call((rs) -> {
				if (rs.next()) {
					final long idVal = rs.getLong(1);
					if (rs.wasNull()) {
						throw new NullPointerException("BesPropertySet.id");
					}
					final BesPropertySet bps = new BesPropertySet(BesPropertySet.Id.of(idVal));
					if (rs.next()) {
						throw new IllegalStateException("Query returned multipe rows for id=" + id.getId());
					}
					final List<BesPropertySet> list = new ArrayList<>(1);
					list.add(bps);
					fillPropertyValues(conn, list, null);
					return bps;
				} else {
					return null;
				}
			});
		} catch (SQLException se) {
			throw Exceptions.show(se);
		}
	}

	@Override
	public BesPropertySet insert(Properties pProperties) {
		final Long setIdObj = newId("PropertySetsSeq");
		final BesPropertySet.Id setId = BesPropertySet.Id.of(setIdObj);
		final BesPropertySet bps = asBesPropertySet(setId, pProperties, true);
		return insert(bps, setId);
	}

	private BesPropertySet asBesPropertySet(BesPropertySet.Id pSetId, Properties pProperties, boolean pNullIdValid) {
		final BesPropertySet bps = new BesPropertySet(pSetId);
		final Map<String, BesProperty> map = bps.getModifiablePropertyMap();
		pProperties.forEach((k,v) -> {
			final String key = (String) k;
			final String value = (String) v;
			map.put(key, BesProperty.of(pSetId, pNullIdValid, key, value));
		});
		return bps;
	}

	@Override
	public BesPropertySet update(BesPropertySet pPropertySet, Properties pProperties) {
		final BesPropertySet.Id id = pPropertySet.getId();
		return update(asBesPropertySet(id, pProperties, false));
	}
}
