package de.tu_berlin.dima.oligos.type.util.parser;

public class IntegerParser extends AbstractParser<Integer> {

  @Override
  public Integer fromString(String value) {
    return new Integer(removeQuotes(value));
  }

}
