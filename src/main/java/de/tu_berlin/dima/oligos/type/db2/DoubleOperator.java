package de.tu_berlin.dima.oligos.type.db2;

import de.tu_berlin.dima.oligos.type.Operator;

public class DoubleOperator implements Operator<Double> {
  
  private final double step;
  
  public DoubleOperator(final double step) {
    this.step = step;
  }

  @Override
  public Double increment(Double value) {
    return value + step;
  }

  @Override
  public Double decrement(Double value) {
    return value - step;
  }

  @Override
  public int difference(Double val1, Double val2) {
    return (int) Math.abs(Math.round(val1 - val2));
  }

}
