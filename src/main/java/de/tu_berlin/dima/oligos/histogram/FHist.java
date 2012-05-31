package de.tu_berlin.dima.oligos.histogram;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Map.Entry;
import java.util.SortedMap;
import java.util.TreeMap;

import com.google.common.base.Preconditions;

public class FHist<V extends Comparable<V>> implements ElementHistogram<V> {

  private SortedMap<V, Long> frequentElements;
  private long total;

  public FHist() {
    this.frequentElements = new TreeMap<V, Long>();
    this.total = 0l;
  }

  public FHist(V[] values, long[] frequencies) {
    Preconditions.checkArgument(values.length == frequencies.length);
    this.frequentElements = new TreeMap<V, Long>();
    this.total = 0l;
    for (int i = 0; i < values.length; i++) {
      addFrequentElement(values[i], frequencies[i]);
    }
  }

  public void addFrequentElement(final V elem, final long count) {
    frequentElements.put(elem, count);
    total += count;
  }

  public Iterator<Entry<V, Long>> iterator() {
    return frequentElements.entrySet().iterator();
  }

  public String toString() {
    StringBuilder strBld = new StringBuilder();
    for (Entry<V, Long> e : frequentElements.entrySet()) {
      strBld.append(probabilityOf(e.getKey()));
      strBld.append('\t');
      strBld.append(e.getValue());
      strBld.append('\n');
    }

    return strBld.toString();
  }

  @Override
  public int numberOfElements() {
    return frequentElements.size();
  }
  
  @Override
  public int numberOfElements(V lowerBound, V upperBound) {
    int count = 0;
    for (V val : frequentElements.keySet()) {
      if (val.compareTo(lowerBound) >= 0 && val.compareTo(upperBound) < 0) {
        count++;
      }
    }
    return count;
  }

  @Override
  public long totalFrequency() {
    return total;
  }

  @Override
  public V min() {
    return frequentElements.firstKey();
  }

  @Override
  public V max() {
    return frequentElements.lastKey();
  }

  @Override
  public long frequencyOf(V value) {
    Long freq = frequentElements.get(value);
    if (freq == null) {
      freq = 0l;
    }
    return freq;
  }

  @Override
  public double probabilityOf(V value) {
    return frequencyOf(value) / (double) numberOfElements();
  }

  @Override
  public boolean contains(V value) {
    return frequentElements.containsKey(value);
  }

  @Override
  public long elementsInRange(V lowerBound, V upperBound) {
    long total = 0l;
    for (Entry<V, Long> e : frequentElements.entrySet()) {
      V val = e.getKey();
      if (val.compareTo(lowerBound) >= 0 && val.compareTo(upperBound) < 0) {
        total += e.getValue();
      }
    }
    return total;
  }
  
  @Override
  public List<V> valuesInRange(V lowerBound, V upperBound) {
    List<V> vals = new ArrayList<V>();
    for (V val : frequentElements.keySet()) {
      if (val.compareTo(lowerBound) >= 0 && val.compareTo(upperBound) < 0) {
        vals.add(val);
      }
    }
    return vals;
  }
}
