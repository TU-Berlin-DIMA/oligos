package de.tu_berlin.dima.oligos.histogram;

import java.util.List;
import java.util.Map.Entry;

public interface ElementHistogram<V extends Comparable<V>> extends
    Iterable<Entry<V, Long>> {
  int numberOfElements();

  int numberOfElements(V lowerBound, V upperBound);

  long totalFrequency();

  V min();

  V max();

  boolean contains(V value);

  long elementsInRange(V lowerBound, V upperBound);

  long frequencyOf(V value);

  double probabilityOf(V value);

  List<V> valuesInRange(V lowerBound, V upperBound);
}
