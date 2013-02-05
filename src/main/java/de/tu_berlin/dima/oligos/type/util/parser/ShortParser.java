package de.tu_berlin.dima.oligos.type.util.parser;

public class ShortParser implements Parser<Short> {

  @Override
  public Short fromString(String value) {
    return Short.valueOf(value);
  }

  @Override
  public String toString(Object value) {
    return value.toString();
  }

}
