package de.tu_berlin.dima.oligos.type.util;

public class CharParser implements Parser<Character> {

  @Override
  public Character fromString(String value) {
    return value.charAt(1);
  }

  @Override
  public String toString(Character value) {
    return value.toString();
  }

}
