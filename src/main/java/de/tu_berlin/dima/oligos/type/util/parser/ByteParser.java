package de.tu_berlin.dima.oligos.type.util.parser;

public class ByteParser implements Parser<Byte> {

  @Override
  public Byte fromString(String value) {
    return Byte.valueOf(value);
  }

  @Override
  public String toString(Object value) {
    return value.toString();
  }

}
