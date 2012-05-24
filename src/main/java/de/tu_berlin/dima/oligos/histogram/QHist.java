package de.tu_berlin.dima.oligos.histogram;

import com.google.common.base.Preconditions;

/**
 * A representation of a Quantil histogram as used by IBM DB2.<br />
 * 
 * @author Christoph Brücke (christoph.bruecke@campus.tu-berlin.de)
 * 
 */
public class QHist {

  private final Comparable<?>[] boundaries;
  private final int[] frequencies;
  private final Comparable<?> min;
  private final Comparable<?> max;

  public QHist(final Comparable<?>[] boundaries, final int[] frequencies,
      Comparable<?> min, Comparable<?> max) {
    this.boundaries = boundaries;
    this.frequencies = frequencies;
    this.min = min;
    this.max = max;
  }

  public QHist(final Comparable<?>[] boundaries, final int[] frequencies) {
    this.boundaries = boundaries;
    this.frequencies = frequencies;
    this.min = 0;
    this.max = boundaries[boundaries.length - 1];
  }

  public int getNumBuckets() {
    return this.frequencies.length;
  }

  public int getNumElements() {
    return this.frequencies[getNumBuckets() - 1];
  }

  @SuppressWarnings({ "unchecked", "rawtypes" })
  public int getIndexOf(Comparable value) {
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

  public Comparable<?> getLowerBoundAt(int index) {
    // TODO add range check
    Comparable<?> lBound = null;
    if (index == 0) {
      lBound = min;
    } else {
      lBound = boundaries[index - 1];
    }
    return lBound;
  }

  public Comparable<?> getUpperBoundAt(int index) {
    // TODO add range check
    Comparable<?> uBound = null;
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

  public int getFrequencyOf(Comparable<?> value, int numElems) {
    int index = getIndexOf(value);
    int freq = 0;
    if (index > -1) {
      freq = getFrequencyAt(index) / numElems;
    }
    return freq;
  }
  
  public int getCumFrequencyOf(Comparable<?> value) {
    // TODO return the frequency of the value WITHIN the bucket
    int index = getIndexOf(value);
    return getCumFrequencyAt(index);
  }
  
  private void checkIndex(int index) {
    Preconditions.checkArgument(index >= 0 && index < getNumBuckets(),
    "No valid bucket index was given. (0 <= index < NUMBUCKETS)");
  }
}
