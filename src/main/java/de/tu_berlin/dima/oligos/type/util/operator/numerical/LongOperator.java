package de.tu_berlin.dima.oligos.type.util.operator.numerical;

public class LongOperator extends AbstractIntegralOperator<Long> {

  @Override
  public long range(Long val1, Long val2) {
    return Math.abs(val2 - val1) + 1l;
  }

}
