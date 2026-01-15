package com.github.jochenw.bes.core.model;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.locks.StampedLock;
import java.util.function.Consumer;

import com.github.jochenw.afw.core.util.Locks;
import com.github.jochenw.afw.core.util.Objects;
import com.github.jochenw.afw.core.util.Strings;
import com.github.jochenw.bes.core.api.IBesModel;

public class XmlBesModel implements IBesModel {
	private final Map<BesUser.Id,BesUser> usersById = new HashMap<>();
	private final Map<String,BesUser> usersByUserId = new HashMap<>();
	private final Map<String,BesUser> usersByEmail = new HashMap<>();
	private final List<IBesUserController.Listener> usersListener = new ArrayList<>();
	private final StampedLock usersLock = new StampedLock();
	private final StampedLock listenerLock = new StampedLock();
	private long maxUserId = -1;

	private class XmlBesUserController implements IBesUserController {
		@Override
		public void add(Listener pListener) {
			Locks.run(listenerLock, true, () -> usersListener.add(pListener));
		}

		@Override
		public void remove(Listener pListener) {
			Locks.run(listenerLock, true, () -> usersListener.remove(pListener));
		}

		protected void notify(Consumer<Listener> pNotification) {
			Locks.run(listenerLock, false, () -> usersListener.forEach(pNotification));
					
		}
		@Override
		public BesUser insert(BesUser pUser) {
			if (pUser.getId() != null) {
				throw new IllegalArgumentException("For an insert, the user's id must be null.");
			}
			final String email = Strings.requireNonEmpty(pUser.getEmail(), "A users email must not be null, or empty.");
			final String userId = Strings.requireNonEmpty(pUser.getUserId(), "A users user id must not be null, or empty.");
			Strings.requireNonEmpty(pUser.getName(), "A users name must not be empty.");
			final BesUser newUser = Locks.call(usersLock, true, () -> {
				final BesUser.Id id = new BesUser.Id(++maxUserId);
				final BesUser bu = new BesUser(id);
				bu.setUserId(pUser.getUserId());
				bu.setEmail(pUser.getEmail());
				bu.setName(pUser.getName());
				if (usersById.get(id) != null) {
					throw new IllegalStateException("Duplicate id: " + id.longValue());
				}
				if (usersByUserId.get(userId) != null) {
					throw new IllegalArgumentException("Duplicate user id: " + bu.getUserId());
				}
				if (usersByEmail.get(email) != null) {
					throw new IllegalArgumentException("Duplicate email address: " + bu.getEmail());
				}
				usersById.put(id, bu);
				usersByUserId.put(bu.getUserId(), bu);
				usersByEmail.put(bu.getEmail(), bu);
				return bu;
			});
			notify((l) -> l.inserted(newUser));
			return newUser;
		}

		@Override
		public void update(BesUser pUser) {
			final BesUser.Id id = Objects.requireNonNull(pUser.getId(), "Updating a user requires a non-null id.");
			final String email = Strings.requireNonEmpty(pUser.getEmail(), "A users email must not be null, or empty.");
			final String userId = Strings.requireNonEmpty(pUser.getUserId(), "A users user id must not be null, or empty.");
			Strings.requireNonEmpty(pUser.getName(), "A users name must not be empty.");
			Locks.run(usersLock, true, () -> {
				final BesUser bu = usersById.get(id);
				if (bu == null) {
					throw new IllegalArgumentException("Unknown user id: " + id);
				}
				final BesUser buEmail = usersByEmail.get(pUser.getEmail());
				if (buEmail != null  &&  buEmail != bu) {
					throw new IllegalArgumentException("Duplicate email address: " + email);
				}
				final BesUser buUserId = usersByUserId.get(userId);
				if (buUserId != null  &&  buUserId != bu) {
					throw new IllegalArgumentException("Duplicate user id: " + userId);
				}
				usersById.put(id, pUser);
				usersByEmail.remove(bu.getEmail());
				usersByEmail.put(email, pUser);
				usersByUserId.remove(bu.getUserId());
				usersByUserId.put(userId, pUser);
			});
			notify((l) -> l.updated(pUser));
		}

		@Override
		public void delete(BesUser pUser) {
			final BesUser.Id id = Objects.requireNonNull(pUser.getId(), "Updating a user requires a non-null id.");
			final BesUser deletedUser = Locks.call(usersLock, true, () -> {
				final BesUser bu = usersById.get(id);
				if (bu == null) {
					throw new IllegalArgumentException("Unknown user id: " + id);
				}
				if (usersByEmail.get(bu.getEmail()) == null) {
					throw new IllegalStateException("Unknown email address: " + bu.getEmail());
				}
				if (usersByUserId.get(bu.getUserId()) == null) {
					throw new IllegalStateException("Unknown user id: " + bu.getEmail());
				}
				usersById.remove(id);
				usersByEmail.remove(bu.getEmail());
				usersByUserId.remove(bu.getUserId());
				return bu;
			});
			notify((l) -> l.deleted(deletedUser));
		}

		@Override
		public void readAll(Consumer<BesUser> pConsumer) {
			Locks.run(usersLock, false, () -> usersById.forEach((id, bu) -> pConsumer.accept(bu)));
		}

		@Override
		public BesUser getUserByUserId(String pUserId) {
			return Locks.call(usersLock, false, () -> usersByUserId.get(pUserId));
		}

		@Override
		public BesUser getUserByEmail(String pEmail) {
			return Locks.call(usersLock, false, () -> usersByEmail.get(pEmail));
		}
	}

	private final XmlBesUserController userController = new XmlBesUserController();

	@Override
	public IBesUserController getUserController() {
		return userController;
	}
}
