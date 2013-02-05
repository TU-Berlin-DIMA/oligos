package de.tu_berlin.dima.oligos.type.util.parser;

public class LongParser implements Parser<Long> {

  @Override
  public Long fromString(String value) {
    return Long.valueOf(value);
  }

  @Override
  public String toString(Object value) {
    return value.toString();
  }

}
