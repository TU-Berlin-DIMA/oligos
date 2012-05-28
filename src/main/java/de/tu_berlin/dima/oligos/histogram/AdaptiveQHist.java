package de.tu_berlin.dima.oligos.histogram;

import java.util.Arrays;
import java.util.LinkedList;
import java.util.List;
import java.util.Map.Entry;
import com.google.common.base.Preconditions;

public class AdaptiveQHist<V extends Comparable<V>> {

  private V min;
  private List<V> boundaries;
  private List<Long> frequencies;
  private Operator<V> operator;

  public AdaptiveQHist(QHist<V> qHist, FHist<V> fHist, Operator<V> operator) {
    this.min = qHist.getMin();
    this.boundaries = new LinkedList<V>(Arrays.asList(qHist.boundaries()));
    this.frequencies = new LinkedList<Long>();
    long[] freqs = qHist.frequencies();
    for (int i = 0; i < freqs.length; i++) {
      frequencies.add(qHist.getFrequencyAt(i));
    }
    this.operator = operator;
    adaptHistogram(fHist);
  }

  private void adaptHistogram(FHist<V> fHist) {
    int index = 0;
    for (Entry<V, Long> e : fHist) {
      V elem = e.getKey();
      long count = e.getValue();
      boolean found = false;

      while (!found) {
        V lBound = getLowerBoundAt(index);
        V uBound = getUpperBoundAt(index);
        if (uBound.compareTo(elem) >= 0) { // check whether most frequent item
                                           // is
          // in current bucket i
          if (lBound.equals(uBound)) { // bucket contains exact one element
            frequencies.set(index, count);
            index++;
            found = true;
          } else if (lBound.equals(elem)) {
            boundaries.add(index, elem);
            long oldFreq = frequencies.get(index);
            frequencies.set(index, oldFreq - count);
            frequencies.add(index, count);
            index++;
            found = true;
          } else if (uBound.equals(elem)) {
            boundaries.set(index, operator.decrement(elem));
            long oldFreq = frequencies.get(index);
            frequencies.set(index, oldFreq - count);
            index++;
            boundaries.add(index, elem);
            frequencies.add(index, count);
            index++;
            found = true;
          } else {
            long oldFreq = frequencies.get(index);
            frequencies.set(index, (oldFreq - count) / 2);
            boundaries.add(index, elem);
            frequencies.add(index, count);
            boundaries.add(index, operator.decrement(elem));
            frequencies.add(index, (oldFreq - count) / 2);
            index += 2;
            found = true;
          }
        } else {
          index++;
        }
      }

    }
  }

  public int getNumBuckets() {
    return boundaries.size();
  }

  public V getLowerBoundAt(int index) {
    checkIndex(index);
    V lBound = null;
    if (index == 0) {
      lBound = min;
    } else {
      lBound = operator.increment(boundaries.get(index - 1));
    }
    return lBound;
  }

  public V getUpperBoundAt(int index) {
    checkIndex(index);
    V uBound = boundaries.get(index);
    return uBound;
  }

  public long getFrequencyAt(int index) {
    checkIndex(index);
    return frequencies.get(index);
  }

  public long getCumFrequencyAt(int index) {
    checkIndex(index);
    long cumFreq = 0l;
    for (int i = 0; i <= index; i++) {
      cumFreq += frequencies.get(i);
    }
    return cumFreq;
  }

  public String toString() {
    StringBuilder strBld = new StringBuilder();
    strBld
        .append("Bucket No.\tLower Bound\tUpper Bound\tFrequency\tCum. Frequency\n");
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
      strBld.append('\n');
    }

    return strBld.toString();
  }

  private void checkIndex(int index) {
    Preconditions.checkArgument(index >= 0 && index < getNumBuckets(),
        "No valid bucket index was given. (0 <= index < NUMBUCKETS)");
  }

}
