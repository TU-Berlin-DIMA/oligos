package de.tu_berlin.dima.oligos.type.util.parser;

public class ByteParser extends AbstractParser<Byte> {

  @Override
  public Byte fromString(String value) {
    return Byte.valueOf(removeQuotes(value));
  }

}
