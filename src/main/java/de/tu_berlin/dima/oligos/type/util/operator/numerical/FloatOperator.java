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

public class FloatOperator extends AbstractOperator<Float> {

  @Override
  public Float increment(Float value) {
    return Math.nextUp(value);
  }

  @Override
  public Float decrement(Float value) {
    return Math.nextAfter(value, Float.NEGATIVE_INFINITY);
  }

  @Override
  public long range(Float val1, Float val2) {
    int a = Float.floatToIntBits(val1);
    int b = Float.floatToIntBits(val2);
    return a - b + 1L;
  }
}
