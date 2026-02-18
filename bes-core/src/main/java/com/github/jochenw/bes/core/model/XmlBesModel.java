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
import com.github.jochenw.bes.core.model.BesUser.Id;

public class XmlBesModel implements IBesModel {
	private final Map<BesUser.Id,BesUser> usersById = new HashMap<>();
	private final Map<String,BesUser> usersByUserId = new HashMap<>();
	private final Map<String,BesUser> usersByEmail = new HashMap<>();
	private final List<IBesUserController.Listener> usersListener = new ArrayList<>();
	private final Map<BesJob.Id,BesJob> jobsById = new HashMap<>();
	private final Map<BesUser.Id,Map<String,BesJob>> jobsByOwnerAndName = new HashMap<>();
	private final List<IBesJobController.Listener> jobsListener = new ArrayList<>();
	private final StampedLock usersLock = new StampedLock();
	private final StampedLock jobLock = new StampedLock();
	private final StampedLock listenerLock = new StampedLock();
	private long maxUserId = -1;
	private long maxJobId = -1;

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

		@Override
		public BesUser getUserById(Id pId) {
			return Locks.call(usersLock,  false, () -> usersById.get(pId));
		}
	}

	private class XmlBesJobController implements IBesJobController {
		@Override
		public void add(Listener pListener) {
			Locks.run(listenerLock, true, () -> jobsListener.add(pListener));
		}

		@Override
		public void remove(Listener pListener) {
			Locks.run(listenerLock, true, () -> jobsListener.remove(pListener));
		}

		protected void notify(Consumer<Listener> pNotification) {
			Locks.run(listenerLock, false, () -> jobsListener.forEach(pNotification));
		}

		@Override
		public BesJob insert(BesJob pJob) {
			if (pJob.getId() != null) {
				throw new IllegalArgumentException("For an insert, the user's id must be null.");
			}
			final String name = Strings.requireNonEmpty(pJob.getName(), "A jobs name must not be null, or empty.");
			final BesUser.Id ownerId = Objects.requireNonNull(pJob.getOwner(), "A job owners id must not be null.");
			final BesUser owner = getUserController().getUserById(ownerId);
			if (owner == null) {
				throw new IllegalArgumentException("A jobs owner must be an existing users id,");
			}
			final BesJob newJob = Locks.call(jobLock, true, () -> {
				final BesJob.Id id = new BesJob.Id(++maxJobId);
				final BesJob bj = new BesJob(id);
				bj.setOwner(ownerId);
				bj.setName(name);
				if (jobsById.get(id) != null) {
					throw new IllegalStateException("Duplicate id: " + id.longValue());
				}
				final Map<String,BesJob> jobsByName = jobsByOwnerAndName.computeIfAbsent(ownerId, (oId) -> {
					return new HashMap<>();
				});
				if (jobsByName.get(name) != null) {
					throw new IllegalArgumentException("Duplicate job name for owner " + ownerId.longValue() + ": " + name);
				}
				jobsById.put(id, bj);
				jobsByName.put(name, bj);
				return bj;
			});
			notify((l) -> l.inserted(newJob));
			return newJob;
			
		}

		@Override
		public void update(BesJob pJob) {
			final BesJob.Id id = Objects.requireNonNull(pJob.getId(), "Updating a job requires a non-null id.");
			final String name = Strings.requireNonEmpty(pJob.getName(), "A jobs name must not be null, or empty.");
			final BesUser.Id ownerId = Objects.requireNonNull(pJob.getOwner(), "A jobs owner id must not be null.");
			Locks.run(jobLock, true, () -> {
				final BesJob bj = jobsById.get(id);
				if (bj == null) {
					throw new IllegalArgumentException("Unknown job id: " + id);
				}
				final String bjName = Strings.requireNonEmpty(bj.getName(), "A jobs name must not be null, or empty.");
				final BesUser.Id bjOwnerId = Objects.requireNonNull(bj.getOwner(), "A jobs owner id must not be null.");
				final Map<String,BesJob> jobsByName = jobsByOwnerAndName.computeIfAbsent(ownerId, (oId) -> {
					return new HashMap<>();
				});
				final BesJob bjOwnerName = jobsByName.get(name);
				final Map<String,BesJob> bjJobsByName = jobsByOwnerAndName.get(bjOwnerId);
				if (bjOwnerName != null  &&  bjOwnerName != bj) {
					throw new IllegalArgumentException("Duplicate job name for owner " + ownerId.longValue() + ": " + name);
				}
				bj.setName(name);
				bj.setOwner(ownerId);
				jobsById.put(id, pJob);
				bjJobsByName.remove(bjName);
				jobsByName.put(name, pJob);
			});
			notify((l) -> l.updated(pJob));
		}

		@Override
		public void delete(BesJob pJob) {
			final BesJob.Id id = Objects.requireNonNull(pJob.getId(), "Updating a job requires a non-null id.");
			Locks.run(jobLock, true, () -> {
				final BesJob bj = jobsById.get(id);
				if (bj == null) {
					throw new IllegalArgumentException("Unknown job id: " + id);
				}
				final String name = Strings.requireNonEmpty(bj.getName(), "A jobs name must not be null, or empty.");
				final BesUser.Id ownerId = Objects.requireNonNull(bj.getOwner(), "A jobs owner id must not be null.");
				final Map<String,BesJob> jobsByName = jobsByOwnerAndName.get(ownerId);
				jobsById.remove(id);
				if (jobsByName != null) {
					jobsByName.remove(name);
				}
				
			});
		}

		@Override
		public void readAll(Consumer<BesJob> pConsumer) {
			Locks.run(jobLock, false, () -> {
				jobsById.values().forEach(pConsumer);
			});
		}

		@Override
		public BesJob getJob(com.github.jochenw.bes.core.model.BesJob.Id pId) {
			final BesJob.Id id = Objects.requireNonNull(pId, "Id");
			return Locks.call(jobLock, false, () -> {
				return jobsById.get(id);
			});
		}

		@Override
		public BesJob getJob(Id pOwner, String pName) {
			final Id id = Objects.requireNonNull(pOwner, "Owner Id");
			final String name = Strings.requireNonEmpty(pName, "Job Name");
			final Map<String,BesJob> map = Locks.call(jobLock, false, () -> {
				return jobsByOwnerAndName.get(id);
			});
			if (map == null) {
				return null;
			}
			return map.get(name);
		}
	}

	private final XmlBesUserController userController = new XmlBesUserController();
	private final XmlBesJobController jobController = new XmlBesJobController();

	@Override
	public IBesUserController getUserController() {
		return userController;
	}

	@Override
	public IBesJobController getJobController() {
		return jobController;
	}
}
