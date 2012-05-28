package de.tu_berlin.dima.oligos.type;

public interface Operator<V extends Comparable<V>> {
  V increment(V value);
  V decrement(V value);
  int difference(V val1, V val2); 
}
