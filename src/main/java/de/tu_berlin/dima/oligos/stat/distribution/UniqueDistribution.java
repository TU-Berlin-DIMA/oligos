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
package de.tu_berlin.dima.oligos.stat.distribution;

import de.tu_berlin.dima.oligos.type.util.operator.Operator;

public class UniqueDistribution<T> implements Distribution<T> {
  
  private final T min;
  private final T max;
  private final long cardinality;
  private final double probability;
  
  public UniqueDistribution(final T min, final T max, final long cardinality) {
    this.min = min;
    this.max = max;
    this.cardinality = cardinality;
    this.probability = 1.0 / cardinality;
  }
  
  public UniqueDistribution(final T min, final T max, final Operator<T> operator) {
    this.min = min;
    this.max = max;
    this.cardinality = operator.range(min, max);
    this.probability = 1.0 / this.cardinality;
  }

  @Override
  public T getMin() {
    return min;
  }

  @Override
  public T getMax() {
    return max;
  }

  @Override
  public long getCardinality() {
    return cardinality;
  }

  @Override
  public double getProbabilityOf(T value) {
    return probability;
  } 
  
}
