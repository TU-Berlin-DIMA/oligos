package de.tu_berlin.dima.oligos.type.util.operator;

import java.math.BigDecimal;
import java.math.RoundingMode;

public class DecimalOperator implements Operator<BigDecimal> {

  private final int scale;
  private final double step;

  public DecimalOperator(int scale) {
    this.scale = scale;
    this.step = Math.pow(10.0, -scale);
  }

  @Override
  public int compare(BigDecimal val1, BigDecimal val2) {
    return val1.compareTo(val2);
  }

  @Override
  public BigDecimal increment(BigDecimal value) {
    return value.add(new BigDecimal(step))
        .setScale(scale, RoundingMode.HALF_UP);
  }

  @Override
  public BigDecimal increment(BigDecimal value, BigDecimal step) {
    throw new UnsupportedOperationException();
  }

  @Override
  public BigDecimal decrement(BigDecimal value) {
    return value.subtract(new BigDecimal(step)).setScale(scale,
        RoundingMode.HALF_UP);
  }

  @Override
  public BigDecimal decrement(BigDecimal value, BigDecimal step) {
    throw new UnsupportedOperationException();
  }

  @Override
  public long range(BigDecimal val1, BigDecimal val2) {
    long unscaledDiff = val1.unscaledValue().subtract(val2.unscaledValue()).longValue();
    return Math.abs(unscaledDiff) + 1l;
  }

  @Override
  public BigDecimal min(BigDecimal val1, BigDecimal val2) {
    if (compare(val1, val2) <= 0) {
      return val1;
    } else {
      return val2;
    }
  }
}
