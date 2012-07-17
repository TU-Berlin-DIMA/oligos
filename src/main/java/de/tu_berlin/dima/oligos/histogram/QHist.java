package de.tu_berlin.dima.oligos.histogram;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import com.google.common.base.Preconditions;

import de.tu_berlin.dima.oligos.type.Type;

/**
 * A representation of a Quantile histogram as used by IBM DB2.<br />
 * 
 * This Class contains methods that could basically divided into to categories:<br />
 * <ol>
 * <li>Methods for buckets</li>
 * <li>Methods for individual values (i.e. their approximation) within buckets</li>
 * </ol>
 * <b>Buckets</b> are identified by their index number <code>(0..n)</code> and
 * their methods end with the suffix <code>at</code>. <b>Value methods</b> take
 * the desired values as parameter, their suffix is <code>of</code>.
 * 
 * @author Christoph Bruecke (christoph.bruecke@campus.tu-berlin.de)
 * 
 */
public class QHist<V extends Type<V>> implements BucketHistogram<V> {

  private final V[] boundaries;
  private final long[] frequencies;
  private final long numberOfNulls;
  private final long cardinality;
  private final V min;
  private final V max;

  /**
   * Constructor for QHist. Creates a quantile histogram with the specified
   * boundaries, bucket frequencies, minimum value and the maximum for the
   * approximated domain.
   * 
   * @param boundaries
   *          Ordered array of boundaries.
   * @param frequencies
   *          Frequencies of the buckets, given by the boundaries.
   * @param min
   *          Minimum value of the given domain.
   * @param max
   *          Maximum value of the given domain.
   */
  public QHist(final V[] boundaries, final long[] frequencies, V min,
      long cardinality, long numNulls) {
    this.boundaries = boundaries;
    this.frequencies = frequencies;
    if (min.compareTo(boundaries[0]) == 0) {
      this.min = min.decrement();
    } else {
      this.min = min;
    }
    this.max = boundaries[boundaries.length - 1];
    this.cardinality = cardinality;
    this.numberOfNulls = numNulls;
    
    // change upper bounds such that for each element per bucket
    // lower <= v < upper holds
    updateBoundaries();
  }

  /**
   * Constructor for QHist. Creates a quantile histogram with the specified
   * boundaries, bucket frequencies, minimum value and the maximum for the
   * approximated domain.
   * 
   * @param boundaries
   *          Ordered array of boundaries.
   * @param frequencies
   *          Frequencies of the buckets, given by the boundaries.
   * @param min
   *          Minimum value of the given domain.
   * @param max
   *          Maximum value of the given domain.
   */
  public QHist(final V[] boundaries, final long[] frequencies, V min, V max,
      long cardinality, long numNulls) {
    this.boundaries = boundaries;
    this.frequencies = frequencies;
    this.min = min;
    if (max.compareTo(boundaries[boundaries.length - 1]) > 0) {
      this.max = max;
    } else {
      this.max = boundaries[boundaries.length - 1];
    }
    this.cardinality = cardinality;
    this.numberOfNulls = numNulls;
  }

  public int numberOfBuckets() {
    return this.frequencies.length;
  }

  @Override
  public long numberOfElements() {
    return this.frequencies[numberOfBuckets() - 1];
  }
  
  @Override
  public V[] lowerBounds() {
    List<V> bounds = new ArrayList<V>();
    for (int i = 0; i < numberOfBuckets(); i++) {
      bounds.add(lowerBoundAt(i));
    }
    return bounds.toArray(Arrays.copyOfRange(boundaries, 0, 1));
  }
  
  @Override
  public V[] upperBounds() {
    return boundaries;
  }

  @Override
  public long numberOfNulls() {
    return numberOfNulls;
  }

  @Override
  public V min() {
    return min;
  }

  @Override
  public V max() {
    return max;
  }

  @Override
  public int indexOf(V value) {
    int i = 0;
    int len = numberOfBuckets();
    while (value.compareTo(boundaries[i]) > 0) {
      if (i < len - 1) {
        i++;
      } else {
        i = -1;
        break;
      }
    }
    return i;
  }

  @Override
  public V lowerBoundAt(int index) {
    checkIndex(index);
    V lBound = null;
    if (index == 0) {
      lBound = min;
    } else {
      lBound = boundaries[index - 1];
    }
    return lBound;
  }

  @Override
  public V upperBoundAt(int index) {
    checkIndex(index);
    V uBound = null;
    uBound = boundaries[index];
    return uBound;
  }

  @Override
  public long cardinalityAt(int index) {
    return lowerBoundAt(index).difference(upperBoundAt(index));
  }

  @Override
  public long frequencyAt(int index) {
    checkIndex(index);
    long freq = 0;
    if (index > 0) {
      freq = frequencies[index] - frequencies[index - 1];
    } else {
      freq = frequencies[index];
    }

    return freq;
  }

  @Override
  public long cumFrequencyAt(int index) {
    checkIndex(index);
    return frequencies[index];
  }

  @Override
  public long frequencyOf(V value) {
    int index = indexOf(value);
    long freq = 0;
    if (index > -1) {
      freq = frequencyAt(index) / cardinalityAt(index);
    }
    return freq;
  }

  @Override
  public double probabilityOf(V value) {
    int index = indexOf(value);
    double bucketProb = frequencyAt(index) / numberOfElements();
    double valueInBucketProb = 1 / cardinalityAt(index);
    return bucketProb * valueInBucketProb;
  }

  public String toString() {
    StringBuilder strBld = new StringBuilder();
    strBld
        .append("Bucket No.\tLower Bound\tUpper Bound\tFrequency\tCum. Frequency\n\n");
    for (int i = 0; i < numberOfBuckets(); i++) {
      strBld.append(i);
      strBld.append('\t');
      strBld.append(lowerBoundAt(i));
      strBld.append('\t');
      strBld.append(upperBoundAt(i));
      strBld.append('\t');
      strBld.append(frequencyAt(i));
      strBld.append('\t');
      strBld.append(cumFrequencyAt(i));
      strBld.append("\n");
    }
    return strBld.toString();
  }

  protected V[] boundaries() {
    return boundaries;
  }

  protected long[] frequencies() {
    return frequencies;
  }

  protected long cardinality() {
    return cardinality;
  }

  private void checkIndex(int index) {
    Preconditions.checkArgument(index >= 0 && index < numberOfBuckets(),
        "No valid bucket index was given. (0 <= index < numberOfBuckets)");
  }

  private void updateBoundaries() {
    for (int i = 0; i < boundaries.length; i++) {
      boundaries[i] = boundaries[i].increment();
    }
  }
}
