package de.tu_berlin.dima.oligos.type.util.parser;

public abstract class AbstractParser<T> implements Parser<T> {

  public String removeQuotes(final String value) {
    return value.replaceAll("(^')|('$)","");
  }

  @Override
  public String toString(Object value) {
    return value.toString();
  }

}
