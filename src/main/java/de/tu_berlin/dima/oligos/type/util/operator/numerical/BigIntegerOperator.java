package de.tu_berlin.dima.oligos.type.util.operator.numerical;

import java.math.BigInteger;

public class BigIntegerOperator extends AbstractIntegralOperator<BigInteger> {

  @Override
  public long range(BigInteger val1, BigInteger val2) {
    // | val2 - val1 | + 1
    return val1.subtract(val2).abs().add(BigInteger.ONE).longValue();
  }

}
