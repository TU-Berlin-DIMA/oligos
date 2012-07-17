package de.tu_berlin.dima.oligos.type;

public interface Type<V> extends Comparable<V> {
  public V increment();
  public V decrement();
  public int difference(V other);
  public V parse(String value);
}
