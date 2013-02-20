package de.tu_berlin.dima.oligos.type.util.operator;

import static org.junit.Assert.*;

import java.util.Random;

import org.junit.Before;
import org.junit.Test;

import de.tu_berlin.dima.oligos.type.util.operator.numerical.IntegerOperator;

public class IntegerOperatorTest {

  private final static long SEED = 0xDEADBEEF;
  private final static int ITERATIONS = 100;
  private Operator<Integer> operator;
  private Random rand;

  @Before
  public void setUp() throws Exception {
    this.operator = new IntegerOperator();
    this.rand = new Random(SEED);
  }

  @Test
  public void testIncrement() {
    for (int i = 0; i < ITERATIONS; i++) {
      Integer value = rand.nextInt();
      assertTrue(value + 1 == operator.increment(value));
    }
  }

  @Test
  public void testDecrement() {
    for (int i = 0; i < ITERATIONS; i++) {
      Integer value = rand.nextInt();
      assertTrue(value - 1 == operator.decrement(value));
    }
  }

  @Test
  public void testRange() {
    for (int i = 0; i < ITERATIONS; i++) {
      Integer val1 = rand.nextInt();
      Integer val2 = rand.nextInt();
      long range = operator.range(val1, val2);
      assertTrue(range >= 1);
      assertTrue(Math.abs(val2 - val1) + 1 == range);
    }
  }

  @Test
  public void testMin() {
    for (int i = 0; i < ITERATIONS; i++) {
      Integer val1 = rand.nextInt();
      Integer val2 = rand.nextInt();
      assertTrue(Math.min(val1, val2) == operator.min(val1, val2));
    }
  }

  @Test
  public void testMax() {
    for (int i = 0; i < ITERATIONS; i++) {
      Integer val1 = rand.nextInt();
      Integer val2 = rand.nextInt();
      assertTrue(Math.max(val1, val2) == operator.max(val1, val2));
    }
  }

  @Test
  public void testCompare() {
    for (int i = 0; i < ITERATIONS; i++) {
      Integer val1 = rand.nextInt();
      Integer val2 = rand.nextInt();
      assertTrue(val1.compareTo(val2) == operator.compare(val1, val2));
    }
  }

}
