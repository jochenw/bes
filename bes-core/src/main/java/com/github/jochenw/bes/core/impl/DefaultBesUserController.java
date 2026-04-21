package com.github.jochenw.bes.core.impl;

import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.function.Consumer;

import com.github.jochenw.afw.core.util.Exceptions;
import com.github.jochenw.afw.core.util.Objects;
import com.github.jochenw.bes.core.api.IBesModel.IBesUserController;
import com.github.jochenw.bes.core.model.BesUser;
import com.github.jochenw.bes.core.model.BesUser.Id;


public class DefaultBesUserController extends AbstractBesObjectController<BesUser.Id,BesUser> implements IBesUserController {
	private static final String TABLE = "Users";
	private static final String FIELDS = "id, userId, email, usrName";

	@Override
	public void readAll(Consumer<BesUser> pConsumer) {
		try (Connection conn = newConnection()) {
			final String sql = "SELECT " + FIELDS + " FROM " + TABLE;
			getJdbcHelper().query(conn, sql).run((rs) -> {
				while (rs.next()) {
					pConsumer.accept(newBesUser(rs));
				}
			});
		} catch (Exception e) {
			throw Exceptions.show(e);
		}
	}

	private BesUser newBesUser(ResultSet pRs) throws SQLException {
		final long id = pRs.getLong(1);
		if (pRs.wasNull()) {
			throw new NullPointerException("BesUsers.id is null");
		}
		final String userId = Objects.requireNonNull(pRs.getString(2), "BesUsers.userId is null");
		final String email = Objects.requireNonNull(pRs.getString(3), "BesUsers.email is null");
		final String name = Objects.requireNonNull(pRs.getString(4), "BesUsers.name is null");
		final BesUser bu = BesUser.of(BesUser.Id.of(id), userId, email, name);
		return bu;
	}

	@Override
	public BesUser getUserById(Id pId) {
		try (Connection conn = newConnection()) {
			return getUserById(conn, pId, false);
		} catch (Exception e) {
			throw Exceptions.show(e);
		}
	}

	protected BesUser getUserById(Connection pConnection, Id pId, boolean pForUpdate) {
		final Long id = Objects.requireNonNull(pId, "Id").getIdObj();
		final String sql = "SELECT " + FIELDS + " FROM " + TABLE + " WHERE id=?";
		try {
			return getJdbcHelper().query(pConnection, sql, pId.getIdObj()).call((rs) -> {
				if (rs.next()) {
					final BesUser bu = newBesUser(rs);
					if (rs.next()) {
						throw new IllegalStateException("Multiple users found for id: " + id);
					}
					return bu;
				} else {
					return null;
				}
			});
		} catch (Exception e) {
			throw Exceptions.show(e);
		}
	}
	
	@Override
	public BesUser getUserByEmail(String pEmail) {
		final String email = Objects.requireNonNull(pEmail, "Email");
		final String sql = "SELECT " + FIELDS + " FROM " + TABLE + " WHERE email=?";
		try (Connection conn = newConnection()) {
			return getJdbcHelper().query(conn, sql, email).call((rs) -> {
				if (rs.next()) {
					final BesUser bu = newBesUser(rs);
					if (rs.next()) {
						throw new IllegalStateException("Multiple users found for email: " + email);
					}
					return bu;
				} else {
					return null;
				}
			});
		} catch (Exception e) {
			throw Exceptions.show(e);
		}
	}

	@Override
	public BesUser getUserByUserId(String pUserId) {
		final String userId = Objects.requireNonNull(pUserId, "UserId");
		final String sql = "SELECT " + FIELDS + " FROM " + TABLE + " WHERE userId=?";
		try (Connection conn = newConnection()) {
			return getJdbcHelper().query(conn, sql, userId).call((rs) -> {
				if (rs.next()) {
					final BesUser bu = newBesUser(rs);
					if (rs.next()) {
						throw new IllegalStateException("Multiple users found for userId: " + userId);
					}
					return bu;
				} else {
					return null;
				}
			});
		} catch (Exception e) {
			throw Exceptions.show(e);
		}
	}

	@Override
	public BesUser insert(BesUser pObject) {
		final BesUser.Id id = Objects.requireNonNull(pObject, "Object").getId();
		if (isNullId(id)) {
			final String sql = "INSERT INTO " + TABLE +
					" (id, userId, email, usrName) VALUES (?, ?, ?, ?)";
			final BesUser result;
			final Id newId = BesUser.Id.of(newId(TABLE + "Seq"));
			try (Connection conn = newConnection()) {
				result = BesUser.of(newId, pObject);
				getJdbcHelper().query(conn, sql,
						              newId.getIdObj(),
						              result.getUserId(), result.getEmail(), result.getName()).run();
			} catch (Exception e) {
				throw Exceptions.show(e);
			}
			notifyListeners((l) -> l.inserted(result));
			return result;
		} else {
			throw new IllegalStateException("The inserted object must have a null id.");
		}
	}

	@Override
	public BesUser update(BesUser pObject) {
		final BesUser user = Objects.requireNonNull(pObject, "Object");
		if (isNullId(user.getId())) {
			throw new IllegalStateException("The updated object must have a non-null id.");
		}
		final BesUser oldUser;
		try (Connection conn = newConnection()) {
			oldUser = getUserById(conn, user.getId(), true);
			if (oldUser == null) {
				throw new IllegalStateException("User id not found for update: " + user.getId().getId());
			}
			final String sql = "UPDATE " + TABLE +
					" SET userId=?, email=?, usrName=? WHERE id=?";
			getJdbcHelper().query(conn, sql, user.getUserId(),
					              user.getEmail(), user.getName(), user.getId().getIdObj())
						   .run();
		} catch (Exception e) {
			throw Exceptions.show(e);
		}
		notifyListeners((l) -> l.updated(oldUser, user));
		return user;
	}

	@Override
	public void delete(BesUser pObject) {
		final BesUser user = Objects.requireNonNull(pObject, "Object");
		if (isNullId(user.getId())) {
			throw new IllegalStateException("The updated object must have a non-null id.");
		}
		try (Connection conn = newConnection()) {
			final String sql = "DELETE FROM " + TABLE + " WHERE id=?";
			final int count = getJdbcHelper().query(conn, sql, user.getId().getIdObj()).run();
			if (count == 0) {
				throw new IllegalStateException("User id not found for delete: " + user.getId().getId());
			}
		} catch (Exception e) {
			throw Exceptions.show(e);
		}
		notifyListeners((l) -> l.deleted(user));
	}
}
