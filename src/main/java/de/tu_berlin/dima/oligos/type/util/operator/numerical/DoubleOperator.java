package de.tu_berlin.dima.oligos.type.util.operator.numerical;

import de.tu_berlin.dima.oligos.type.util.operator.AbstractOperator;

public class DoubleOperator extends AbstractOperator<Double> {

  @Override
  public long range(Double val1, Double val2) {
    long a = Double.doubleToLongBits(val1);
    long b = Double.doubleToLongBits(val2);
    return Math.abs(a - b) + 1L;
  }

  @Override
  public Double increment(Double value) {
    return Math.nextUp(value);
  }

  @Override
  public Double decrement(Double value) {
    return Math.nextAfter(value, Double.NEGATIVE_INFINITY);
  }

}
