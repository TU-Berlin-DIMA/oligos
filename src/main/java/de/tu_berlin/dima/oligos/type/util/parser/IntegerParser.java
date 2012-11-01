package de.tu_berlin.dima.oligos.type.util.parser;

public class IntegerParser implements Parser<Integer> {

  @Override
  public Integer fromString(String value) {
    return new Integer(value);
  }
  
  @Override
  public String toString(Object value) {
    return value.toString();
  }

}
