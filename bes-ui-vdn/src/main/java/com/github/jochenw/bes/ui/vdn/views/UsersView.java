package com.github.jochenw.bes.ui.vdn.views;

import java.util.function.Consumer;

import org.apache.logging.log4j.util.Strings;

import com.github.jochenw.afw.core.function.Functions;
import com.github.jochenw.afw.di.api.IComponentFactory;
import com.github.jochenw.afw.vdn.grid.GridContainer;
import com.github.jochenw.afw.vdn.grid.Persistor;
import com.github.jochenw.bes.core.api.BesUser;
import com.github.jochenw.bes.core.api.BesUser.Id;
import com.github.jochenw.bes.core.api.IBesModel;
import com.github.jochenw.bes.ui.vdn.MainView;

public class UsersView extends AbstractView {
	private static final long serialVersionUID = -4570583137846079127L;

	public UsersView(MainView pMainView) {
		super(pMainView);
	}

	public static class UserBean {
		private final BesUser.Id id;
		private String userId, name, email;

		private UserBean(Id id, String userId, String name, String email) {
			this.id = id;
			this.userId = userId;
			this.name = name;
			this.email = email;
		}

		public BesUser.Id getId() { return id; }
		public String getUserId() { return userId; }
		public String getName() { return name; }
		public String getEmail() { return email; }
		public void setUserId(String pUserId) { userId = pUserId; }
		public void setName(String pName) { name = pName; }
		public void setEmail(String pEmail) { email = pEmail; }


		public static UserBean of(BesUser pUser) {
			if (pUser == null) {
				return new UserBean(null, null, null, null);
			} else {
				return new UserBean(pUser.getId(), pUser.getUserId(), pUser.getName(), pUser.getEmail());
			}
		}

	}

	public static class UsersGrid extends GridContainer<UserBean> {
		private static final long serialVersionUID = -8437279174816095427L;

	}

	private IBesModel model;

	@Override
	protected void initContent() {
		final IComponentFactory cf = getCf();
		model = cf.requireInstance(IBesModel.class);
		final Persistor.Builder<UserBean,String> persBuilder = Persistor.builder();
		final Persistor<UserBean,?> persistor = persBuilder
				.idMapper((ub) -> {
					final BesUser.Id id = ub.getId();
					return id == null ? null : id.getIdObj().toString();
				})
				.reader((cons) -> {
					final Consumer<BesUser> besUserConsumer = (bu) -> {
						Functions.accept(cons, UserBean.of(bu));
					};
					model.getUsers(besUserConsumer);
				})
				.insert((ub) -> {
					final BesUser bu = BesUser.of(ub.getUserId(), ub.getName(), ub.getEmail());
					final BesUser bu2 = model.getUsersController().insert(bu);
					return UserBean.of(bu2);
				})
				.update((ub) -> {
					final BesUser bu = BesUser.of(ub.getId().getId(), ub.getUserId(), ub.getName(), ub.getEmail());
					model.getUsersController().update(bu);
				})
				.delete((ub) -> {
					final BesUser bu = BesUser.of(ub.getId().getId(), ub.getUserId(), ub.getName(), ub.getEmail());
					model.getUsersController().delete(bu);
				})
				.supplier(() -> UserBean.of(null))
				.get();
		final UsersGrid ug = GridContainer.builder(getCf(), UserBean.class)
				.persistor(persistor)
				.stringColumn("userId", UserBean::getUserId, "User Id")
				    .setter(UserBean::setUserId)
				    .validator((col, ub, userId) -> {
				    	if (Strings.isEmpty(userId)) {
				    		return "Empty user id";
				    	} else {
				    		if (ub.getId() != null  &&  userId.equals(ub.getUserId())) {
				    			return null; // User id hasn't changed, so okay.
				    		} else {
				    			final BesUser bu = model.getUserByUserId(userId);
				    			if (bu == null) {
				    				return null;
				    			} else {
				    				return "Duplicate user id";
				    			}
				    		}
				    	}
				    }).end()
					.stringColumn("name", UserBean::getName, "Name")
				        .setter(UserBean::setName)
				        .validator((col,ub,name) -> {
				        	if (Strings.isEmpty(name)) {
				        		return "Empty name";
				        	} else {
				        		return null;
				        	}
				        })
				        .end()
						.stringColumn("email", UserBean::getEmail, "Email")
					    .setter(UserBean::setEmail)
					    .validator((col,ub,email) -> {
					    	if (Strings.isEmpty(email)) {
					    		return "Empty email address";
					    	} else {
					    		if (ub.getId() != null  &&  email.equals(ub.getEmail())) {
					    			return null; // Email address hasn't changed, so okay.
					    		} else {
					    			final BesUser bu = model.getUserByEmail(email);
					    			if (bu == null) {
					    				return null;
					    			} else {
					    				return "Duplicate email address";
					    			}
					    		}
					    	}
					    }).end()
				    .build(UsersGrid.class);
		super.add(ug);
	}

}
