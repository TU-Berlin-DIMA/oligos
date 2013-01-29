package de.tu_berlin.dima.oligos.type.util.operator.numerical;

import java.math.BigDecimal;

public class BigDecimalOperator extends AbstractIntegralOperator<BigDecimal> {

  @Override
  public long range(BigDecimal val1, BigDecimal val2) {
    long unscaledDiff = val1.unscaledValue().subtract(val2.unscaledValue()).longValue();
    return Math.abs(unscaledDiff) + 1l;
  }

}
