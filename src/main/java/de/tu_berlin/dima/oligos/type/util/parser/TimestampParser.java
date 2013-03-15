package de.tu_berlin.dima.oligos.type.util.parser;

import java.sql.Timestamp;

public class TimestampParser extends AbstractParser<Timestamp> {

  @Override
  public Timestamp fromString(String value) {
    char[] vals = removeQuotes(value).toCharArray();
    vals[10] = ' ';
    vals[13] = ':';
    vals[16] = ':';
    return Timestamp.valueOf(new String(vals));
  }

}
