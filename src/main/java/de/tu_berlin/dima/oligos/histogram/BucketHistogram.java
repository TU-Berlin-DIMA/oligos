package de.tu_berlin.dima.oligos.histogram;

import de.tu_berlin.dima.oligos.type.Operator;

public interface BucketHistogram<V extends Comparable<V>> {

  public Operator<V> operator();

  public int numberOfBuckets();

  public V[] lowerBounds();

  public V[] upperBounds();

  /*
   * Domain information
   */
  /**
   * Obtain the total number of elements within the histogram.
   */
  public long numberOfElements();

  public V min();

  public V max();

  public long numberOfNulls();

  /*
   * Bucket methods
   */
  public V lowerBoundAt(int index);

  public V upperBoundAt(int index);

  public long frequencyAt(int index);

  public long cumFrequencyAt(int index);

  public long cardinalityAt(int index);

  /*
   * Value methods
   */
  public int indexOf(V value);

  public long frequencyOf(V value);

  public double probabilityOf(V value);
}
