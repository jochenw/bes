package com.github.jochenw.bes.core.api;

import java.util.NoSuchElementException;
import java.util.function.Consumer;

public interface IBesExecutionsController {
	public BesExecution insert(BesExecution pExecution);
	public void update(BesExecution pExecution) throws NoSuchElementException;
	public void delete(BesExecution pExecution) throws NoSuchElementException;
}
