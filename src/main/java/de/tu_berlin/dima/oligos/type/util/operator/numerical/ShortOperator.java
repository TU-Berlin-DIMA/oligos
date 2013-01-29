package de.tu_berlin.dima.oligos.type.util.operator.numerical;

public class ShortOperator extends AbstractIntegralOperator<Short> {

  @Override
  public long range(Short val1, Short val2) {
    return Math.abs(val2 - val1) + 1;
  }

}
