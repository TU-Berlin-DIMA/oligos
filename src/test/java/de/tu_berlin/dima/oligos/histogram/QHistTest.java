package de.tu_berlin.dima.oligos.histogram;

import static org.junit.Assert.*;

import org.junit.Before;
import org.junit.Test;

public class QHistTest {
  
  private static final int[] REF_SEQUENCE = {1, 1, 2, 3, 4, 4, 4, 5, 6, 6, 7};
  private static final int[] REF_FREQUENCIES = {3, 4, 4};
  private static final Integer[] BOUNDARIES = {2, 4, 7};
  private static final int[] SUM_FREQUENCIES = {3, 7, 11};
  
  private QHist hist;

  @Before
  public void setUp() throws Exception {
    hist = new QHist(BOUNDARIES, SUM_FREQUENCIES);
  }

  @Test
  public void testGetNumBuckets() {
    assertTrue(hist.getNumBuckets() == 3);
  }

  @Test
  public void testGetBucketIndexOf() {
    assertEquals(0, hist.getIndexOf(1));
    assertEquals(1, hist.getIndexOf(4));
    assertEquals(2, hist.getIndexOf(6));
    assertEquals(-1, hist.getIndexOf(10));
  }
  
  @Test
  public void testGetFrequencyAt() {
    assertEquals(3, hist.getFrequencyAt(0));
    assertEquals(4, hist.getFrequencyAt(1));
    assertEquals(4, hist.getFrequencyAt(2));
  }
  
  @Test
  public void testGetFrequencyOf() {
    assertEquals(1, hist.getFrequencyOf(1, 2));
    assertEquals(2, hist.getFrequencyOf(4, 2));
    assertEquals(1, hist.getFrequencyOf(6, 3));
  }

}
