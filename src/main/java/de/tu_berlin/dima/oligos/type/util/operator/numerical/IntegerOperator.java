package de.tu_berlin.dima.oligos.type.util.operator.numerical;

public class IntegerOperator extends AbstractIntegralOperator<Integer> {

  @Override
  public long range(Integer val1, Integer val2) {
    return Math.abs(val2 - val1) + 1;
  }

}
