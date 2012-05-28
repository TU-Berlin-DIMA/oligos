package de.tu_berlin.dima.oligos.type.db2;

import java.util.Calendar;
import java.util.Date;

import org.joda.time.DateTime;
import org.joda.time.Days;

import de.tu_berlin.dima.oligos.type.Operator;

public class DateOperator implements Operator<Date> {
  private Calendar cal = Calendar.getInstance();

  @Override
  public Date increment(Date value) {
    cal.setTime(value);
    cal.add(Calendar.DATE, 1);
    return cal.getTime();
  }

  @Override
  public Date decrement(Date value) {
    cal.setTime(value);
    cal.add(Calendar.DATE, -1);
    return cal.getTime();
  }

  @Override
  public int difference(Date val1, Date val2) {
    DateTime dt1 = new DateTime(val1);
    DateTime dt2 = new DateTime(val2);
    return Days.daysBetween(dt1, dt2).getDays();
  }
}
