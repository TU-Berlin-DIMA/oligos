package de.tu_berlin.dima.oligos.stats;

import static org.junit.Assert.*;

import java.util.List;
import java.util.SortedSet;

import org.junit.Before;
import org.junit.Test;

import de.tu_berlin.dima.oligos.stat.Bucket;
import de.tu_berlin.dima.oligos.stat.histogram.QuantileHistogram;
import de.tu_berlin.dima.oligos.type.util.operator.IntegerOperator;
import de.tu_berlin.dima.oligos.type.util.operator.Operator;

public class QuantileHistogramTest {
  
  public static final int[] BOUNDARIES = {1, 2, 5, 10};
  public static final long[] FREQUENCIES = {10, 10, 10, 10};
  
  private QuantileHistogram<Integer> histogram;
  private Operator<Integer> operator;

  @Before
  public void setUp() throws Exception {
    operator = IntegerOperator.getInstance();
    histogram = new QuantileHistogram<Integer>(0, operator);
    for (int i = 0; i < BOUNDARIES.length; i++) {
      histogram.addBound(BOUNDARIES[i], FREQUENCIES[i]);
    }
  }
  
  @Test
  public void testSizes() {
    assertEquals(histogram.getLowerBounds().size(), histogram.getUpperBounds().size());
    assertEquals(histogram.getUpperBounds().size(), histogram.getFrequencies().size());
    assertEquals(histogram.getLowerBounds().size(), histogram.getFrequencies().size());
  }

  @Test
  public void testBoundaries() {
    SortedSet<Integer> lowerBounds = histogram.getLowerBounds();
    SortedSet<Integer> upperBounds = histogram.getUpperBounds();
    List<Long> frequencies = histogram.getFrequencies();
    
    for (Bucket<Integer> bucket : histogram) {
      assertTrue(operator.compare(bucket.getLowerBound(), bucket.getUpperBound()) <= 0);
    }
  }

}
