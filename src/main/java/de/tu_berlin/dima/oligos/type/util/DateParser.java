package de.tu_berlin.dima.oligos.type.util;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;

public class DateParser implements Parser<Date> {
  
  private final static String DEFAULT_INPUT_FORMAT = "yyyy-MM-dd";
  private final static String DEFAULT_OUTPUT_FORMAT = "yyyy-MM-dd";
  
  private static boolean isInstantiated = false;
  private static DateParser instance = null;

  private DateParser() {};
  
  public static synchronized DateParser getInstance() {
    if (isInstantiated) {
      return instance;
    } else {
      isInstantiated = true;
      instance = new DateParser();
      return instance;
    }
  }
  
  @Override
  public Date fromString(String value) {
    Date date = new java.util.Date(0);
    SimpleDateFormat inputFormat = new SimpleDateFormat(DEFAULT_INPUT_FORMAT);
    try {
      date = inputFormat.parse(value.replaceAll("\'", ""));
    } catch (ParseException e) {
      throw new IllegalArgumentException("Could not parse " + date
          + " with pattern" + inputFormat.toPattern());
    }
    return date;
  }

  @Override
  public String toString(Date value) {
    SimpleDateFormat outFormat = new SimpleDateFormat(DEFAULT_OUTPUT_FORMAT);
    return outFormat.format(value);
  }

}
