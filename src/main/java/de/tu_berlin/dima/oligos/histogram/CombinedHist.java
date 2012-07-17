package de.tu_berlin.dima.oligos.histogram;

import java.lang.reflect.Array;
import java.util.LinkedList;
import java.util.List;
import java.util.Map.Entry;

import org.apache.commons.lang3.ArrayUtils;

import com.google.common.base.Preconditions;
import com.google.common.collect.Lists;

import de.tu_berlin.dima.oligos.type.Type;

public class CombinedHist<V extends Type<V>> implements
    BucketHistogram<V> {

  private final static int IN_FHIST = -2;
  private final static int NOT_CONTAINED = -1;

  private final ElementHistogram<V> fHist;
  private V[] lowerBounds;
  private V[] upperBounds;
  private long[] frequencies;

  public CombinedHist(BucketHistogram<V> qHist, ElementHistogram<V> fHist) {
    this.fHist = fHist;
    adaptHistogram(qHist);
  }

  @SuppressWarnings("unchecked")
  private void adaptHistogram(BucketHistogram<V> hist) {
    List<V> lBounds = new LinkedList<V>();
    List<V> uBounds = new LinkedList<V>();
    List<Long> freqs = new LinkedList<Long>();
    for (int i = 0; i < hist.numberOfBuckets(); i++) {
      lBounds.add(hist.lowerBoundAt(i));
      uBounds.add(hist.upperBoundAt(i));
      freqs.add(hist.frequencyAt(i));
    }

    // update frequencies
    for (int i = 0; i < hist.numberOfBuckets(); i++) {
      long frequentElems = fHist
          .elementsInRange(lBounds.get(i), uBounds.get(i));
      long freq = freqs.get(i) - frequentElems;
      freqs.set(i, freq);
    }

    // update boundaries
    int index = 0;
    for (Entry<V, Long> e : fHist) {
      V elem = e.getKey();
      boolean found = false;

      while (!found && index < freqs.size() - 1) {
        V lBound = lBounds.get(index);
        V uBound = uBounds.get(index);
        if (uBound.compareTo(elem) > 0) {
          if (lBound.equals(uBound.decrement())) {
            lBounds.remove(index);
            uBounds.remove(index);
            freqs.remove(index);
            found = true;
          } else if (lBound.equals(elem)) {
            lBounds.set(index, elem.increment());
            found = true;
          } else if (uBound.equals(elem.increment())) {
            uBounds.set(index, elem);
            found = true;
            index++;
          } else {
            int div = fHist.numberOfElements(lBound, uBound);
            List<V> vals = fHist.valuesInRange(lBound, uBound);
            long freq = freqs.get(index) / (div + 1);
            freqs.set(index, freq);
            vals = Lists.reverse(vals);
            for (V val : vals) {
              uBounds.add(index, val);
              lBounds.add(index + 1, val.increment());
              freqs.add(index, freq);
            }
            found = true;
          }
        } else {
          index++;
        }
      }
    }
    Class<?> type = lBounds.get(0).getClass();
    this.lowerBounds = (V[]) Array.newInstance(type, lBounds.size());
    this.lowerBounds = lBounds.toArray(this.lowerBounds);
    this.upperBounds = (V[]) Array.newInstance(type, uBounds.size());
    this.upperBounds = uBounds.toArray(this.upperBounds);
    this.frequencies = ArrayUtils.toPrimitive(freqs.toArray(new Long[0]));
  }

  @Override
  public String toString() {
    StringBuilder strBld = new StringBuilder();
    double cumProb = 0.0;
    // HEADER
    strBld.append("# numberofexactvals: ");
    strBld.append(fHist.numberOfElements());
    strBld.append('\n');
    strBld.append("# numberofbins: ");
    strBld.append(numberOfBuckets());
    strBld.append('\n');
    strBld.append("# nullprobability: ");
    strBld.append(numberOfNulls() / (double) numberOfElements());
    strBld.append('\n');
    for (Entry<V, Long> e : fHist) {
      double prob = probabilityOf(e.getKey());
      cumProb += prob;
      strBld.append(prob);
      strBld.append('\t');
      strBld.append(e.getKey().toString());
      strBld.append('\n');
    }
    for (int i = 0; i < numberOfBuckets(); i++) {
      double prob = frequencyAt(i) / (double) numberOfElements();
      cumProb += prob;
      strBld.append(prob);
      strBld.append('\t');
      strBld.append(lowerBoundAt(i).toString());
      strBld.append('\t');
      strBld.append(upperBoundAt(i).toString());
      strBld.append('\n');
    }
    //strBld.append(cumProb);

    return strBld.toString();
  }

  public String toString2() {
    StringBuilder strBld = new StringBuilder();
    strBld.append("\nHistogram\nNo\tLower Bound\tUpper Bound\tFrequency\t"
        + "Cum. Frequency\tProbability\tCum. Probability\tCardinality\n");
    strBld.append("Num. Elements: " + numberOfElements() + "\n");
    long cumFreq = 0l;
    double cumProb = 0.0;
    for (int i = 0; i < numberOfBuckets(); i++) {
      strBld.append(i);
      strBld.append('\t');
      strBld.append(lowerBoundAt(i));
      strBld.append('\t');
      strBld.append(upperBoundAt(i));
      strBld.append('\t');
      strBld.append(frequencyAt(i));
      strBld.append('\t');
      cumFreq += frequencyAt(i);
      strBld.append(cumFreq);
      strBld.append('\t');
      double prob = frequencyAt(i) / (double) numberOfElements();
      strBld.append(prob);
      strBld.append('\t');
      cumProb += prob;
      strBld.append(cumProb);
      strBld.append('\t');
      strBld.append(cardinalityAt(i));
      strBld.append('\n');
    }

    return strBld.toString();
  }

  public int numberOfBuckets() {
    return frequencies.length;
  }

  @Override
  public long numberOfElements() {
    long count = fHist.numberOfElements();
    for (long freq : frequencies) {
      count += freq;
    }
    return count;
  }

  @Override
  public V min() {
    return (lowerBounds[0].compareTo(fHist.min()) <= 0) ? lowerBounds[0]
        : fHist.min();
  }

  @Override
  public V max() {
    return (upperBounds[0].compareTo(fHist.max()) >= 0) ? upperBounds[upperBounds.length - 1]
        : fHist.max();
  }

  @Override
  public long numberOfNulls() {
    // TODO Auto-generated method stub
    return 0;
  }

  @Override
  public V lowerBoundAt(int index) {
    checkIndex(index);
    return lowerBounds[index];
  }

  @Override
  public V upperBoundAt(int index) {
    checkIndex(index);
    return upperBounds[index];
  }

  @Override
  public long frequencyAt(int index) {
    if (index == IN_FHIST) {
      return 0l;
    } else if (index == NOT_CONTAINED) {
      return 0l;
    } else {
      checkIndex(index);
      return frequencies[index];
    }
  }

  @Override
  public long cumFrequencyAt(int index) {
    checkIndex(index);
    return 0;
  }

  @Override
  public long cardinalityAt(int index) {
    if (index == IN_FHIST) {
      return 1l;
    } else if (index == NOT_CONTAINED) {
      return 0l;
    } else {
      return lowerBoundAt(index).difference(upperBoundAt(index));
    }
  }

  @Override
  public int indexOf(V value) {
    int i = 0;
    int len = numberOfBuckets();
    if (fHist.contains(value)) {
      return IN_FHIST;
    }
    while (value.compareTo(upperBounds[i]) > 0) {
      if (i < len - 1) {
        i++;
      } else {
        i = NOT_CONTAINED;
        break;
      }
    }
    return i;
  }

  @Override
  public long frequencyOf(V value) {
    int index = indexOf(value);
    if (index == IN_FHIST) {
      return fHist.frequencyOf(value);
    } else if (index == NOT_CONTAINED) {
      return 0l;
    } else {
      checkIndex(index);
      return frequencyAt(index) / cardinalityAt(index);
    }
  }

  @Override
  public double probabilityOf(V value) {
    int index = indexOf(value);
    if (index == IN_FHIST) {
      return fHist.frequencyOf(value) / (double) numberOfElements();
    } else if (index == NOT_CONTAINED) {
      return 0.0;
    } else {
      checkIndex(index);
      double bucketProb = frequencyAt(index) / numberOfElements();
      double valueProb = 1.0 / cardinalityAt(index);
      return bucketProb * valueProb;
    }
  }

  private void checkIndex(int index) {
    Preconditions.checkArgument(index >= 0 && index < numberOfBuckets(),
        "No valid bucket index was given. (0 <= index < NUMBUCKETS)");
  }

  @Override
  public V[] lowerBounds() {
    return lowerBounds;
  }

  @Override
  public V[] upperBounds() {
    return upperBounds;
  }
}
