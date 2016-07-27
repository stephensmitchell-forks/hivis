/**
 * 
 */
package hivis.data;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.Iterator;
import java.util.List;

import hivis.common.Util;
import hivis.data.view.SeriesFunction;


/**
 * Generic data series implementation. If the data is numeric then the specialised 
 * series classes, for example {@link DataSeriesReal} should be used for efficiency.
 * 
 * @author O. J. Coleman
 */
public class DataSeriesGeneric<V> extends AbstractDataSeries<V> {
	protected List<V> elements;
	
	
	/**
	 * Create a new empty DataSeries.
	 */
	public DataSeriesGeneric() {
		super();
		elements = new ArrayList<V>();
    }
	
	/**
	 * Create a new empty DataSeries.
	 */
	public DataSeriesGeneric(V... items) {
		super();
		elements = new ArrayList<V>();
		elements.addAll(Arrays.asList(items));
	}
    
	/**
	 * Create a copy of the given DataSeries.
	 */
    public DataSeriesGeneric(DataSeriesGeneric<V> data) {
    	super();
    	this.elements = new ArrayList<V>(data.elements);
    }
    
    
	@Override
	public int length() {
		return elements.size();
	}
	
	@Override
	public V get(int index) {
		if (index < 0 || index >= elements.size()) {
			return null;
		}
		return elements.get(index);
	}
	
	@Override
	public void setValue(int index, V value) {
		if (!Util.equalsIncNull(elements.get(index), value)) {
			elements.set(index, value);
			this.setDataChanged(DataSeriesChange.ValuesChanged);
		}
	}
	
	@Override
	public void appendValue(V value) {
		elements.add(value);
		this.setDataChanged(DataSeriesChange.ValuesAdded);
	}
	
	@Override
	public void remove(int index) {
		elements.remove(index);
		this.setDataChanged(DataSeriesChange.ValuesRemoved);
	}
	
	@Override
	public V getEmptyValue() {
		return null;
	}
	
	@Override
	public V[] asArray(V[] data) {
		if (data == null || data.length < elements.size()) {
			return Arrays.copyOf(data, elements.size());
		}
		System.arraycopy(elements, 0, data, 0, elements.size());
		return data;
	}

	@Override
	public void resize(int newLength) {
		if (newLength < elements.size()) {
			while (newLength < elements.size()) {
				elements.remove(elements.size() - 1);
			}
			this.setDataChanged(DataSeriesChange.ValuesRemoved);
		}
		else if (newLength > elements.size()) {
			while (newLength > elements.size()) {
				elements.add(getEmptyValue());
			}
			this.setDataChanged(DataSeriesChange.ValuesAdded);
		}
	}

	@Override
	public DataSeries<V> getNewSeries() {
		return new DataSeriesGeneric<V>();
	}

//	@Override
//	public List<V> asList() {
//		return Collections.unmodifiableList(elements);
//	}
}
