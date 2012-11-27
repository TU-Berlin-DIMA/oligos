package de.tu_berlin.dima.oligos.type.util.parser;

public class StringParser implements Parser<String> {

  @Override
  public String fromString(String value) {
    return value.replace('\'', ' ').trim().replace('#', '~');
  }

  @Override
  public String toString(Object value) {
    return value.toString();
  }
}
