package com.github.jochenw.bes.core.api;

import java.util.NoSuchElementException;
import java.util.function.Consumer;

public interface IBesJobsController {
	public BesJob insert(BesJob pJob);
	public void update(BesJob pJob) throws NoSuchElementException;
	public void delete(BesJob pJob) throws NoSuchElementException;
}
