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
package de.tu_berlin.dima.oligos.stat.distribution.histogram;

public class Bucket<T> {
  private final T lowerBound;
  private final T upperBound;
  private final long frequency;
  
  public Bucket(final T lowerBound, final T upperBound, final long frequency) {
    this.lowerBound = lowerBound;
    this.upperBound = upperBound;
    this.frequency = frequency;
  }
  
  public T getLowerBound() {
    return lowerBound;
  }
  
  public T getUpperBound() {
    return upperBound;
  }
  
  public long getFrequency() {
    return frequency;
  }

  @Override
  public String toString() {
    return "" + lowerBound + '\t' + upperBound + '\t' + frequency;
  }

  @Override
  public int hashCode() {
    final int prime = 31;
    int result = 1;
    result = prime * result + (int) (frequency ^ (frequency >>> 32));
    result = prime * result
        + ((lowerBound == null) ? 0 : lowerBound.hashCode());
    result = prime * result
        + ((upperBound == null) ? 0 : upperBound.hashCode());
    return result;
  }

  @Override
  public boolean equals(Object obj) {
    if (this == obj)
      return true;
    if (obj == null)
      return false;
    if (getClass() != obj.getClass())
      return false;
    @SuppressWarnings("unchecked")
    Bucket<T> other = (Bucket<T>) obj;
    if (frequency != other.frequency)
      return false;
    if (lowerBound == null) {
      if (other.lowerBound != null)
        return false;
    } else if (!lowerBound.equals(other.lowerBound))
      return false;
    if (upperBound == null) {
      if (other.upperBound != null)
        return false;
    } else if (!upperBound.equals(other.upperBound))
      return false;
    return true;
  }  
  
}
