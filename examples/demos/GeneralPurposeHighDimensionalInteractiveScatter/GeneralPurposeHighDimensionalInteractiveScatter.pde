import hivis.common.*;
import hivis.data.*;
import hivis.data.reader.*;
import hivis.data.view.*;
import controlP5.*;
import java.util.*;

// Creates a multi-dimensional visualisation of a user selected spreadsheet.

// The available plot dimensions, the ways of visualisaing the data.
String[] plotDims = new String[] {"label", "x", "y", "hue", "brightness", "shape size", "shape edges", "shape type"};

// The list of selectors to select a table column/series for each plot dimension.
// <ScrollableList> indicates that the list stores 'ScrollableList's
List<ScrollableList> selectors;

// The source data
DataTable data;

// The data to plot.
DataTable toPlot;

// This is a flag to indicate that data is being (re)loaded and so the plot 
// should not be drawn yet.
boolean settingUp = true;


void setup() {
  size(1000, 700);
  textSize(15);
  
  ControlP5 cp5 = new ControlP5(this);
  
  selectors = new ArrayList();
  for (int d = 0; d < plotDims.length; d++) {
    ScrollableList list = cp5.addScrollableList(plotDims[d])
      .setPosition(d * 110 + 10, 15)
      .setSize(100, 100)
      .setBarHeight(15)
      .setItemHeight(15);
    
    selectors.add(list);
    
    // Listen for when a column selection is made with this selector.
    list.addListener(new ControlListener() {
      public void controlEvent(ControlEvent event) {
        // Update the plot table when a new selection is made.
        updatePlotTable();
      }
    });
  }
  
  // We automatically load the supplied data file by default. 
  // If you want to load your own data then comment the below line and 
  // uncomment the line beginning "selectInput(..." 
  fileSelected(sketchFile("mtcars.xlsx"));
  //selectInput("Select an xlsx or CSV file to visualise:", "fileSelected");
}


void fileSelected(File selection) {
  if (selection == null) {
    println("No file selected.");
  } 
  else {
    // Get data from spread sheet. The SpreadSheetReader will automatically 
    // update the DataTable it provides.
    data = HV.loadSpreadSheet(
	    HV.loadSSConfig().sourceFile(selection)
	  );
  
    println("\nLoaded data:\n" + data);
    
    int selectIndex = 0;
    for (ScrollableList list : selectors) {
      list.clear();
      list.addItems(data.getSeriesLabels());
      
      // Automatically select the next column.
      list.setValue(selectIndex++);
    }
    
    updatePlotTable();
  }
}


void updatePlotTable() {
  settingUp = true;
  
  toPlot = HV.newTable();
  // Add the selected series to the plot table.
  //println();
  for (int d = 0; d < plotDims.length; d++) {
    ScrollableList select = selectors.get(d);
    int seriesIndex = (int) select.getValue();
    
    //println(data.getLabel(seriesIndex) + " => " + plotDims[d]);
    
    DataSeries series;
    // First dimension is label, we just convert values to strings in draw method.
    // 7th dimension is edges, just use raw value rounded to int.
    if (seriesIndex == 0 || seriesIndex == 6) {
      series = data.get(seriesIndex);
    }
    else {
      // For all other plot dimensions get a view of the series that converts the 
      // values to a unit range [0, 1] as this is easier to work with in the draw method.
      series = data.get(seriesIndex).toUnitRange();
    }
   
    // Just use dimension index as the label to ensure the labels are unique.
    toPlot.addSeries("" + d, series);
  }
  
  settingUp = false;
}


// Draws the chart and a title.
void draw() {
  background(0, 0, 0);
  textSize(12);
  colorMode(HSB, 1, 1, 1, 1);
  
  fill(1, 0, 1, 1);
  for (int d = 0; d < plotDims.length; d++) {
    text(plotDims[d], d * 110 + 15, 10);
  }
  
  if (!settingUp) {
    strokeWeight(2);
    
    float minShapeSize = 15;
    float maxShapeSize = 50;
    float paddingTop = 60;
    
    for (DataRow row : toPlot) {
      //"label", "x", "y", "hue", "brightness", "shape size", "shape type", "shape edges"
      String label = row.get(0).toString();
      float x = row.getFloat(1) * (width - maxShapeSize - 10) + 5 + maxShapeSize/2;
      float y = (1 - row.getFloat(2)) * (height - maxShapeSize - paddingTop - 10) + paddingTop + 5;
      float hue = row.getFloat(3);
      float bri = row.getFloat(4) * 0.7 + 0.3;
      float size = row.getFloat(5) * (maxShapeSize - minShapeSize) + minShapeSize;
      int edges = round(row.getInt(6));
      boolean type = row.getFloat(7) > 0.5;
      
      // If this is a spiky shape rather than a polygon
      if (type) {
        noStroke();
        fill(hue, 1, bri, 0.6);
        polygon(x, y, size/2, edges);
      }
      else {
        stroke(hue, 1, bri, 0.6);
        noFill();
        spiky(x, y, size/2, edges);
      }
      fill(1, 0, 1, 0.5);
      text(label, x-maxShapeSize/3, y);
    }
  }
}

void polygon(float x, float y, float radius, int npoints) {
  float angle = TWO_PI / npoints;
  beginShape();
  for (float a = 0; a < TWO_PI; a += angle) {
    float sx = x + cos(a) * radius;
    float sy = y + sin(a) * radius;
    vertex(sx, sy);
  }
  endShape(CLOSE);
}

void spiky(float x, float y, float radius, int npoints) {
  float angle = TWO_PI / npoints;
  beginShape();
  vertex(x, y);
  for (float a = 0; a < TWO_PI; a += angle) {
    float sx = x + cos(a) * radius;
    float sy = y + sin(a) * radius;
    vertex(sx, sy);
    vertex(x, y);
  }
  endShape(CLOSE);
}