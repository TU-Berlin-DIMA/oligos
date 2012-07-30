package de.tu_berlin.dima.oligos.stats;

import java.util.Iterator;
import java.util.List;
import java.util.SortedMap;
import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import de.tu_berlin.dima.oligos.type.util.Operator;

public class QuantileHistogram<T> implements Histogram<T> {
  
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
  
  public void addBound(T bound, long frequency) {
    buckets.put(bound, frequency);
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

  @Override
  public long getElementsInRange() {
    // TODO Auto-generated method stub
    return 0;
  }
  
  public T getLowerBoundAt(int bucket) {
    T lBound = min;
    int i = 0;
    Iterator<T> uBoundIter = buckets.keySet().iterator();
    while (i < bucket && uBoundIter.hasNext()) {
      lBound = uBoundIter.next();
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
  
  public List<T> getLowerBounds() {
    List<T> lBounds = Lists.newArrayList();
    for (int i = 0; i < getNumberOfBuckets(); i++) {
      lBounds.add(getLowerBoundAt(i));
    }
    return lBounds;
  }
  
  public List<T> getUpperBounds() {
    List<T> uBounds = Lists.newArrayList();
    for (int i = 0; i < getNumberOfBuckets(); i++) {
      uBounds.add(getLowerBoundAt(i));
    }
    return uBounds;
  }
  
}
