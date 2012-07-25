package de.tu_berlin.dima.oligos.type.util;

public interface Parser<T> {

  public T fromString(String value);

  public String toString(T value);
}
