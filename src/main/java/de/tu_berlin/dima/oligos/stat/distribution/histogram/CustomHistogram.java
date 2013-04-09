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

import java.util.Iterator;
import java.util.List;
import java.util.SortedSet;

import com.google.common.collect.Lists;
import com.google.common.collect.Sets;

import de.tu_berlin.dima.oligos.type.util.operator.Operator;

public class CustomHistogram<T> extends AbstractHistogram<T> {

  private SortedSet<T> lowerBounds;
  private SortedSet<T> upperBounds;
  private List<Long> frequencies;

  public CustomHistogram(Operator<T> operator) {
    setOperator(operator);
    this.lowerBounds = Sets.newTreeSet(operator);
    this.upperBounds = Sets.newTreeSet(operator);
    this.frequencies = Lists.newArrayList();
  }
  
  @Override
  @Deprecated
  public void add(T lowerBound, T upperBound, long frequency) {
    // TODO broken
    if (lowerBounds.contains(lowerBound) || upperBounds.contains(upperBound)) {
      return;
    }
    lowerBounds.add(lowerBound);
    upperBounds.add(upperBound);
    frequencies.add(frequency);
  }

  @Override
  public T getMin() {
    return getOperator().increment(lowerBounds.first());
  }

  @Override
  public T getMax() {
    return upperBounds.last();
  }

  @Override
  public int getNumberOfBuckets() {
    assert checkConsistence();
    return lowerBounds.size();
  }

  @Override
  public int getBucketOf(T value) {
    assert checkConsistence();
    Operator<T> operator = getOperator();
    Iterator<T> lBoundsIter = lowerBounds.iterator();
    Iterator<T> uBoundsIter = upperBounds.iterator();
    int index = 0;
    while (lBoundsIter.hasNext() && uBoundsIter.hasNext()) {
      T lBound = lBoundsIter.next();
      T uBound = uBoundsIter.next();
      operator.compare(lBound, value);
      if (operator.compare(lBound, value) < 0
          && operator.compare(uBound, value) <= 0) {
        return index;
      } else {
        index++;
      }
    }
    return -1;
  }

  @Override
  public long getTotalNumberOfValues() {
    long total = 0l;
    for (long freq : frequencies) {
      total += freq;
    }
    return total;
  }

  @Override
  public SortedSet<T> getLowerBounds() {
    return lowerBounds;
  }

  @Override
  public SortedSet<T> getUpperBounds() {
    return upperBounds;
  }

  @Override
  public List<Long> getFrequencies() {
    return frequencies;
  }
  
  @Override
  public Iterator<Bucket<T>> iterator() {
    return new Iterator<Bucket<T>>() {
      
      private final Iterator<T> lBoundsIter = lowerBounds.iterator();
      private final Iterator<T> uBoundsIter = upperBounds.iterator();
      private final Iterator<Long> freqIter = frequencies.iterator();

      @Override
      public boolean hasNext() {
        return lBoundsIter.hasNext() && uBoundsIter.hasNext() && freqIter.hasNext();
      }

      @Override
      public Bucket<T> next() {
        return new Bucket<T>(lBoundsIter.next(), uBoundsIter.next(), freqIter.next());
      }

      @Override
      public void remove() {
        throw new UnsupportedOperationException();
      }
    };
  }
  
  private boolean checkConsistence() {
    return lowerBounds.size() == upperBounds.size()
        && lowerBounds.size() == frequencies.size()
        && upperBounds.size() == frequencies.size();
  }

}
