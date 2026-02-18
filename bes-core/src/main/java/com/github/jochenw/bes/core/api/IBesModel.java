package com.github.jochenw.bes.core.api;

import java.util.function.Consumer;

import com.github.jochenw.bes.core.model.BesBean;
import com.github.jochenw.bes.core.model.BesJob;
import com.github.jochenw.bes.core.model.BesUser;

public interface IBesModel {
	public interface IBesBeanController<B extends BesBean<?>, L extends IBesBeanController.Listener> {
		public interface Listener {}
		public void add(L pListener);
		public void remove(L pListener);
	}
	public interface IBesUserController extends IBesBeanController<BesUser, IBesUserController.Listener> {
		public interface Listener extends IBesBeanController.Listener {
			default void inserted(BesUser pUser) {}
			default void updated(BesUser pUser) {}
			default void deleted(BesUser pUser) {}
		}
		public BesUser insert(BesUser pUser);
		public void update(BesUser pUser);
		public void delete(BesUser pUser);
		public void readAll(Consumer<BesUser> pConsumer);
		public BesUser getUserById(BesUser.Id pId);
		public BesUser getUserByUserId(String pUserId);
		public BesUser getUserByEmail(String pEmail);
	}
	public interface IBesJobController extends IBesBeanController<BesJob, IBesJobController.Listener> {
		public interface Listener extends IBesBeanController.Listener {
			default void inserted(BesJob pJob) {}
			default void updated(BesJob pJob) {}
			default void deleted(BesJob pJob) {}
		}

		public BesJob insert(BesJob pJob);
		public void update(BesJob pJob);
		public void delete(BesJob pJob);
		public void readAll(Consumer<BesJob> pConsumer);
		public BesJob getJob(BesJob.Id pId);
		public BesJob getJob(BesUser.Id pOwner, String pName);
	}

	public IBesUserController getUserController();
	public IBesJobController getJobController();
}
