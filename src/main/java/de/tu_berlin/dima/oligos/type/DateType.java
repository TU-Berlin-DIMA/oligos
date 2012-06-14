package de.tu_berlin.dima.oligos.type;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import org.joda.time.DateTime;
import org.joda.time.Days;


public class DateType implements Type<DateType> {
  
  private static String inFormat = "yyyy-MM-dd";
  
  private final java.util.Date value;
  private final Calendar cal;
  
  public DateType(java.util.Date value) {
    this.value = value;
    this.cal = Calendar.getInstance();
  }
  
  public DateType(String value) {
    java.util.Date date = new java.util.Date(0);
    SimpleDateFormat inputFormat = new SimpleDateFormat(inFormat);
    try {
      date = inputFormat.parse(value.replaceAll("\'", ""));
    } catch (ParseException e) {
      throw new IllegalArgumentException("Could not parse " + date
          + " with pattern" + inputFormat.toPattern());
    }
    this.value = date;
    this.cal = Calendar.getInstance();
  }
  
  public static void setInputFormat(String format) {
    inFormat = format;
  }

  @Override
  public int compareTo(DateType o) {
    return this.value.compareTo(o.value);
  }

  @Override
  public DateType increment() {
    cal.setTime(value);
    cal.add(Calendar.DATE, 1);
    return new DateType(cal.getTime());
  }

  @Override
  public DateType decrement() {
    cal.setTime(value);
    cal.add(Calendar.DATE, -1);
    return new DateType(cal.getTime());
  }

  @Override
  public int difference(DateType other) {
    DateTime dt1 = new DateTime(this.value);
    DateTime dt2 = new DateTime(other.value);
    return Days.daysBetween(dt1, dt2).getDays();
  }

  @Override
  public DateType parse(String value) {
    // TODO Auto-generated method stub
    return null;
  }
  
  public String toString() {
    SimpleDateFormat outFormat = new SimpleDateFormat(inFormat);
    return outFormat.format(value);
  }

}
