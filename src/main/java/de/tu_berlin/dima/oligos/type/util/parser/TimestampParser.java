package de.tu_berlin.dima.oligos.type.util.parser;

import java.sql.Timestamp;

public class TimestampParser implements Parser<Timestamp> {

  @Override
  public Timestamp fromString(String value) {
    char[] vals = value.toCharArray();
    vals[10] = ' ';
    vals[13] = ':';
    vals[16] = ':';
    return Timestamp.valueOf(new String(vals));
  }

  @Override
  public String toString(Object value) {
    return value.toString();
  }

}
