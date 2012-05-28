package de.tu_berlin.dima.oligos.type.db2;

import java.math.BigDecimal;

import de.tu_berlin.dima.oligos.type.Operator;

public class DecimalOperator implements Operator<BigDecimal> {
  
  private final BigDecimal step;
  
  public DecimalOperator(final BigDecimal step) {
    this.step = step;
  }

  @Override
  public BigDecimal increment(BigDecimal value) {
    return value.add(step);
  }

  @Override
  public BigDecimal decrement(BigDecimal value) {
    return value.subtract(step);
  }

  @Override
  public int difference(BigDecimal val1, BigDecimal val2) {
    double v1 = val1.doubleValue();
    double v2 = val2.doubleValue();
    return (int) Math.abs(Math.round(v1 - v2));
  }

}
