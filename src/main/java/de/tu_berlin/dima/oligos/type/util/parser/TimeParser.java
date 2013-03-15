package de.tu_berlin.dima.oligos.type.util.parser;

import java.sql.Time;

public class TimeParser extends AbstractParser<Time> {

  @Override
  public Time fromString(String value) {
    return Time.valueOf(removeQuotes(value));
  }

  @Override
  public String toString(Object value) {
    char[] chrs = value.toString().toCharArray();
    chrs[2] = '.';
    chrs[5] = '.';
    return String.valueOf(chrs);
  }

}
