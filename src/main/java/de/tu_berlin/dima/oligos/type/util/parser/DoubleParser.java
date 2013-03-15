package de.tu_berlin.dima.oligos.type.util.parser;

public class DoubleParser extends AbstractParser<Double> {

  @Override
  public Double fromString(String value) {
    return Double.valueOf(removeQuotes(value));
  }

}
