import hivis.common.*;
import hivis.data.*;
import hivis.data.reader.*;
import hivis.data.view.*;

// Plots the ratio of the corresponding values stored in two columns, 
// separated into sub plots according to the integer values stored in a third column.
// Hovering over a point will provide more information.
// Exercises:
// 1. Try plotting ratio or other functions over other series/columns.
// 3. Change the shape that is drawn depending on the value of a series with integer values.
// 2. Currently when hovering over two data points that are close to each other they 
//    may both have labels shown for them simultaneously and rather messily. Modify the code 
//    so that only the data point closest to the mouse pointer has its label shown (hint: only 
//    draw the label after iterating over all points and recording the closest).


// Stores the data to plot.
DataTable data;

// Series containing data we want to plot.
DataSeries cyl; // // Number of cylinders.
DataSeries disp; // // Displacement.
DataSeries hp; // Horse Power. Because that's still a sensible unit of power. Neigh!
DataSeries hpOnDisp; // hp divided by the corresponding values in disp.


void setup() {
  size(1000, 300);
  textSize(15);
  colorMode(HSB, 1, 1, 1, 1);
  
  // Get a data set.
  data = HV.mtCars();
  
  // Get some series from the data table.
  cyl = data.getSeries("cyl");
  DataSeries dispOrig = data.getSeries("disp");
  DataSeries hpOrig = data.getSeries("hp");
  
  // Get a series containing the values in hp divided by the corresponding values in disp.
  hpOnDisp = hpOrig.divide(dispOrig);
  
  // Scale the series used for x and y coorindates (disp and hp) to unit range [0, 1].
  disp = dispOrig.toUnitRange();
  hp = hpOrig.toUnitRange();
}


// Draws the plot.
void draw() {
  background(0, 0, 0);
  
  int xScale = 1000;
  int xOffset = 60;
  int yScale = 50;
  int yOffset = 40;
  
  // Draw row labels (for 4, 6 and 8 cylinders).
  fill(1);
  textAlign(LEFT, CENTER);
  for (int c = 4; c <= 8; c += 2) {
    text(c, 10, (c - 4) * yScale + yOffset);
  }
  
  for (int row = 0; row < data.length(); row++) {
    int c = cyl.getInt(row);
    
    int y = (c - 4) * yScale + yOffset;
    float x = (hpOnDisp.getFloat(row) - hpOnDisp.min().getFloat()) * xScale + xOffset;
    
    float w = disp.getFloat(row) * 100;
    float h = hp.getFloat(row) * 100;
    
    // Determine if we're hovering over a data point by testing if the distance from 
    // the centre of it to the mouse is less than the average/approximate radius of the point. 
    float approxRadius = (w + h) / (2 * 2);
    boolean isHovering = distanceToMouse(x, y) < approxRadius;
    
    float hue = c / 10.0;
    float saturationAndBrightness = isHovering ? 1 : 0.8;
    float alpha = isHovering ? 1 : 0.6; // make semi-transparent if mouse is not hovering over this point.
    
    fill(hue, saturationAndBrightness, saturationAndBrightness, alpha);
    ellipse(x, y, w, h);
    
    if (isHovering) {
      fill(1, 0, 1, 1);
      text("  hp: " + hp.get(row) + "\ndisp: " + disp.get(row), x, y-40);
    }
  }
}

// Returns the distance to the mouse pointer for the given coordinates.
float distanceToMouse(float x, float y) {
  float dx = x - mouseX;
  float dy = y - mouseY;
  return sqrt(dx*dx + dy*dy);
}