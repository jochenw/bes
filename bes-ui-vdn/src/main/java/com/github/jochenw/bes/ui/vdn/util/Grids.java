package com.github.jochenw.bes.ui.vdn.util;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.NoSuchElementException;
import java.util.function.BiFunction;
import java.util.function.Consumer;
import java.util.function.Function;
import java.util.function.Predicate;

import com.github.jochenw.afw.core.function.Functions.FailableConsumer;
import com.github.jochenw.afw.core.util.Exceptions;
import com.github.jochenw.afw.core.util.Generics;
import com.github.jochenw.afw.core.util.MutableInteger;
import com.github.jochenw.afw.core.util.NotImplementedException;
import com.github.jochenw.afw.core.util.Objects;
import com.github.jochenw.afw.core.vdn.Filters;
import com.vaadin.flow.component.grid.Grid;
import com.vaadin.flow.component.html.H2;
import com.vaadin.flow.component.html.H3;
import com.vaadin.flow.component.orderedlayout.HorizontalLayout;
import com.vaadin.flow.component.orderedlayout.VerticalLayout;
import com.vaadin.flow.component.textfield.TextField;
import com.vaadin.flow.data.provider.CallbackDataProvider.CountCallback;
import com.vaadin.flow.data.provider.CallbackDataProvider.FetchCallback;
import com.vaadin.flow.data.provider.DataProvider;
import com.vaadin.flow.data.provider.QuerySortOrder;
import com.vaadin.flow.data.provider.SortDirection;
import com.vaadin.flow.function.ValueProvider;


public class Grids {
	public interface IPredicateBuilder<T> {
		Predicate<T> getPredicate(String pFilter);
		String getPredicateDescription(String pFieldName, String pFilter);
	}
	public static class GridContainer<B> extends VerticalLayout {
		private static final long serialVersionUID = -7848898743756856976L;
		private final HorizontalLayout filterBar;
		private final HorizontalLayout statusBar;
		private final Grid<B> grid;
		private final HorizontalLayout buttonsBar;
		public GridContainer(HorizontalLayout pFilterBar, HorizontalLayout pStatusBar, Grid<B> pGrid,
				HorizontalLayout pButtonsBar) {
			filterBar = pFilterBar;
			statusBar = pStatusBar;
			grid = pGrid;
			buttonsBar = pButtonsBar;
			if (filterBar != null) {
				add(filterBar);
			}
			if (statusBar != null) {
				add(statusBar);
			}
			if (grid != null) {
				add(grid);
			}
			if (buttonsBar != null) {
				add(buttonsBar);
			}
		}
		public HorizontalLayout getFilterBar() { return filterBar; }
		public HorizontalLayout getStatusBar() { return statusBar; }
		public Grid<B> getGrid() { return grid; }
		public HorizontalLayout getButtonsBar() { return buttonsBar; }
	}

	public static class Column<B,T> {
		private final GridBuilder<B> builder;
		private final String columnId;
		private final String columnName;
		private final Class<T> columnType;
		private final Function<B,T> getter;
		private boolean caseSensitive, nullSafe, sortable;
		private Comparator<T> comparator;
		private IPredicateBuilder<T> predicateBuilder;

		public Column(GridBuilder<B> pBuilder, String pColumnId, String pColumnName, Class<T> pColumnType,
				Function<B, T> pGetter) {
			builder = pBuilder;
			columnId = pColumnId;
			columnName = pColumnName;
			columnType = pColumnType;
			getter = pGetter;
		}

		public boolean isCaseSensitive() {
			if (String.class == columnType) {
				return caseSensitive;
			} else {
				return false;
			}
		}

		public boolean isNullSafe() { return nullSafe; }
		public boolean isSortable() { return sortable; }

		public String getColumnId() { return columnId; }
		public String getColumnName() { return columnName; }
		public Class<T> getColumnType() { return columnType; }
		public Function<B,T> getGetter() { return getter; }
		public IPredicateBuilder<T> getPredicateBuilder() { return predicateBuilder; }

		public Comparator<T> getComparator() {
			return comparator;
		}

		public Column<B,T> caseSensitive() {
			if (getComparator() == null) {
				if (String.class == columnType) {
					caseSensitive = true;
				} else {
					throw new IllegalStateException("A column can only be caseSensitive, if the column type is String, not "
							+ columnType.getName());
				}
			} else {
				throw new IllegalStateException("A calumn can only be caseSensitive, if the column comparator is null.");
			}
			return this;
		}

		public Column<B,T> nullSafe() {
			nullSafe = true;
			return this;
		}

		public Column<B,T> comparator(Comparator<T> pComparator) {
			comparator = pComparator;
			if (pComparator != null) {
				sortable(true);
			}
			return this;
		}

		public Column<B,T> predicateBuilder(IPredicateBuilder<T> pPredicateBuilder) {
			predicateBuilder = pPredicateBuilder;
			return this;
		}

