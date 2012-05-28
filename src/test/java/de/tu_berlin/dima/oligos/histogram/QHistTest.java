package de.tu_berlin.dima.oligos.histogram;

import static org.junit.Assert.*;

import org.junit.Before;
import org.junit.Test;

import de.tu_berlin.dima.oligos.type.Operator;

public class QHistTest {

  // private static final int[] REF_SEQUENCE = { 1, 1, 2, 3, 4, 4, 4, 5, 6, 6, 7 };
  // private static final int[] REF_FREQUENCIES = { 3, 4, 4 };
  private static final Integer[] BOUNDARIES = { 2, 4, 7 };
  private static final long[] SUM_FREQUENCIES = { 3l, 7l, 11l };

  private QHist<Integer> hist;

  @Before
  public void setUp() throws Exception {
    Operator<Integer> op = new Operator<Integer>() {
      
      @Override
      public Integer increment(Integer value) {
        return value + 1;
      }
      
      @Override
      public int difference(Integer val1, Integer val2) {
        return Math.abs(val1 - val2);
      }
      
      @Override
      public Integer decrement(Integer value) {
        return value - 1;
      }
    };
    hist = new QHist<Integer>(BOUNDARIES, SUM_FREQUENCIES, 1, 7, 7l, 0l, op);
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
