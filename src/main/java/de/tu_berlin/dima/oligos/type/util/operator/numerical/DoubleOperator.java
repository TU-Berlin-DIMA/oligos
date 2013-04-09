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
package de.tu_berlin.dima.oligos.type.util.operator.numerical;

import de.tu_berlin.dima.oligos.type.util.operator.AbstractOperator;

public class DoubleOperator extends AbstractOperator<Double> {

  @Override
  public long range(Double val1, Double val2) {
    long a = Double.doubleToLongBits(val1);
    long b = Double.doubleToLongBits(val2);
    return Math.abs(a - b) + 1L;
  }

  @Override
  public Double increment(Double value) {
    return Math.nextUp(value);
  }

  @Override
  public Double decrement(Double value) {
    return Math.nextAfter(value, Double.NEGATIVE_INFINITY);
  }

}
