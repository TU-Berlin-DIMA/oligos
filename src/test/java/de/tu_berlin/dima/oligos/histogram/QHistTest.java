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
    assertTrue(hist.numBuckets() == 3);
  }

  @Test
  public void testGetBucketIndexOf() {
    assertEquals(0, hist.indexOf(1));
    assertEquals(1, hist.indexOf(4));
    assertEquals(2, hist.indexOf(6));
    assertEquals(-1, hist.indexOf(10));
  }

  @Test
  public void testGetFrequencyAt() {
    assertEquals(3, hist.frequencyAt(0));
    assertEquals(4, hist.frequencyAt(1));
    assertEquals(4, hist.frequencyAt(2));
  }

  @Test
  public void testGetFrequencyOf() {
    assertEquals(1, hist.frequencyOf(1));
    assertEquals(2, hist.frequencyOf(4));
    assertEquals(1, hist.frequencyOf(6));
  }

}
