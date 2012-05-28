package de.tu_berlin.dima.oligos.histogram;

import java.util.Iterator;
import java.util.Map.Entry;
import java.util.SortedMap;
import java.util.TreeMap;

public class FHist<V extends Comparable<V>> implements Iterable<Entry<V, Long>> {

  private SortedMap<V, Long> frequentElements;
  
  public FHist() {
    this.frequentElements = new TreeMap<V, Long>();
  }
  
  public void addFrequentElement(final V elem, final long count) {
    frequentElements.put(elem, count);
  }

  @Override
  public Iterator<Entry<V, Long>> iterator() {
    return frequentElements.entrySet().iterator();
  }
  
  public String toString() {
    StringBuilder strBld = new StringBuilder();
    strBld.append("Element\tFrequency\n");
    for (Entry<V, Long> e : frequentElements.entrySet()) {
      strBld.append(e.getKey());
      strBld.append('\t');
      strBld.append(e.getValue());
      strBld.append('\n');
    }
    
    return strBld.toString();
  }
}
