package com.github.jochenw.bes.core.api;

import java.util.NoSuchElementException;
import java.util.function.Consumer;

import com.github.jochenw.afw.core.util.Objects;


public interface IBesModel {
	public static class Id {
		private final Long id;
		protected Id(Long pId) {
			id = pId;
		}

		public long getId() { return id.longValue(); }
		public Long getIdObj() { return id; }
	}
	public static class BesBean<T extends Id> {
		private final T id;

		protected BesBean(T pId) {
			id = pId;
		}

		public T getId() { return id; }
		public String getIdStr() { return String.valueOf(id); }
	}
	public interface Listener {
		public default void userInserted(BesUser pUser) {}
		public default void userUpdated(BesUser pUser) {}
		public default void userDeleted(BesUser pUser) {}
		public default void jobInserted(BesJob pUser) {}
		public default void jobUpdated(BesJob pJob) {}
		public default void jobDeleted(BesJob pJob) {}
		public default void executionInserted(BesExecution pExecution) {}
		public default void executionUpdated(BesExecution pExecution) {}
		public default void executionDeleted(BesExecution pExecution) {}
	}

	public void getUsers(Consumer<BesUser> pConsumer);
	public BesUser getUserById(long pId);
	public BesUser getUserByUserId(String pUserId);
	public default BesUser getUserById(BesUser.Id pId) { return getUserById(pId.getId()); }
	public default BesUser getUserByIdStr(String pIdStr) {
		final String idStr = Objects.requireNonNull(pIdStr, "Id String");
		final long id;
		try {
			id = Long.valueOf(idStr);
		} catch (NumberFormatException nfe) {
			throw new IllegalArgumentException("Invalid id: Expected long integer, got " + idStr);
		}
		return getUserById(id);
	}
	public BesUser getUserByEmail(String pEmail);

	public IBesUsersController getUsersController();
	public void getJobs(Consumer<BesJob> pConsumer);
	public BesJob getJobById(long pId);
	public default BesJob getJobById(BesJob.Id pId) { return getJobById(pId.getId()); }

	public void getExecutions(Consumer<BesExecution> pConsumer);
	public BesExecution getExecution(long pId);
	public default BesExecution getExecution(BesJob.Id pId) { return getExecution(pId.getId()); }


	public BesFileContent getContentById(long pId);
	public default BesFileContent getContentById(BesFileContent.Id pId) { return getContentById(pId.getId()); }

	public void add(Listener pListener);
	public void remove(Listener pListener) throws NoSuchElementException;
}
