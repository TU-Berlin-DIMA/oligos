package de.tu_berlin.dima.oligos.type.util.operator;

public class IntegerOperator implements Operator<Integer> {

  @Override
  public Integer increment(Integer value) {
    return value + 1;
  }

  @Override
  public Integer increment(Integer value, Integer step) {
    return value + step;
  }

  @Override
  public Integer decrement(Integer value) {
    return value - 1;
  }

  @Override
  public Integer decrement(Integer value, Integer step) {
    return value - step;
  }

  @Override
  public long range(Integer val1, Integer val2) {
    return (long) val2 - val1;
  }
  
  @Override
  public Integer min(Integer val1, Integer val2) {
    return Math.min(val1, val2);
  }

  @Override
  public int compare(Integer o1, Integer o2) {
    return o1.compareTo(o2);
  }
}
