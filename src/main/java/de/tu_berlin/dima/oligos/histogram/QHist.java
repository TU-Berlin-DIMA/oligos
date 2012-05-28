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
 * their methods end with the suffix <code>at</code>. <b>Value methods</b> take
 * the desired values as parameter, their suffix is <code>of</code>.
 * 
 * @author Christoph Brücke (christoph.bruecke@campus.tu-berlin.de)
 * 
 */
public class QHist<V extends Comparable<V>> {

  private final V[] boundaries;
  private final long[] frequencies;
  private final long numberOfNulls;
  private final long cardinality;
  private final V min;
  private final V max;
  private final Operator<V> operator;

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
  /*
   * public QHist(final V[] boundaries, final int[] frequencies, V min) {
   * this.boundaries = boundaries; this.frequencies = frequencies; this.min =
   * min; this.max = boundaries[boundaries.length - 1]; }
   */

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
      long cardinality, long numNulls, Operator<V> op) {
    this.boundaries = boundaries;
    this.frequencies = frequencies;
    this.min = min;
    this.max = boundaries[boundaries.length - 1];
    this.cardinality = cardinality;
    this.numberOfNulls = numNulls;
    this.operator = op;
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
      long cardinality, long numNulls, Operator<V> op) {
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
    this.operator = op;
  }

  public int getNumBuckets() {
    return this.frequencies.length;
  }

  public long getNumElements() {
    return this.frequencies[getNumBuckets() - 1];
  }

  public V getMin() {
    return min;
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
      lBound = operator.increment(boundaries[index - 1]);
    }
    return lBound;
  }

  public V getUpperBoundAt(int index) {
    checkIndex(index);
    V uBound = null;
    uBound = boundaries[index];
    return uBound;
  }

  public long getFrequencyAt(int index) {
    checkIndex(index);
    long freq = 0;
    if (index > 0) {
      freq = frequencies[index] - frequencies[index - 1];
    } else {
      freq = frequencies[index];
    }

    return freq;
  }

  public long getCumFrequencyAt(int index) {
    checkIndex(index);
    return frequencies[index];
  }

  public long getFrequencyOf(V value, int numElems) {
    int index = getIndexOf(value);
    long freq = 0;
    if (index > -1) {
      freq = getFrequencyAt(index) / numElems;
    }
    return freq;
  }

  public long getCumFrequencyOf(V value) {
    // TODO return the frequency of the value WITHIN the bucket
    int index = getIndexOf(value);
    return getCumFrequencyAt(index);
  }

  public String toString() {
    StringBuilder strBld = new StringBuilder();
    strBld
        .append("Bucket No.\tLower Bound\tUpper Bound\tFrequency\tCum. Frequency\n\n");
    for (int i = 0; i < getNumBuckets(); i++) {
      strBld.append(i);
      strBld.append('\t');
      strBld.append(getLowerBoundAt(i));
      strBld.append('\t');
      strBld.append(getUpperBoundAt(i));
      strBld.append('\t');
      strBld.append(getFrequencyAt(i));
      strBld.append('\t');
      strBld.append(getCumFrequencyAt(i));
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

  protected V min() {
    return min;
  }

  protected V max() {
    return max;
  }

  protected long numberOfNulls() {
    return numberOfNulls;
  }

  protected long cardinality() {
    return cardinality;
  }

  private void checkIndex(int index) {
    Preconditions.checkArgument(index >= 0 && index < getNumBuckets(),
        "No valid bucket index was given. (0 <= index < NUMBUCKETS)");
  }
}
