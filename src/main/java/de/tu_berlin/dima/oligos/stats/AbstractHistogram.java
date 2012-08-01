package de.tu_berlin.dima.oligos.stats;

import java.util.Iterator;
import java.util.List;
import java.util.SortedSet;

import de.tu_berlin.dima.oligos.type.util.operator.Operator;

public abstract class AbstractHistogram<T> implements Histogram<T> {
  
  private Operator<T> operator;

  @Override
  public abstract Iterator<Bucket<T>> iterator();

  @Override
  public abstract void add(T lowerBounds, T upperBound, long frequency);

  @Override
  public abstract T getMin();

  @Override
  public abstract T getMax();

  @Override
  public abstract int getNumberOfBuckets();

  @Override
  public abstract int getBucketOf(T value);

  @Override
  public abstract long getTotalNumberOfValues();

  @Override
  public abstract long getElementsInRange();

  @Override
  public long getCardinality() {
    T min = getMin();
    T max = getMax();
    return operator.difference(min, max);
  }

  @Override
  public abstract SortedSet<T> getLowerBounds();

  @Override
  public abstract SortedSet<T> getUpperBounds();

  @Override
  public abstract List<Long> getFrequencies();
  
  @Override
  public String toString() {
    StringBuilder strBld = new StringBuilder();
    for (Bucket<T> bucket : this) {
      T lBound = bucket.getLowerBound();
      T uBound = bucket.getUpperBound();
      long freq = bucket.getFrequency();
      strBld.append(lBound);
      strBld.append('\t');
      strBld.append(uBound);
      strBld.append('\t');
      strBld.append(freq);
      strBld.append('\n');
    }
    return strBld.toString();
  }

}