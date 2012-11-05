package de.tu_berlin.dima.oligos.stat.histogram;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.SortedMap;
import java.util.SortedSet;

import com.google.common.collect.Maps;
import com.google.common.collect.Sets;

import de.tu_berlin.dima.oligos.stat.Bucket;
import de.tu_berlin.dima.oligos.type.util.operator.Operator;

public class QuantileHistogram<T> extends AbstractHistogram<T> {
  
  private T min;
  private Operator<T> operator;
  private SortedMap<T, Long> buckets;
  
  public QuantileHistogram(Operator<T> operator) {
    this.operator = operator;
    this.min = null;
    this.buckets = Maps.newTreeMap(operator);
  }
  
  public QuantileHistogram(T min, Operator<T> operator) {
    this.operator = operator;
    this.min = min;
    this.buckets = Maps.newTreeMap(operator);
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
      if (operator.compare(value, bound) > 0) {
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
  
  public T getLowerBoundAt(int bucket) {
    T lBound = min;
    int i = 0;
    Iterator<T> uBoundIter = buckets.keySet().iterator();
    while (i < bucket && uBoundIter.hasNext()) {
      lBound = operator.increment(uBoundIter.next());
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
  
  public SortedSet<T> getLowerBounds() {
    SortedSet<T> lBounds = Sets.newTreeSet(operator);
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
        Bucket<T> bucket = new Bucket<T>(getLowerBoundAt(index),
            getUpperBoundAt(index), getFrequencyAt(index));
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
