package de.tu_berlin.dima.oligos.type.util;

import java.util.Calendar;
import java.util.Date;

public class DateOperator implements Operator<Date> {
  
  private static boolean isInstantiated = false;
  private static DateOperator instance = null;
  
  private final Calendar calendar;
  
  private DateOperator() {
    this.calendar = Calendar.getInstance();
  }
  
  public static synchronized DateOperator getInstance() {
    if (isInstantiated) {
      return instance;
    } else {
      isInstantiated = true;
      instance = new DateOperator();
      return instance;
    }
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
    // TODO Auto-generated method stub
    return null;
  }

  @Override
  public Date decrement(Date value) {
    // TODO Auto-generated method stub
    return null;
  }

  @Override
  public Date decrement(Date value, Date step) {
    // TODO Auto-generated method stub
    return null;
  }

  @Override
  public long difference(Date val1, Date val2) {
    // TODO Auto-generated method stub
    return 0;
  }

  @Override
  public Date min(Date val1, Date val2) {
    // TODO Auto-generated method stub
    return null;
  }

}
