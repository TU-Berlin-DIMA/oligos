package de.tu_berlin.dima.oligos.type.util.operator.date;

import java.sql.Time;
import de.tu_berlin.dima.oligos.type.util.operator.Operator;
import de.tu_berlin.dima.oligos.type.util.operator.Operators;

public class TimeOperator implements Operator<Time> {

  @Override
  public int compare(Time o1, Time o2) {
    return o1.compareTo(o2);
  }

  @Override
  public Time increment(Time value) {
    return Operators.increment(value);
  }

  @Override
  public Time decrement(Time value) {
    return Operators.decrement(value);
  }

  @Override
  public long range(Time val1, Time val2) {
    return val2.getTime() - val1.getTime() + 1;
  }

  @Override
  public Time min(Time val1, Time val2) {
    return (val1.compareTo(val2) <= 0) ? val1 : val2;
  }

  @Override
  public Time max(Time val1, Time val2) {
    return (val1.compareTo(val2) >= 0) ? val1 : val2;
  }

}
