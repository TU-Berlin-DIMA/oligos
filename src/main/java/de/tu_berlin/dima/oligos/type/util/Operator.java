package de.tu_berlin.dima.oligos.type.util;

import java.util.Comparator;

public interface Operator<T> extends Comparator<T> {

  public T increment(T value);
  public T increment(T value, T step);
  public T decrement(T value);
  public T decrement(T value, T step);
  public long difference(T val1, T val2);
  public T min(T val1, T val2);
}
