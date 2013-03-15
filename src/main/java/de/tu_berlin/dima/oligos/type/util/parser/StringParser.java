package de.tu_berlin.dima.oligos.type.util.parser;

public class StringParser extends AbstractParser<String> {

  @Override
  public String fromString(String value) {
    return removeQuotes(value).trim().replace('#', '~');
  }

}
