package de.tu_berlin.dima.oligos.type.util.parser;

public class ShortParser extends AbstractParser<Short> {

  @Override
  public Short fromString(String value) {
    return Short.valueOf(removeQuotes(value));
  }

}
