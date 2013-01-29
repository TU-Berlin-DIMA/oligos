package de.tu_berlin.dima.oligos.type.util.operator.date;

import java.sql.Date;
import org.joda.time.DateTime;
import org.joda.time.Days;

import de.tu_berlin.dima.oligos.type.util.operator.Operator;
import de.tu_berlin.dima.oligos.type.util.operator.Operators;

public class DateOperator implements Operator<Date> {

  @Override
  public int compare(Date o1, Date o2) {
    return o1.compareTo(o2);
  }

  @Override
  public Date increment(Date value) {
    return Operators.increment(value);
  }

  @Override
  public Date decrement(Date value) {
    return Operators.decrement(value);
  }

  @Override
  public long range(Date val1, Date val2) {
    DateTime dt1 = new DateTime(val1);
    DateTime dt2 = new DateTime(val2);
    return Days.daysBetween(dt1, dt2).getDays();
  }

  @Override
  public Date min(Date val1, Date val2) {
    return (val1.compareTo(val2) <= 0) ? val1 : val2;
  }

  @Override
  public Date max(Date val1, Date val2) {
    return (val1.compareTo(val2) >= 0) ? val1 : val2;
  }

}
