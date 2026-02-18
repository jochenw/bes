package com.github.jochenw.bes.ui.vdn.util;

import java.math.BigDecimal;
import java.math.BigInteger;
import java.util.Comparator;

import com.github.jochenw.afw.core.util.Generics;
import com.github.jochenw.afw.core.util.Objects;

public class DefaultComparatorGenerator implements IComparatorGenerator {
	public static final DefaultComparatorGenerator THE_INSTANCE = new DefaultComparatorGenerator();

	protected DefaultComparatorGenerator() {
	}

	@Override
	public <T> Comparator<T> getComparator(Class<T> pType, boolean pCaseSensitive) {
		final Class<T> type = Objects.requireNonNull(pType,"Type");
		final Comparator<Object> comparator;
		if (type == String.class) {
			if (pCaseSensitive) {
				comparator = (o1, o2) -> ((String) o1).compareTo((String) o2);
			} else {
				comparator = (o1, o2) -> ((String) o1).compareToIgnoreCase((String) o2);
			}
		} else if (type == Long.class) {
			comparator = (o1, o2) -> ((Long) o1).compareTo((Long) o2);
		} else if (type == Integer.class) {
			comparator = (o1, o2) -> ((Integer) o1).compareTo((Integer) o2);
		} else if (type == Short.class) {
			comparator = (o1, o2) -> ((Short) o1).compareTo((Short) o2);
		} else if (type == Byte.class) {
			comparator = (o1, o2) -> ((Byte) o1).compareTo((Byte) o2);
		} else if (type == Double.class) {
			comparator = (o1, o2) -> ((Double) o1).compareTo((Double) o2);
		} else if (type == Float.class) {
			comparator = (o1, o2) -> ((Float) o1).compareTo((Float) o2);
		} else if (type == BigDecimal.class) {
			comparator = (o1, o2) -> ((BigDecimal) o1).compareTo((BigDecimal) o2);
		} else if (type == BigInteger.class) {
			comparator = (o1, o2) -> ((BigInteger) o1).compareTo((BigInteger) o2);
		} else {
			comparator = null;
		}
		final Comparator<T> result = Generics.cast(comparator);
		return result;
	}

}
