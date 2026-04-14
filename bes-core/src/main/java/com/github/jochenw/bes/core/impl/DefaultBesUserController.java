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
	private static final String TABLE = "BesUsers";
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
		final Long id = Objects.requireNonNull(pId, "Id").getIdObj();
		final String sql = "SELECT " + FIELDS + " FROM " + TABLE + " WHERE id=?";
		try (Connection conn = newConnection()) {
			return getJdbcHelper().query(conn, sql, pId.getIdObj()).call((rs) -> {
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
}
