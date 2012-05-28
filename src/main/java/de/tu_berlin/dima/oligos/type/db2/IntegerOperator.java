package de.tu_berlin.dima.oligos.type.db2;

import de.tu_berlin.dima.oligos.type.Operator;

public class IntegerOperator implements Operator<Integer> {

  @Override
  public Integer increment(Integer value) {
    return value + 1;
  }

  @Override
  public Integer decrement(Integer value) {
    return value - 1;
  }

  @Override
  public int difference(Integer val1, Integer val2) {
    return Math.abs(val1 - val2);
  }

}
