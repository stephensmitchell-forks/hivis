/**
 * This library is free software; you can redistribute it and/or
 * modify it under the terms of the GNU Lesser General Public License
 * as published by the Free Software Foundation; either version 2.1
 * of the License, or (at your option) any later version.
 * This library is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU
 * Lesser General Public License for more details.
 * 
 * You should have received a copy of the GNU Lesser General
 * Public License along with this library; if not, write to the
 * Free Software Foundation, Inc., 59 Temple Place, Suite 330,
 * Boston, MA 02111-1307 USA
 */
package hivis.data.view;

import java.io.File;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

import com.google.common.collect.Streams;

import hivis.common.HV;
import hivis.data.AbstractUnmodifiableDataSeries;
import hivis.data.AggregateFunction;
import hivis.data.DataEvent;
import hivis.data.DataMap;
import hivis.data.DataRow;
import hivis.data.DataSeries;
import hivis.data.DataSeriesDouble;
import hivis.data.DataTable;
import hivis.data.DataValue;

/**
 * <p>
 * Create a view of a DataTable containing the rows collected into groups. The
 * grouping is represented as a {@link DataMap} where the map keys represent the
 * group identifier and the map values are DataTable views containing the rows
 * belonging to that group. See the various constructors for details on how
 * groups are formed.
 * </p>
 * <p>
 * The rows in the group table views appear in the same order as their order in
 * the input table.
 * </p>
 * <p>
 * The group table views returned by {@link #get(Object)} and {@link #values()}
 * will be emptied (set to length 0) if the group size becomes zero (and calls
 * to {@link #get(Object)} for groups that do not yet exist will return empty
 * group table views, but not add them to the list of groups returned by
 * {@link #values()} ). If the group size subsequently becomes non-zero this
 * same group table view will be reused. This allows external observers to
 * monitor the size of a group (even before it has existed in the input table).
 * </p>
 *
 * @author O. J. Coleman
 */
public class DefaultGroupedTable<K> extends CalcMap<K, TableView, DataTable> implements GroupedTable<K> {
	/**
	 * The function used to generate group keys from values.
	 */
	protected Function<DataRow, K> keyFunction;

	/**
	 * A map of all groups ever produced. Allows re-use of groups.
	 */
	protected Map<K, TableViewFilterRows> allGroups = new HashMap<>();

	/**
	 * Create a grouping where the key for a row is based on the value of the
	 * specified series in that row. More specifically, groups are formed by
	 * placing all rows for which
	 * input.getRow(x).get(groupingSeries).equals(input.getRow(y).get(
	 * groupingSeries)), for all rows x and y, into the same group (and rows
	 * where this is false into different groups). The key for each group is a
	 * value such that key.equals(group.getRow(z).get(groupingSeries)) for all
	 * rows z in the group table.
	 */
	public DefaultGroupedTable(DataTable input, int groupingSeries) {
		super(input);
		keyFunction = new Function<DataRow, K>() {
			@Override
			public K apply(DataRow input) {
				return (K) input.get(groupingSeries);
			}
		};
	}

	/**
	 * Create a grouping where the key for a row is based on the value of the
	 * specified series in that row. More specifically, groups are formed by
	 * placing all rows for which
	 * input.getRow(x).get(groupingSeries).equals(input.getRow(y).get(
	 * groupingSeries)), for all rows x and y, into the same group (and rows
	 * where this is false into different groups). The key for each group is a
	 * value such that key.equals(group.getRow(z).get(groupingSeries)) for all
	 * rows z in the group table.
	 */
	public DefaultGroupedTable(DataTable input, String groupingSeries) {
		super(input);
		keyFunction = new Function<DataRow, K>() {
			@Override
			public K apply(DataRow input) {
				return (K) input.get(groupingSeries);
			}
		};
	}

	/**
	 * Create a grouping where the key for a row is calculated using the given
	 * key function, which should accept a row from the table and return a group
	 * key (identifier) for that row. Groups are formed by placing all rows for
	 * which keyFunction(input.getRow(x)).equals(keyFunction(input.getRow(y)),
	 * for all rows x and y, into the same group (and rows where this is false
	 * into different groups). The key for each group is a value such that
	 * key.equals(keyFunction(group.getRow(z)) for all rows z in the group
	 * table.
	 */
	public DefaultGroupedTable(DataTable input, Function<DataRow, K> keyFuntion) {
		super(input);
		this.keyFunction = keyFuntion;
	}

