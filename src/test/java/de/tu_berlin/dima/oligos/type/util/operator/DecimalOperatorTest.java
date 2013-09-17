/*******************************************************************************
 * Copyright 2013 DIMA Research Group, TU Berlin (http://www.dima.tu-berlin.de)
 * 
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 * 
 *   http://www.apache.org/licenses/LICENSE-2.0
 * 
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 ******************************************************************************/
package de.tu_berlin.dima.oligos.type.util.operator;

import static org.junit.Assert.*;

import java.math.BigDecimal;
import java.sql.SQLException;
import java.util.Random;

import org.junit.Before;
import org.junit.Test;

import de.tu_berlin.dima.oligos.type.util.operator.numerical.BigDecimalOperator;

public class DecimalOperatorTest {
  
  private static final long SEED = 0xDEADBEEFl;
  private static final int SCALE = 2;
  private static final int ITERATIONS = 100;
  
  private final Random rand = new Random(SEED);
  private final Operator<BigDecimal> op = new BigDecimalOperator();
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
  public void testIncrement() throws SQLException {
    BigDecimal step = BigDecimal.valueOf(1, SCALE);
    for (int i = 0; i < ITERATIONS; i++) {
      BigDecimal orig = BigDecimal.valueOf(rand.nextLong(), SCALE);
      BigDecimal expected = orig.add(step);
      assertEquals(expected, op.increment(orig));
    }
  }

  @Test
  public void testDecrement() throws SQLException {
    BigDecimal step = BigDecimal.valueOf(1, SCALE);
    for (int i = 0; i < ITERATIONS; i++) {
      BigDecimal orig = BigDecimal.valueOf(rand.nextLong(), SCALE);
      BigDecimal expected = orig.subtract(step);
      assertEquals(expected, op.decrement(orig));
    }
  }

  @Test
  public void testRange() throws SQLException {
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
