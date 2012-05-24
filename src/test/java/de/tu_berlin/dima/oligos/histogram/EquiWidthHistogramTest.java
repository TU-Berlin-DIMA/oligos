package de.tu_berlin.dima.oligos.histogram;

import junit.framework.Assert;

import org.junit.Before;
import org.junit.Test;


public class EquiWidthHistogramTest {
  
  private EquiWidthHistogram hist;
  private final int[] values = {1, 2, 3, 4, 5, 6, 7, 8, 9, 1, 2, 3, 4};
  
  @Before
  public void setup() {
    hist = new EquiWidthHistogram(1, 0, 10);
    for (int val : values) {
      hist.addValue(val);
    }
  }
  
  @Test
  public void testGetFrequency() {
    Assert.assertTrue(hist.getFrequency(1) == 2);
  }
}