	@Override
	public TableView get(K key) {
		if (!allGroups.containsKey(key)) {
			TableViewFilterRows newGroup = new TableViewFilterRows(input, new GroupRowFilter(key));
			allGroups.put(key, newGroup);
		}
		return allGroups.get(key);
	}

	@Override
	public void update() {
		// Determine current groups.
		Map<K, TableView> newGroups = new HashMap<>();

		for (DataRow row : input) {
			K key = keyFunction.apply(row);
			if (!newGroups.containsKey(key)) {
				TableViewFilterRows group = (TableViewFilterRows) get(key);
				newGroups.put(key, group);
			}
		}

		// Remove groups that no longer exist.
		for (K key : cache.keys().asArray()) {
			if (!newGroups.containsKey(key)) {
				cache.remove(key);
			}
		}

		// Add new groups.
		for (K key : newGroups.keySet()) {
			if (!cache.containsKey(key)) {
				cache.put(key, newGroups.get(key));
			}
		}
	}

	private class GroupRowFilter implements RowFilter {
		K key;

		GroupRowFilter(K key) {
			this.key = key;
		}

		@Override
		public boolean excludeRow(DataTable input, int index) {
			return !keyFunction.apply(input.getRow(index)).equals(key);
		}
	}
	

	@Override
	public TableView aggregate(final AggregateFunction function) {
		final DataTable source = this.input;
		final DefaultGroupedTable<K> me = this;
		
		AbstractTableView<?, ?> view = new AbstractTableView<CalcSeries<?, ?>, DefaultGroupedTable<K>>(this) {
			@Override
			protected void updateSeries(List<Object> eventTypes) {
				// Maintain old list of series so we can reuse them. 
				HashMap<String, CalcSeries<?, ?>> oldSeries = new HashMap<>(series);
				
				series.clear();
				
				for (final String columnLabel : source.getSeriesLabels()) {
					CalcSeries<?, ?> s = oldSeries.containsKey(columnLabel) ? oldSeries.get(columnLabel) : new CalcSeries<Object, Object> (source) {
						@Override
						@SuppressWarnings({ "unchecked", "rawtypes" })
						public DataSeries getNewSeries() {
							if (source.get(columnLabel).isNumeric()) {
								return new DataSeriesDouble();
							}
							return source.get(columnLabel).getNewSeries();
						}
						@Override
						public void update() {
							int k = 0;
							for (K groupKey : me.keys()) {
								DataSeries<?> groupTableSeries = me.get(groupKey).get(columnLabel);
								cache.set(k, function.apply(columnLabel, groupTableSeries));
								k++;
							}
						}
						@Override
						public int length() {
							return me.size();
						}
					};
					
					series.put(columnLabel, s);
				}
			}
		};
		
		// Trigger initial update.
		view.updateSeries();
		
		return view;
	}


	@Override
	public TableView aggregateMin() {
		return this.aggregate(new AggStat() {
	        public Object applyStat(DataSeries<?> series) {
	            return series.min().get();
	        }
	    });
	}

	@Override
	public TableView aggregateMax() {
		return this.aggregate(new AggStat() {
	        public Object applyStat(DataSeries<?> series) {
	            return series.max().get();
	        }
	    });
	}

	@Override
	public TableView aggregateSum() {
		return this.aggregate(new AggStat() {
	        public Object applyStat(DataSeries<?> series) {
	            return series.sum().get();
	        }
	    });
	}

	@Override
	public TableView aggregateProduct() {
		return this.aggregate(new AggStat() {
	        public Object applyStat(DataSeries<?> series) {
	            return series.product().get();
	        }
	    });
	}

	@Override
	public TableView aggregateMean() {
		return this.aggregate(new AggStat() {
	        public Object applyStat(DataSeries<?> series) {
	            return series.mean().get();
	        }
	    });
	}

	@Override
	public TableView aggregateVariance() {
		return this.aggregate(new AggStat() {
	        public Object applyStat(DataSeries<?> series) {
	            return series.variance().get();
	        }
	    });
	}

	@Override
	public TableView aggregateStdDev() {
		return this.aggregate(new AggStat() {
	        public Object applyStat(DataSeries<?> series) {
	            return series.stdDev().get();
	        }
	    });
	}
	
	
	private static abstract class AggStat implements AggregateFunction {
		public Object apply(String seriesLabel, DataSeries<?> series) {
	        if (series.isNumeric()) {
	          return applyStat(series);
	        }
	        return series.get(0);
	      }
		public abstract Object applyStat(DataSeries<?> series);
	}
}