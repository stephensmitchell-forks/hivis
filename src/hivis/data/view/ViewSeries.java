package hivis.data.view;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import com.google.common.reflect.TypeToken;

import hivis.data.AbstractDataSeries;
import hivis.data.DataEvent;
import hivis.data.DataListener;
import hivis.data.DataSeries;

public abstract class ViewSeries<I, O> extends AbstractDataSeries<O> implements DataListener {
	private TypeToken<O> typeToken = new TypeToken<O>(getClass()) {};
	private Class<?> type = typeToken.getRawType();
	
	/**
	 * The (optional) input series on which this view is based. Null if no input series are used.
	 */
	protected List<DataSeries<I>> inputSeries;

	/**
	 * Create a ViewSeries for the given input series, with length equal to the (first) input series.
	 */
	public ViewSeries(DataSeries<I>... input) {
		inputSeries = Arrays.asList(Arrays.copyOf(input, input.length));
		for (DataSeries<I> s : inputSeries) {
			s.addChangeListener(this);
		}
	}
	
	/**
	 * Create a ViewSeries that is not based in input series.
	 */
	public ViewSeries() {
	}
	
	@Override
	public Class<?> getType() {
		if (length() == 0) {
			return type;
		}
		return get(0).getClass();
	}
	
	@Override
	public void setValue(int index, O value) {
		throw new UnsupportedOperationException("Can not set values in a calculated series.");
	}

	@Override
	public void appendValue(O value) {
		throw new UnsupportedOperationException("Can not append values to a calculated series.");
	}

	@Override
	public void remove(int index) {
		throw new UnsupportedOperationException("Can not remove values from a calculated series.");
	}

	@Override
	public void resize(int newLength) {
		throw new UnsupportedOperationException("Can not resize a calculated series.");
	}

	@Override
	public void dataChanged(DataEvent event) {
		if (inputSeries.contains(event.affected)) {
			// Forward the change event.
			this.fireChangeEvent(new DataEvent(this, event, event.getTypes().toArray()));
		}
	}

}