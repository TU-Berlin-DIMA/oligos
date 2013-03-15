package de.tu_berlin.dima.oligos.type.util.parser;

public class LongParser extends AbstractParser<Long> {

  @Override
  public Long fromString(String value) {
    return Long.valueOf(removeQuotes(value));
  }

}
