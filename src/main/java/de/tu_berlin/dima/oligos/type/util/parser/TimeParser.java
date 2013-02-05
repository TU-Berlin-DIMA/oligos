package de.tu_berlin.dima.oligos.type.util.parser;

import java.sql.Time;

public class TimeParser implements Parser<Time> {

  @Override
  public Time fromString(String value) {
    return Time.valueOf(value);
  }

  @Override
  public String toString(Object value) {
    return value.toString();
  }

}
