package de.tu_berlin.dima.oligos.histogram;

import com.google.common.base.Preconditions;

/**
 * A representation of a Quantil histogram as used by IBM DB2.<br />
 * 
 * This Class contains methods that could basically devided into to categories:<br />
 * <ol>
 * <li>Methods for buckets</li>
 * <li>Methods for individual values (i.e. their approximation) within buckets</li>
 * </ol>
 * <b>Buckets</b> are identified by their index number <code>(0..n)</code> and
 * end with the suffix <code>at</code>. <b>Value methods</b> take the desired
 * values as parameter, their suffix is <code>of</code>.
 * 
 * @author Christoph Brücke (christoph.bruecke@campus.tu-berlin.de)
 * 
 */
public class QHist<V extends Comparable<V>> {

  private final V[] boundaries;
  private final int[] frequencies;
  private final V min;
  private final V max;

  /**
   * Constructor for QHist. Creates a quantile histogram with the specified
   * boundaries, bucket frequencies and minimum value for the approximated
   * domain. The maximum value of the domain is taken from the boundaries.
   * 
   * @param boundaries
   *          Ordered array of boundaries.
   * @param frequencies
   *          Frequencies of the buckets, given by the boundaries.
   * @param min
   *          Minimum value of the given domain.
   */
  public QHist(final V[] boundaries, final int[] frequencies, V min) {
    this.boundaries = boundaries;
    this.frequencies = frequencies;
    this.min = min;
    this.max = boundaries[boundaries.length - 1];
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
  public QHist(final V[] boundaries, final int[] frequencies, V min, V max) {
    this.boundaries = boundaries;
    this.frequencies = frequencies;
    this.min = min;
    this.max = max;
  }

  public int getNumBuckets() {
    return this.frequencies.length;
  }

  public int getNumElements() {
    return this.frequencies[getNumBuckets() - 1];
  }

  public int getIndexOf(Comparable<V> value) {
    int i = 0;
    int len = getNumBuckets();
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

  public V getLowerBoundAt(int index) {
    checkIndex(index);
    V lBound = null;
    if (index == 0) {
      lBound = min;
    } else {
      lBound = boundaries[index - 1];
    }
    return lBound;
  }

  public V getUpperBoundAt(int index) {
    checkIndex(index);
    V uBound = null;
    uBound = boundaries[index];
    return uBound;
  }

  public int getFrequencyAt(int index) {
    checkIndex(index);
    int freq = 0;
    if (index > 0) {
      freq = frequencies[index] - frequencies[index - 1];
    } else {
      freq = frequencies[index];
    }

    return freq;
  }

  public int getCumFrequencyAt(int index) {
    checkIndex(index);
    return frequencies[index];
  }

  public int getFrequencyOf(V value, int numElems) {
    int index = getIndexOf(value);
    int freq = 0;
    if (index > -1) {
      freq = getFrequencyAt(index) / numElems;
    }
    return freq;
  }

  public int getCumFrequencyOf(V value) {
    // TODO return the frequency of the value WITHIN the bucket
    int index = getIndexOf(value);
    return getCumFrequencyAt(index);
  }

  private void checkIndex(int index) {
    Preconditions.checkArgument(index >= 0 && index < getNumBuckets(),
        "No valid bucket index was given. (0 <= index < NUMBUCKETS)");
  }
}
