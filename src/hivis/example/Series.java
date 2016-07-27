package hivis.example;


import hivis.common.HV;
import hivis.data.DataSeries;
import hivis.data.DataTable;
import hivis.data.view.Function;
import hivis.data.view.ViewTableTranspose;

/**
 * Examples of working with DataSeries.
 * 
 * @author O. J. Coleman
 */
public class Series {
	public static void main(String[] args) {
		// Create a data series containing the specified integer numbers.
		DataSeries<Integer> intNumbers = HV.newIntegerSeries(1, 1, 2, 3, 5, 8);
		
		// Create an empty real number data series and add some values to it.
		DataSeries<Double> realNumbers = HV.newRealSeries();
		for (Integer v : intNumbers) {
			realNumbers.append(Math.log(v));
		}
		
		System.out.println("\nintNumbers\n" + intNumbers);
		System.out.println("\nrealNumbers\n" + realNumbers);
		
		
		// We can set the values in a series, here we swap the first values in our series.
		// Some thing to note here is that we assigned a real (double) number value to a series that stores integer (int) values. 
		// In general HiVis will do its best to accommodate the interchange of data of different types when it makes sense to do so. 
		// (If we tried to add a value of, say 0.5, to an integer series then an error would occur.)
		int int0 = intNumbers.get(0);
		double real0 = realNumbers.get(0);
		intNumbers.set(0, real0);
		realNumbers.set(0, int0);
		System.out.println("\nintNumbers (changed first value)\n" + intNumbers);
		System.out.println("\nrealNumbers (changed first value)\n" + realNumbers);
		
		
		// We can create new series by performing simple arithmetic operations on each element of an existing series:
		DataSeries<Double> plus1 = realNumbers.add(1);
		System.out.println("\nplus1 = realNumbers.add(1)\n" + plus1);
		// Similar functions exist for subtract, multiply and divide.
		
		// Or by performing simple arithmetic operations over all elements of two series:
		DataSeries<Double> realsMinusInts = realNumbers.subtract(intNumbers);
		System.out.println("\nrealsMinusInts = realNumbers.subtract(intNumbers)\n" + realsMinusInts);
		
		
		// We can also create new series by performing custom operations on each element of an existing series:
		DataSeries<Double> customFunc = plus1.apply(new Function<Double, Double>() {
			public Double apply(Double input) {
				return Math.pow(input, 3);
			}
		});
		System.out.println("\ncustomFunc = plus1^3\n" + customFunc);
		
		
		// Series can be appended to each other:
		DataSeries<Double> realNumbersAppendPlus1 = realNumbers.append(plus1);
		System.out.println("\nrealNumbersAppendPlus1 = realNumbers.append(plus1)\n" + realNumbersAppendPlus1);
				
		
		// These new series are "views" of the original series but with the operation performed on 
		// each element, so changes in the original series will be reflected in the new series.
		// Note that the customFunc series is a view of the "plus1" series, which in turn is a view of realNumbers; 
		// changes to the underlying data will "bubble up" through the chain of views.
		realNumbers.append(55.55);
		System.out.println("\nplus1 reflecting appended value in realNumbers\n" + plus1);
		System.out.println("\ncustomFunc reflecting appended value in realNumbers (via plus1)\n" + customFunc);
		System.out.println("\nrealNumbersAppendPlus1 reflecting appended value in realNumbers\n" + realNumbersAppendPlus1);
	}
}
