package de.tu_berlin.dima.oligos.histogram;

import static org.junit.Assert.*;

import org.junit.Before;
import org.junit.Test;

public class EquiDepthHistogramTest {
  
  private final static int[] VALUES = {1,1,2,2,2,3,4,4,4,4,5,6,7,8};
  private final static int NUM_BUCKETS = 4;
  
  private EquiDepthHistogram hist;

  @Before
  public void setUp() throws Exception {
    hist = new EquiDepthHistogram(NUM_BUCKETS, VALUES, true);
  }

  @Test
  public void testGetNumBuckets() {
    assertTrue(hist.getNumBuckets() == NUM_BUCKETS);
  }

}
