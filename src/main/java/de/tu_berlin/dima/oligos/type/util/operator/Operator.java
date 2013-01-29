package de.tu_berlin.dima.oligos.type.util.operator;

import java.util.Comparator;

public interface Operator<T> extends Comparator<T> {

  /**
   * Increments the given value by the minimum amount
   * @param value
   *  Value to increment
   * @return
   *  Incremented value
   */
  public T increment(T value);
  
  /**
   * Decrements the given value by the minimum amount
   * @param value
   *  Value to decrement
   * @return
   *  Decremented value
   */
  public T decrement(T value);
  
  /**
   * Calculates the range between the given values. I.e. how many unique values
   * exist between val1 and val2, including val1 and val2.
   * @param val1
   * @param val2
   * @return
   *  Range between val1 and val2
   */
  public long range(T val1, T val2);
  
  /**
   * Calculates the minimum of the given values
   * @param val1
   * @param val2
   * @return
   *  val1 if val1 <= val2, val2 otherwise
   */
  public T min(T val1, T val2);

  /**
   * Calculates the maximum of the given values
   * @param val1
   * @param val2
   * @return
   *  val1 if val1 >= val2, val2 otherwise
   */
  public T max(T val1, T val2);
}
