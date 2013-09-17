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

import java.sql.SQLException;
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
  public void testIncrement() throws SQLException {
    for (int i = 0; i < ITERATIONS; i++) {
      Integer value = rand.nextInt();
      assertTrue(value + 1 == operator.increment(value));
    }
  }

  @Test
  public void testDecrement() throws SQLException {
    for (int i = 0; i < ITERATIONS; i++) {
      Integer value = rand.nextInt();
      assertTrue(value - 1 == operator.decrement(value));
    }
  }

  @Test
  public void testRange() throws SQLException {
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
