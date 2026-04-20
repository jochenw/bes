package com.github.jochenw.bes.core.api;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.NoSuchElementException;
import java.util.Properties;
import java.util.function.Consumer;

import com.github.jochenw.bes.core.model.BesObject;
import com.github.jochenw.bes.core.model.BesPropertySet;
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
		O insert(O pObject);
		void update(O pObject);
		void delete(O pObject);
	}
	public interface IBesUserController extends IBesObjectController<BesUser.Id,BesUser>{
		BesUser getUserById(BesUser.Id pId);
		BesUser getUserByEmail(String pEmail);
		BesUser getUserByUserId(String pUserId);
	}
	public interface IBesPropertiesController extends IBesObjectController<BesPropertySet.Id,BesPropertySet>{
		BesPropertySet getPropertySetById(BesPropertySet.Id pId);
		BesPropertySet insert(Properties pProperties);
		void update(BesPropertySet pPropertySet, Properties pProperties);
	}

	public IBesUserController getUserController();
}
