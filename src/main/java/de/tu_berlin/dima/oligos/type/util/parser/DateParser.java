package de.tu_berlin.dima.oligos.type.util.parser;

import java.sql.Date;
import java.text.SimpleDateFormat;

public class DateParser extends AbstractParser<Date> {
  
  private final static String DEFAULT_OUTPUT_FORMAT = "yyyy-MM-dd";
  
  @Override
  public Date fromString(String value) {
    return Date.valueOf(removeQuotes(value));
  }

  @Override
  public String toString(Object value) {
    SimpleDateFormat outFormat = new SimpleDateFormat(DEFAULT_OUTPUT_FORMAT);
    return outFormat.format(value);
  }

}
