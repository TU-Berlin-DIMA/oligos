package de.tu_berlin.dima.oligos.histogram;

import java.util.Arrays;

public class EquiDepthHistogram {

  private final int[] bounds;
  private final int[] frequencies;
  private final int minValue;
  private final int maxValue;
  private final int numValues;
  private long totalValues = 0l;

  /*public EquiDepthHistogram(int numQuantils, int[] values, boolean sorted, int minValue,
      int maxValue) {
    this.bounds = new int[numQuantils + 1];
    this.frequencies = new int[numQuantils];
    this.minValue = minValue;
    this.maxValue = maxValue;
    this.numValues = values.length;
    if (!sorted) {
      Arrays.sort(values);
    }
    addPoints(values);
  }*/
  
  public EquiDepthHistogram(int numQuantils, int[] values, boolean sorted) {
    if (!sorted) {
      Arrays.sort(values);
    }
    this.bounds = new int[numQuantils + 1];
    this.frequencies = new int[numQuantils];
    this.minValue = values[0];
    this.maxValue = values[values.length - 1];
    this.numValues = values.length;
    addPoints(values);
  }

  public int getNumBuckets() {
    return bounds.length - 1;
  }

  public int getLowerBoundAt(int index) {
    return bounds[index];
  }

  public int getUpperBoundAt(int index) {
    return bounds[index + 1];
  }

  private void addPoints(int[] values) {
    int maxSize = numValues / getNumBuckets();
    int index = 0;
    bounds[index] = minValue;
    bounds[bounds.length - 1] = maxValue;
    
    for (int val : values) {      
      if (frequencies[index] > maxSize) {
        index++;
        bounds[index] = val;
      }
      frequencies[index]++;
      totalValues++;
    }
  }

}
