package com.github.jochenw.bes.core.api;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.NoSuchElementException;
import java.util.function.Consumer;

import com.github.jochenw.bes.core.model.BesObject;
import com.github.jochenw.bes.core.model.BesUser;

public interface IBesModel {
	public interface IListener<ID extends BesObject.Id,O extends BesObject<ID>> {
		void inserted(O pObject);
		void updated(O pOld, O pNew);
		void deleted(O pOld);
	}
	public interface IBesObjectController<ID extends BesObject.Id,O extends BesObject<ID>> {
		void add(IListener<ID,O> pListener);
		void remove(IListener<ID,O> pListener);

		void readAll(Consumer<O> pConsumer);
		default List<O> getAll() {
			final List<O> list = new ArrayList<>();
			readAll(list::add);
			return list;
		}
		default List<O> getAll(Comparator<O> pComparator) {
			final List<O> list = new ArrayList<>();
			readAll(list::add);
			list.sort(pComparator);
			return list;
		}
	}
	public interface IBesUserController extends IBesObjectController<BesUser.Id,BesUser>{
		BesUser getUserById(BesUser.Id pId);
		BesUser getUserByEmail(String pEmail);
		BesUser getUserByUserId(String pUserId);
	}

	public IBesUserController getUserController();
}
