package de.tu_berlin.dima.oligos.stat.distribution.histogram;

import java.util.List;
import java.util.SortedSet;

import de.tu_berlin.dima.oligos.stat.distribution.Distribution;

/**
 * For all elements <code>e</code> per bucket <code>b</code><br/ >
 * <code>l_b &lt;= e &lt;= u_b </code><br />
 * holds.
 * 
 * @author Christoph Brücke (christoph.bruecke@campus.tu-berlin.de)
 * 
 * @param <T>
 */
public interface Histogram<T> extends Distribution<T>, Iterable<Bucket<T>> {

  public void add(T lowerBounds, T upperBound, long frequency);

  public int getNumberOfBuckets();

  public int getBucketOf(T value);

  public long getTotalNumberOfValues();
  
  //public long getFrequencyAt(int bucket);

  public Histogram<T> getExactValues();

  public Histogram<T> getNonExactValues();

  public SortedSet<T> getLowerBounds();

  public SortedSet<T> getUpperBounds();

  public List<Long> getFrequencies();

  public boolean isEmpty();

}
