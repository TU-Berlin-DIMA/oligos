package de.tu_berlin.dima.oligos.type.util.operator;

import static org.junit.Assert.*;

import java.math.BigDecimal;
import java.util.Random;

import org.junit.Before;
import org.junit.Test;

public class DecimalOperatorTest {
  
  private static final long SEED = 0xDEADBEEFl;
  private static final int SCALE = 2;
  private static final int ITERATIONS = 100;
  
  private final Random rand = new Random(SEED);
  private final Operator<BigDecimal> op = new DecimalOperator(SCALE);
  private BigDecimal first;
  private BigDecimal second;
  private BigDecimal third;

  @Before
  public void setUp() throws Exception {
    first = BigDecimal.valueOf(0.1);
    second = BigDecimal.valueOf(1.0);
    third = BigDecimal.valueOf(1.0);
  }

  @Test
  public void testCompare() {
    assertTrue(op.compare(first, second) < 0);
    assertTrue(op.compare(second, third) == 0);
    assertTrue(op.compare(first, third) < 0);
    assertTrue(op.compare(second, first) > 0);
    for (int i = 0; i < ITERATIONS; i++) {
      double origFirst = rand.nextDouble();
      double origSecond = rand.nextDouble();
      BigDecimal decFirst = BigDecimal.valueOf(origFirst);
      BigDecimal decSecond = BigDecimal.valueOf(origSecond);
      int doubleCmp = Double.compare(origFirst, origSecond);
      int decimalCmp = op.compare(decFirst, decSecond);
      assertTrue("Decimal comparison of " + origFirst + " and " + origSecond
          + " was " + decimalCmp + " expected " + doubleCmp
          , decimalCmp == doubleCmp);
    }
  }

  @Test
  public void testIncrement() {
    BigDecimal step = BigDecimal.valueOf(1, SCALE);
    for (int i = 0; i < ITERATIONS; i++) {
      BigDecimal orig = BigDecimal.valueOf(rand.nextLong(), SCALE);
      BigDecimal expected = orig.add(step);
      assertEquals(expected, op.increment(orig));
    }
  }

  @Test
  public void testIncrementStep() {
    fail("Not yet implemented");
    /*
    int scale = rand.nextInt() % 10;
    BigDecimal step = BigDecimal.valueOf(rand.nextLong(), scale);
    for (int i = 0; i < ITERATIONS; i++) {
      BigDecimal orig = BigDecimal.valueOf(rand.nextLong(), scale);
      BigDecimal expected = orig.add(step);
      assertEquals(expected, op.increment(orig, step));
    }
    */
  }

  @Test
  public void testDecrement() {
    BigDecimal step = BigDecimal.valueOf(1, SCALE);
    for (int i = 0; i < ITERATIONS; i++) {
      BigDecimal orig = BigDecimal.valueOf(rand.nextLong(), SCALE);
      BigDecimal expected = orig.subtract(step);
      assertEquals(expected, op.decrement(orig));
    }
  }

  @Test
  public void testDecrementStep() {
    fail("Not yet implemented");
  }

  @Test
  public void testRange() {
    assertEquals(10, op.range(first, second));
    for (int i = 0; i < ITERATIONS; i++) {
      BigDecimal val1 = BigDecimal.valueOf(rand.nextLong(), SCALE);
      BigDecimal val2 = BigDecimal.valueOf(rand.nextLong(), SCALE);
      long expected = val1.unscaledValue().subtract(val2.unscaledValue()).longValue();
      expected = Math.abs(expected) + 1l;
      assertEquals(expected, op.range(val1, val2));
      assertEquals(expected, op.range(val2, val1));
    }
  }

  @Test
  public void testMin() {
    for (int i = 0; i < ITERATIONS; i++) {
      BigDecimal val1 = BigDecimal.valueOf(rand.nextLong(), SCALE);
      BigDecimal val2 = BigDecimal.valueOf(rand.nextLong(), SCALE);
      BigDecimal expected = val1.min(val2);
      assertEquals(expected, op.min(val1, val2));
    }
  }

}
