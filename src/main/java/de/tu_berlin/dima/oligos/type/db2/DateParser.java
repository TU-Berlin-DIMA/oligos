package de.tu_berlin.dima.oligos.type.db2;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;

import de.tu_berlin.dima.oligos.type.Parser;

public class DateParser implements Parser<Date> {
  
  private final SimpleDateFormat dateFormat;
  
  public DateParser(final String format) {
    this.dateFormat = new SimpleDateFormat(format);
  }

  @Override
  public Date parse(String input) {
    Date date = new Date(0);
    try {
      date = dateFormat.parse(input.replaceAll("\'", ""));
    } catch (ParseException e) {
      // TODO Auto-generated catch block
      e.printStackTrace();
    }
    return date;
  }

}
