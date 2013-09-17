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

import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.SortedMap;
import java.util.SortedSet;
import java.util.TreeMap;

import com.google.common.collect.Sets;

import de.tu_berlin.dima.oligos.type.util.operator.Operator;

public class QuantileHistogram<T> extends AbstractHistogram<T> {
  
  private T min;
  private SortedMap<T, Long> buckets;
  
  public QuantileHistogram(Operator<T> operator) {
    this.min = null;
    this.buckets = new TreeMap<T, Long>(operator);
  }
  
  public QuantileHistogram(T min, Operator<T> operator) {
    setOperator(operator);
    this.min = min;
    this.buckets = new TreeMap<T, Long>(operator);
  } 
  
  public void setMin(T min) {
    this.min = min;
  }
  
  public void addBound(T upperBound, long frequency) {
    buckets.put(upperBound, frequency);
  }
  
  @Override
  public T getMin() {
    return min;
  }
  
  @Override
  public T getMax() {
    return buckets.lastKey();
  }
  
  @Override
  public void add(T lowerBound, T upperBound, long frequency) {
    // TODO check consistency here, i.e. if lowerbound matches?
    addBound(upperBound, frequency);
  }
  
  @Override
  public int getNumberOfBuckets() {
    return this.buckets.size();
  }

  @Override
  public int getBucketOf(T value) {
    int index = 0;
    for (T bound : buckets.keySet()) {
      if (getOperator().compare(value, bound) > 0) {
        index++;
      }
    }
    return index;
  }

  @Override
  public long getTotalNumberOfValues() {
    long total = 0;
    for (long cnt : buckets.values()) {
      total += cnt;
    }
    return total;
  }
  
  public T getLowerBoundAt(int bucket) throws SQLException {
    T lBound = min;
    int i = 0;
    Iterator<T> uBoundIter = buckets.keySet().iterator();
    while (i < bucket && uBoundIter.hasNext()) {
      lBound = getOperator().increment(uBoundIter.next());
      i++;
    }
    return lBound;
  }
  
  public T getUpperBoundAt(int bucket) {
    T uBound = null;
    int i = 0;
    for (T ub : buckets.keySet()) {
      if (i == bucket) {
        uBound = ub;
      }
      i++;
    }
    return uBound;
  }
  
  public long getFrequencyAt(int bucket) {
    return new ArrayList<Long>(buckets.values()).get(bucket);
  }
  
  public SortedSet<T> getLowerBounds() throws SQLException {
    SortedSet<T> lBounds = Sets.newTreeSet(getOperator());
    for (int i = 0; i < getNumberOfBuckets(); i++) {
      lBounds.add(getLowerBoundAt(i));
    }
    return lBounds;
  }
  
  public SortedSet<T> getUpperBounds() {    
    return (SortedSet<T>) buckets.keySet();
  }
  
  public List<Long> getFrequencies() {
    return new ArrayList<Long>(buckets.values());
  }
  
  //@Override
  public void setFrequencyAt(int bucket, long frequency) {
    T uBound = getUpperBoundAt(bucket);
    buckets.put(uBound, frequency);
  }
  
  @Override
  public Iterator<Bucket<T>> iterator() {
    return new Iterator<Bucket<T>>() {
      
      private int index = 0;

      @Override
      public boolean hasNext() {
        return index < getNumberOfBuckets();
      }

      @Override
      public Bucket<T> next() {
        Bucket<T> bucket = null;
				try {
					bucket = new Bucket<T>(getLowerBoundAt(index),
					    getUpperBoundAt(index), getFrequencyAt(index));
				} catch (SQLException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
        index++;
        return bucket;
      }

      @Override
      public void remove() {
        throw new UnsupportedOperationException();
      }
    };
  }
  
}
