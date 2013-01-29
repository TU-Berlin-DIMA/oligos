package de.tu_berlin.dima.oligos.type.util.parser;

public class CharParser implements Parser<Character> {

  @Override
  public Character fromString(String value) {
    return value.charAt(0);
  }

  @Override
  public String toString(Object value) {
    return value.toString();
  }

}