		public Column<B,T> sortable(boolean pSortable) {
			sortable = pSortable;
			return this;
		}

		public GridBuilder<B> end() {
			return builder;
		}
	}
	public static class GridBuilder<B> {
		private final Class<B> beanType;
		private final List<Column<B,Object>> columnList = new ArrayList<>();
		private final Map<String,Column<B,Object>> columnMap = new HashMap<>();
		private FailableConsumer<Consumer<B>,?> reader;
		private IComparatorGenerator comparatorGenerator = DefaultComparatorGenerator.THE_INSTANCE;
		private Function<GridBuilder<B>,HorizontalLayout> filterBarBuilder;
		private BiFunction<GridBuilder<B>,HorizontalLayout,HorizontalLayout> statusBarBuilder;
		private Function<GridBuilder<B>,Grid<B>> gridBuilder;
		private BiFunction<GridBuilder<B>,Grid<B>,HorizontalLayout> buttonBarBuilder;

		public GridBuilder(Class<B> pBeanType) {
			beanType = pBeanType;
		}

		public Class<B> getBeanType() {
			return beanType;
		}

		public BiFunction<GridBuilder<B>,Grid<B>,HorizontalLayout> getButtonBarBuilder() {
			return buttonBarBuilder;
		}
	
		public List<Column<B,?>> getColumns() {
			final List<Column<B,?>> result = Generics.cast(columnList);
			return result;
		}
	
		public IComparatorGenerator getComparatorGenerator() {
			return comparatorGenerator;
		}
	
		public Function<GridBuilder<B>,HorizontalLayout> getFilterBarBuilder() {
			return filterBarBuilder;
		}

		public Function<GridBuilder<B>,Grid<B>> getGridBuilder() {
			return gridBuilder;
		}
	
		public BiFunction<GridBuilder<B>,HorizontalLayout,HorizontalLayout> getStatusBarBuilder() {
			return statusBarBuilder;
		}
		
		public FailableConsumer<Consumer<B>,?> getReader() {
			return reader;
		}
	
		public GridBuilder<B> buttonBarBuilder(BiFunction<GridBuilder<B>,Grid<B>,HorizontalLayout> pButtonBarBuilder) {
			buttonBarBuilder = pButtonBarBuilder;
			return this;
		}
	
		public GridBuilder<B> comparatorGenerator(IComparatorGenerator pComparatorGenerator) {
			comparatorGenerator = Objects.requireNonNull(pComparatorGenerator, "ComparatorGenerator");
			return this;
		}

		public GridBuilder<B> filterBarBuilder(Function<GridBuilder<B>,HorizontalLayout> pFilterBarBuilder) {
			filterBarBuilder = pFilterBarBuilder;
			return this;
		}

		public GridBuilder<B> gridBuilder(Function<GridBuilder<B>,Grid<B>> pGridBuilder) {
			gridBuilder = pGridBuilder;
			return this;
		}
	
		public GridBuilder<B> statusBarBuilder(BiFunction<GridBuilder<B>,HorizontalLayout,HorizontalLayout> pStatusBarBuilder) {
			statusBarBuilder = pStatusBarBuilder;
			return this;
		}
	
		public GridBuilder<B> reader(FailableConsumer<Consumer<B>,?> pReader) {
			reader = pReader;
			return this;
		}

		public GridContainer<B> build() {
			final HorizontalLayout filterBar;
			final Function<GridBuilder<B>,HorizontalLayout> filterBarBldr = getFilterBarBuilder();
			if (filterBarBldr == null) {
				filterBar = newFilterBar();
			} else {
				filterBar = filterBarBldr.apply(this);
			}
			final HorizontalLayout statusBar;
			if (statusBarBuilder == null) {
				statusBar = newStatusBar(filterBar);
			} else {
				statusBar = statusBarBuilder.apply(this, filterBar);
			}
		}

