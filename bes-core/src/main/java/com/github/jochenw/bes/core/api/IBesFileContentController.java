package com.github.jochenw.bes.core.api;

import java.io.InputStream;
import java.util.NoSuchElementException;

public interface IBesFileContentController {
	public BesFileContent insert(InputStream pContent);
	public void update(BesFileContent pContent) throws NoSuchElementException;
	public void delete(BesFileContent pContent) throws NoSuchElementException;
}
