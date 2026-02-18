package com.github.jochenw.bes.ui.vdn.util;

import java.util.Comparator;

public interface IComparatorGenerator {
	public <T> Comparator<T> getComparator(Class<T> pType, boolean pCaseSensitive);
}