		protected HorizontalLayout newFilterBar(Runnable pFiltersChangedHandler) {
			final HorizontalLayout hl = new HorizontalLayout();
			final List<Column<B,Object>> colList = Generics.cast(getColumns());
			final H3 h3 = new H3("Filters");
			hl.add(h3);
			for (Column<B,Object> col : colList) {
				final IPredicateBuilder predicateBuilder = col.getPredicateBuilder();
				if (predicateBuilder != null) {
					final TextField tf = new TextField();
					tf.setLabel(col.getColumnName());
					tf.addValueChangeListener((e) -> { pFiltersChangedHandler.run(); }
					hl.add(tf);
				}
			}
		}
	
		protected Grid<B> newGrid() {
			final Grid<B> grid = new Grid<B>();
			for (Column<B,Object> c : columnList) {
				final ValueProvider<B,?> valueProvider = (B b) -> c.getter.apply(b);
				final com.vaadin.flow.component.grid.Grid.Column<?> gridCol = grid.addColumn(valueProvider);
				if (c.columnName == null  ||  c.columnName.length() == 0) {
					gridCol.setVisible(false);
				} else {
					gridCol.setHeader(c.columnName).setVisible(true);
				}
				gridCol.setId(c.columnId);
				gridCol.setSortable(c.isSortable());
			}
			grid.setDataProvider(newDataProvider());
			return grid;
		}
		
		protected DataProvider<B,Map<String,Object>> newDataProvider() {
			final FailableConsumer<Consumer<B>,?> rdr = getReader();
			if (rdr == null) {
				throw new NullPointerException("No reader has been configured. Did you invoke the reader() method?");
			}
			final Predicate<B> predicate = getPredicate();
			final FetchCallback<B,Map<String,Object>> fetchCallback = (q) -> {
				final int limit = q.getLimit();
				final int offset = q.getOffset();
				final Comparator<B> comparator = getComparator(q.getSortOrders());
				final List<B> list = new ArrayList<>();
				final Predicate<B> limitFilter = Filters.limit(offset, limit);
				try {
					rdr.accept((b) -> {
						if (predicate.test(b)  &&  limitFilter.test(b)) {
							list.add(b);
						}
					});
				} catch (Throwable t) {
					throw Exceptions.show(t);
				}
				if (comparator != null) {
					list.sort(comparator);
				}
				return list.stream();
			};
			final CountCallback<B,Map<String,Object>> countCallback = (q) -> {
				final MutableInteger counter = new MutableInteger();
				try {
					rdr.accept((b) -> {
						if (predicate.test(b)) {
							counter.inc();
						}
					});
				} catch (Throwable t) {
					throw Exceptions.show(t);
				}
				return counter.intValue();
			};
			return DataProvider.fromFilteringCallbacks(fetchCallback, countCallback);
		}

		protected Predicate<B> getPredicate() {
			throw new NotImplementedException();
		}

		protected <T> Comparator<T> getComparator(Column<B,T> pColumn) {
			if (pColumn.isSortable()) {
				final Comparator<T> comp = pColumn.getComparator();
				if (comp != null) {
					return comp;
				}
				final Comparator<T> generatedComp = getComparatorGenerator().getComparator(pColumn.getColumnType(), pColumn.isCaseSensitive());
				if (generatedComp == null) {
					throw new IllegalStateException("The comparator generator did not supply a comparator for column type "
							+ pColumn.getColumnType().getName());
				}
				return generatedComp;
			} else {
				return null;
			}
		}
		protected Comparator<B> getComparator(List<QuerySortOrder> pQuerySortOrders) {
			final List<Comparator<B>> comparators = new ArrayList<>();
			for (QuerySortOrder qso : pQuerySortOrders) {
				final String colId = qso.getSorted();
				final Column<B,Object> col = columnMap.get(colId);
				if (col == null) {
					throw new NoSuchElementException("Invalid column id: " + colId);
				}
				Comparator<Object> comparator = getComparator(col);
				if (comparator != null) {
					final Comparator<Object> oComparator = comparator;
					final Comparator<B> comp = (b1,b2) -> {
						final Function<B, Object> getter = col.getGetter();
						Object o1 = getter.apply(b1);
						Object o2 = getter.apply(b2);
						if (qso.getDirection() == SortDirection.DESCENDING) {
							Object o = o1;
							o1 = o2;
							o2 = o;
						}
						if (!col.isNullSafe()) {
							if (o1 == null) {
								if (o2 == null) {
									return -1;
								} else {
									return 0;
								}
							} else {
								if (o2 == null) {
									return 1;
								}
							}
						}
						return oComparator.compare(o1, o2);
					};
					comparators.add(comp);
				}
			}
			if (comparators.isEmpty()) {
				return null;
			}
			return (b1, b2) -> {
				for (Comparator<B> comparator : comparators) {
					final int result = comparator.compare(b1, b2);
					if (result != 0) {
						return result;
					}
				}
				return 0;
			};
		}

		@SuppressWarnings("unchecked")
		public <T> Column<B,T> column(String pColumnId, String pColumnName, Class<T> pColumnType, Function<B,T> pGetter) {
			final String colId = Objects.requireNonNull(pColumnId, "Column Id");
			final Class<T> colType = Objects.requireNonNull(pColumnType, "Column Type");
			final Function<B,T> getter = Objects.requireNonNull(pGetter, "Getter");
			if (columnMap.containsKey(colId)) {
				throw new IllegalArgumentException("Duplicate column id: " + colId);
			}
			Column<B, T> col = new Column<B,T>(this, colId, pColumnName, colType, getter);
			final Column<B,Object> oCol = (Column<B,Object>) col;
			columnList.add(oCol);
			columnMap.put(colId, oCol);
			return col;
		}
	}

	public static <B> GridBuilder<B> builder(Class<B> pBeanType) {
		return new GridBuilder<B>(pBeanType);
	}

}
