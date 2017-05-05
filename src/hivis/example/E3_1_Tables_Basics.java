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

package hivis.example;


import java.util.Comparator;

import hivis.common.HV;
import hivis.data.DataMap;
import hivis.data.DataRow;
import hivis.data.DataSeries;
import hivis.data.DataTable;
import hivis.data.view.Function;
import hivis.data.view.RowFilter;
import hivis.data.view.SeriesFunction;

/**
 * Examples of working with {@link DataTable}s.
 * 
 * @author O. J. Coleman
 */
public class E3_1_Tables_Basics {
	public static void main(String[] args) {
		// DataTables represent an ordered, labelled set of DataSeries, typically of the same length.
		// The series may store different data types.
		// For the complete API see https://olivercoleman.github.io/hivis/reference/hivis/data/DataTable.html
		
		// Make a new empty table.
		DataTable myTable = HV.newTable();
		
		// Make some new series.
		// HV.randomIntegerSeries(length, min, max) creates a new series containing randomly generated integer values.
		// HV.randomUniformSeries(length, min, max) creates a new series containing randomly generated real values.
		DataSeries<Integer> ints = HV.randomIntegerSeries(5, 0, 10);
		DataSeries<Double> reals = HV.randomUniformSeries(5, 0, 10);
		
		
		// Add our series to our table. Each series in a table must have a unique label.
		myTable.addSeries("ints", ints);
		myTable.addSeries("reals", reals);
		
		
		// Get another table containing some series with randomly generated values, with same length as the first table.
		DataTable randomTable = HV.makeRandomTable(myTable.length());
		
		
		// Add some of the series from the random table to the first table.
		// Selecting series by label.
		myTable.addSeries("rand", randomTable.get("real normal"));
		// Selecting series by index (counting from 0).
		myTable.addSeries("date", randomTable.get(3));
		
		
		// Print out our table.
		System.out.println("myTable:\n" + myTable);
		
		
		// Data can be loaded from a spreadsheet file with something like 
		// (uncomment and set the file location to a real file to try it out):
		//   DataTable ssData = HV.loadSpreadSheet(new File("myspreadsheet.xlsx"));
		// The above will try to figure out where the header row is and where the 
		// data starts.
		// To specify these parameters, and many more, use something like:
		//   DataTable data = HV.loadSpreadSheet(
	    //     HV.loadSSConfig().sourceFile("myspreadsheet.csv").headerRowIndex(2).rowIndex(3).columnIndex(2)
	    //   );
	    // For all configuration options see https://olivercoleman.github.io/hivis/reference/hivis/data/reader/SpreadSheetReader.Config.html
	    // You can call as many of the configuration option methods as necessary, in any order.
		// Note that the various 'index' options start counting at 0, not 1.
		
		
		// Note that changes to the series after they're added to the table will be reflected in the table.
		// (By default a table stores "references" to the series it contains).
		// Here we change the first value in our "reals" series.
		reals.set(0, -5);
		System.out.println("\nmyTable reflecting changed value in 'reals' series:\n" + myTable);
		

		// Iterate over each row in a table.
		System.out.println("\nSome selected values from myTable retrieved via row iterator:");
		for (DataRow row : myTable) {
			// We can get the value for a column by specifying the column index or label,
			// and we can get the value as one of the primitive types boolean, int, long, 
			// float or double, or as a generic Object:
			int myInt = row.getInt("ints");
			Object myObject = row.get(3);
			
			// The requested type doesn't have to match the type stored by the column/series, 
			// as long as the stored type can be converted to the requested type:
			float myFloat = row.getFloat("reals");
			
			System.out.println("row " + row.getRowIndex() + ":\t" + myInt + ",\t" + myFloat + ",\t\t" + myObject);
		}
	}
}
