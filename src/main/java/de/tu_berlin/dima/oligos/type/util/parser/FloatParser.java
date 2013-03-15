package de.tu_berlin.dima.oligos.type.util.parser;

public class FloatParser extends AbstractParser<Float> {

  @Override
  public Float fromString(String value) {
    return Float.valueOf(removeQuotes(value));
  }

}
