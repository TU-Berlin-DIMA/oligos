package de.tu_berlin.dima.oligos.stat.distribution.histogram;

import java.util.Iterator;
import java.util.List;
import java.util.SortedSet;

import de.tu_berlin.dima.oligos.type.util.operator.Operator;

public abstract class AbstractHistogram<T> implements Histogram<T> {
  
  private Operator<T> operator;

  protected Operator<T> getOperator() {
    return operator;
  }

  protected void setOperator(Operator<T> operator) {
    this.operator = operator;
  }

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
  public long getCardinality() {
    T min = getMin();
    T max = getMax();
    return operator.range(min, max);
  }
  
  /*@Override
  public long getFrequencyAt(int bucket) {
    return getFrequencies().get(bucket);
  }
  
  @Override
  public long getBucketCardinality(int bucket) {
    T upperBound = getUpperBounds().
  }
  
  @Override
  public double getProbabilityOf(T value) {
    int bucket = getBucketOf(value);
    long bucketFreq = getFrequencyAt(bucket);
    double bucketProb = getFrequencyAt(bucket) / getTotalNumberOfValues();
  }*/
  
  @Override
  public double getProbabilityOf(T value) {
    return 0.0;
  }

  @Override
  public Histogram<T> getExactValues() {
    Histogram<T> exactValues = new CustomHistogram<T>(operator);
    for (Bucket<T> bucket : this) {
      T lb = bucket.getLowerBound();
      T ub = bucket.getUpperBound();
      if (operator.compare(lb, ub) == 0) {
        exactValues.add(lb, ub, bucket.getFrequency());
      }
    }
    return exactValues;
  }

  public Histogram<T> getNonExactValues() {
    Histogram<T> nonExactValues = new CustomHistogram<T>(operator);
    for (Bucket<T> bucket : this) {
      T lb = bucket.getLowerBound();
      T ub = bucket.getUpperBound();
      if (operator.compare(lb, ub) != 0) {
        nonExactValues.add(lb, ub, bucket.getFrequency());
      }
    }
    return nonExactValues;
  }

  @Override
  public abstract SortedSet<T> getLowerBounds();

  @Override
  public abstract SortedSet<T> getUpperBounds();

  @Override
  public abstract List<Long> getFrequencies();
  
  @Override
  public boolean isEmpty() {
    return getFrequencies().isEmpty();
  }
  
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
