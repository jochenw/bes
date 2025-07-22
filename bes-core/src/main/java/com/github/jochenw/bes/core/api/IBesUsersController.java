package com.github.jochenw.bes.core.api;

import java.util.NoSuchElementException;
import java.util.function.Consumer;

import com.github.jochenw.afw.core.util.Objects;

public interface IBesUsersController {
	public BesUser insert(BesUser pUser);
	public void update(BesUser pUser) throws NoSuchElementException;
	public void delete(BesUser pUser) throws NoSuchElementException;
}
