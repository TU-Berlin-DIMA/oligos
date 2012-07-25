package de.tu_berlin.dima.oligos.stats;

public interface Histogram<T> {

  public int getNumberOfBuckets();

  public int getBucketOf(T value);

  public long getTotalNumberOfValues();

  public long getElementsInRange();
}
