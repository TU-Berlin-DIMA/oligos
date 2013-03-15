package de.tu_berlin.dima.oligos.type.util.parser;

public class CharParser extends AbstractParser<Character> {

  @Override
  public Character fromString(String value) {
    return removeQuotes(value).charAt(0);
  }

}
