package de.tu_berlin.dima.oligos.type.util.operator;

import java.math.BigDecimal;
import java.math.BigInteger;
import java.sql.Time;
import java.sql.Timestamp;
import java.util.Calendar;
import java.util.Date;

public final class Operators {

  @SuppressWarnings("unchecked")
  public static <T extends Number> T increment(T number) {
    if (number instanceof Byte) {
      return (T) increment((Byte) number);
    }
    else if (number instanceof Short) {
      return (T) increment((Short) number);
    }
    else if (number instanceof Integer) {
      return (T) increment((Integer) number);
    }
    else if (number instanceof Long) {
      return (T) increment((Long) number);
    }
    else if (number instanceof BigInteger) {
      return (T) increment((BigInteger) number);
    }
    else if (number instanceof BigDecimal) {
      return (T) increment((BigDecimal) number);
    } else {
      throw new IllegalArgumentException();
    }
  }

  @SuppressWarnings("unchecked")
  public static <T extends Number> T decrement(T number) {
    if (number instanceof Short) {
      return (T) decrement((Short) number);
    }
    else if (number instanceof Integer) {
      return (T) decrement((Integer) number);
    }
    else if (number instanceof Long) {
      return (T) decrement((Long) number);
    }
    else if (number instanceof BigInteger) {
      return (T) decrement((BigInteger) number);
    }
    else if (number instanceof BigDecimal) {
      return (T) decrement((BigDecimal) number);
    } else {
      throw new IllegalArgumentException();
    }
  }

  @SuppressWarnings("unchecked")
  public static <T extends Date> T increment(T date) {
    Calendar calendar = Calendar.getInstance();
    calendar.clear();
    calendar.setTime(date);
    if (date instanceof java.sql.Date) {
      calendar.add(Calendar.DATE, 1);
      return (T) new java.sql.Date(calendar.getTimeInMillis());
    }
    else if (date instanceof Time) {
      calendar.add(Calendar.SECOND, 1);
      return (T) new Time(calendar.getTimeInMillis());
    }
    else if (date instanceof Timestamp) {
      calendar.add(Calendar.MILLISECOND, 1);
      return (T) new Timestamp(calendar.getTimeInMillis());
    }
    else {
      throw new IllegalArgumentException();
    }
  }

  @SuppressWarnings("unchecked")
  public static <T extends Date> T decrement(T date) {
    Calendar calendar = Calendar.getInstance();
    calendar.clear();
    calendar.setTime(date);
    if (date instanceof java.sql.Date) {
      calendar.add(Calendar.DATE, -1);
      return (T) new java.sql.Date(calendar.getTimeInMillis());
    }
    else if (date instanceof Time) {
      calendar.add(Calendar.SECOND, -1);
      return (T) new Time(calendar.getTimeInMillis());
    }
    else if (date instanceof Timestamp) {
      calendar.add(Calendar.MILLISECOND, -1);
      return (T) new Timestamp(calendar.getTimeInMillis());
    }
    else {
      throw new IllegalArgumentException();
    }
  }

  public static Byte increment(Byte number) {
    return ++number;
  }

  public static Byte decrement(Byte number) {
    return --number;
  }

  public static Short increment(Short number) {
    return ++number;
  }

  public static Short decrement(Short number) {
    return --number;
  }

  public static Integer increment(Integer number) {
    return ++number;
  }

  public static Integer decrement(Integer number) {
    return --number;
  }

  public static Long increment(Long number) {
    return ++number;
  }

  public static Long decrement(Long number) {
    return --number;
  }

  public static BigInteger increment(BigInteger number) {
    return number.add(BigInteger.ONE);
  }

  public static BigInteger decrement(BigInteger number) {
    return number.subtract(BigInteger.ONE);
  }

  public static BigDecimal increment(BigDecimal number) {
    BigDecimal inc = new BigDecimal(BigInteger.ONE, number.scale());
    return number.add(inc);
  }

  public static BigDecimal decrement(BigDecimal number) {
    BigDecimal inc = new BigDecimal(BigInteger.ONE, number.scale());
    return number.subtract(inc);
  }

  public static Timestamp increment(Timestamp timestamp) {
    Calendar calendar = Calendar.getInstance();
    calendar.clear();
    calendar.setTime(timestamp);
    calendar.add(Calendar.MILLISECOND, 1);
    return new Timestamp(calendar.getTimeInMillis());
  }

  public static Timestamp decrement(Timestamp timestamp) {
    Calendar calendar = Calendar.getInstance();
    calendar.clear();
    calendar.setTime(timestamp);
    calendar.add(Calendar.MILLISECOND, 1);
    return new Timestamp(calendar.getTimeInMillis());
  }

  public static Time increment(Time time) {
    Calendar calendar = Calendar.getInstance();
    calendar.clear();
    calendar.setTime(time);
    calendar.add(Calendar.SECOND, 1);
    return new Time(calendar.getTimeInMillis());
  }

  public static Time decrement(Time time) {
    Calendar calendar = Calendar.getInstance();
    calendar.clear();
    calendar.setTime(time);
    calendar.add(Calendar.SECOND, -1);
    return new Time(calendar.getTimeInMillis());
  }

  public static java.sql.Date increment(java.sql.Date date) {
    Calendar calendar = Calendar.getInstance();
    calendar.clear();
    calendar.setTime(date);
    calendar.add(Calendar.DATE, 1);
    return new java.sql.Date(calendar.getTimeInMillis());
  }

  public static java.sql.Date decrement(java.sql.Date date) {
    Calendar calendar = Calendar.getInstance();
    calendar.clear();
    calendar.setTime(date);
    calendar.add(Calendar.DATE, -1);
    return new java.sql.Date(calendar.getTimeInMillis());
  }

  public static <T extends Comparable<T>> T min(T val1, T val2) {
    return (val1.compareTo(val2) <= 0) ? val1 : val2;
  }

  public static <T extends Comparable<T>> T max(T val1, T val2) {
    return (val1.compareTo(val2) >= 0) ? val1 : val2;
  }
}