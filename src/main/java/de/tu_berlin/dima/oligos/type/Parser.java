package de.tu_berlin.dima.oligos.type;

public interface Parser<V> {
  V parse(String input);
}
