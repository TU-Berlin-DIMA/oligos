package de.tu_berlin.dima.oligos.type.util.operator;

import java.util.Calendar;
import java.util.Date;

import org.joda.time.DateTime;
import org.joda.time.Days;

public class DateOperator implements Operator<Date> {

  private final Calendar calendar;
  
  public DateOperator() {
    this.calendar = Calendar.getInstance();
  }

  @Override
  public int compare(Date o1, Date o2) {
    return o1.compareTo(o2);
  }

  @Override
  public Date increment(Date value) {
    calendar.setTime(value);
    calendar.add(Calendar.DATE, 1);
    return calendar.getTime();
  }

  @Override
  public Date increment(Date value, Date step) {
    throw new UnsupportedOperationException();
  }

  @Override
  public Date decrement(Date value) {
    calendar.setTime(value);
    calendar.add(Calendar.DATE, -1);
    return calendar.getTime();
  }

  @Override
  public Date decrement(Date value, Date step) {
    throw new UnsupportedOperationException();
  }

  @Override
  public long range(Date val1, Date val2) {
    DateTime dt1 = new DateTime(val1);
    DateTime dt2 = new DateTime(val2);
    return Days.daysBetween(dt1, dt2).getDays();
  }

  @Override
  public Date min(Date val1, Date val2) {
    if (compare(val1, val2) <= 0) {
      return val1;
    } else {
      return val2;
    }
  }

}
