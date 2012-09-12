package de.tu_berlin.dima.oligos.stats;

import java.util.List;
import java.util.SortedSet;

/**
 * For all elements <code>e</code> per bucket <code>b</code><br/ >
 * <code>l_b &lt;= e &lt;= u_b </code><br />
 * holds. 
 * @author Christoph Brücke (christoph.bruecke@campus.tu-berlin.de)
 *
 * @param <T>
 */
public interface Histogram<T> extends Iterable<Bucket<T>> {
  
  public void add(T lowerBounds, T upperBound, long frequency);
  
  public T getMin();
  
  public T getMax();

  public int getNumberOfBuckets();

  public int getBucketOf(T value);

  public long getTotalNumberOfValues();

  public long getElementsInRange();
  
  public long getCardinality();
  
  public SortedSet<T> getLowerBounds();
  
  public SortedSet<T> getUpperBounds();
  
  public List<Long> getFrequencies();
  
  //public void setFrequencyAt(int bucket, long frequency);
}
